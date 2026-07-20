# Frontend next — manifest

Thư mục này chỉ là bản staging. Chưa có file nào trong `factory-management-frontend` được thay đổi bởi gói này.

## File thay thế

Sao chép các file sau vào đúng đường dẫn tương ứng trong frontend:

| File staging | File đích |
|---|---|
| `package.json` | `factory-management-frontend/package.json` |
| `src/main.tsx` | `factory-management-frontend/src/main.tsx` |
| `src/App.tsx` | `factory-management-frontend/src/App.tsx` |
| `src/types/index.ts` | `factory-management-frontend/src/types/index.ts` |
| `src/routes/roleNavigation.ts` | `factory-management-frontend/src/routes/roleNavigation.ts` |
| `src/routes/AppRoutes.tsx` | `factory-management-frontend/src/routes/AppRoutes.tsx` |
| `src/layouts/DashboardLayout.tsx` | `factory-management-frontend/src/layouts/DashboardLayout.tsx` |
| `src/api/dashboardApi.ts` | `factory-management-frontend/src/api/dashboardApi.ts` |
| `src/api/shiftReportApi.ts` | `factory-management-frontend/src/api/shiftReportApi.ts` |
| `src/api/client.ts` | `factory-management-frontend/src/api/client.ts` |
| `src/api/employeeApi.ts` | `factory-management-frontend/src/api/employeeApi.ts` |
| `src/pages/RoleOverviewPage.tsx` | `factory-management-frontend/src/pages/RoleOverviewPage.tsx` |
| `src/pages/TeamLeaderReportPage.tsx` | `factory-management-frontend/src/pages/TeamLeaderReportPage.tsx` |
| `src/pages/ApprovalPage.tsx` | `factory-management-frontend/src/pages/ApprovalPage.tsx` |
| `src/pages/OperationalPages.tsx` | `factory-management-frontend/src/pages/OperationalPages.tsx` |
| `src/pages/ReportsPage.tsx` | `factory-management-frontend/src/pages/ReportsPage.tsx` |
| `src/pages/EmployeePortalPage.tsx` | `factory-management-frontend/src/pages/EmployeePortalPage.tsx` |

## File mới

| File staging | File đích |
|---|---|
| `src/frontend-next.css` | `factory-management-frontend/src/frontend-next.css` |
| `src/components/StagingReportList.tsx` | `factory-management-frontend/src/components/StagingReportList.tsx` |
| `src/components/StagingDetailsPanel.tsx` | `factory-management-frontend/src/components/StagingDetailsPanel.tsx` |
| `src/components/StagingDetailEditor.tsx` | `factory-management-frontend/src/components/StagingDetailEditor.tsx` |
| `src/api/hrApi.ts` | `factory-management-frontend/src/api/hrApi.ts` |
| `src/pages/HrManagementPage.tsx` | `factory-management-frontend/src/pages/HrManagementPage.tsx` |
| `src/api/executiveDashboardApi.ts` | `factory-management-frontend/src/api/executiveDashboardApi.ts` |
| `src/pages/ExecutiveDashboardPage.tsx` | `factory-management-frontend/src/pages/ExecutiveDashboardPage.tsx` |

Các file Admin và Finance hiện tại không nằm trong manifest nên được giữ nguyên. Employee Portal được thay thế để bổ sung gửi và xem lịch sử đăng ký tăng ca.

Màn hình `DIRECTOR` chỉ còn **Báo cáo điều hành**. Trang này tổng hợp doanh thu, chi phí, lợi nhuận, sản lượng, năng suất, chất lượng, rủi ro và các điểm cần quyết định; không hiển thị danh sách hoặc sự cố của từng máy.

Sau khi sao chép cần chạy `npm install` để thêm `react-router-dom` và cập nhật `package-lock.json`. Gói staging này chưa chạy build theo yêu cầu.

## API backend bắt buộc

0. `GET /api/v1/executive-dashboard?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD&factoryId={id}`
   - Chỉ dành cho `DIRECTOR` (và `ADMIN` nếu backend cần hỗ trợ vận hành).
   - `factoryId` là tùy chọn; bỏ trống nghĩa là toàn công ty.
   - Trả về đúng cấu trúc `ExecutiveDashboardResponse`, chỉ gồm dữ liệu tổng hợp, không trả về chi tiết máy hoặc nhân viên.

1. `GET /api/v1/production-report-staging/my-scope`
   - Trả về tất cả staging report mà JWT hiện tại được xem.
   - TEAM_LEADER cần thấy ít nhất `DRAFT` và `CHANGE_REQUESTED` của tổ được quản lý.
   - Người có quyền duyệt cần thấy `SUBMITTED` trong phạm vi được cấp.
2. `GET /api/v1/production-reports/search/my-scope`
   - Hỗ trợ tối thiểu `fromDate` và `toDate`.
   - Lọc scope ở server, không tin ID do frontend gửi.
3. Các endpoint detail theo report đang được dùng:
   - `GET /machine-downtime-staging/report/{reportId}`
   - `GET /quality-report-staging/report/{reportId}`
   - `GET /material-issue-staging/report/{reportId}`
   - `GET /employee-actual-staging/report/{reportId}`
4. Các endpoint update/delete detail và `PUT /production-report-staging/{id}` phải cho phép khi report ở `DRAFT` và phải kiểm tra scope.
5. `PUT /production-report-staging/{id}/return-to-draft` phải cho TEAM_LEADER có quyền quản lý report ở `CHANGE_REQUESTED`.
6. `GET /production-reports/{id}/details` phải kiểm tra scope của báo cáo chính thức.

## Phần còn phụ thuộc backend

- Form tạo báo cáo vẫn phải lấy danh sách nhà máy ban đầu từ `/factories/all`, vì Auth response hiện không có team/scope và chưa có endpoint entry context. Nên bổ sung `GET /production-report-staging/entry-context` trả về đúng factories/departments/lines/teams/employees/machines được phép, sau đó bỏ lần dùng `/all` còn lại này.
- Trang máy hiện suy ra máy từ các staging report trong scope. Muốn theo dõi đầy đủ máy chưa từng có báo cáo và trạng thái thời gian thực, cần `GET /machines/my-scope`.
- `stagingDetailsInScope` hiện gọi một request detail cho mỗi report. Với dữ liệu lớn nên bổ sung endpoint bulk/search theo scope cho downtime, quality, material và employee actual để tránh N+1 request.
- Backend phải cấp quyền duyệt cho FACTORY_MANAGER/PRODUCTION_MANAGER đúng phạm vi; chỉ thêm endpoint mà vẫn giới hạn `ADMIN/DIRECTOR` sẽ khiến Approval tiếp tục 403.
- Các trang HR đã nối `schedules`, `attendance`, `leave-requests`, `kpis`, `overtime-requests`, `assignments` và API tạo `notifications`. Backend hiện chưa có `GET /hr/notifications`, vì vậy màn hình quản lý thông báo chỉ hiển thị các phản hồi tạo thành công trong phiên. Nên bổ sung endpoint GET có phân trang và lọc scope.
- HR form hiện dùng ID nhân viên/tổ/ca vì backend chưa có endpoint lookup nhân sự theo scope dành cho manager. Nên bổ sung `GET /hr/context` hoặc `GET /hr/employees` để thay ID bằng dropdown nhưng vẫn tránh gọi `/employees/all` toàn hệ thống.
- Employee Portal đã dùng cả `GET` và `POST /employee-portal/overtime-requests` cho lịch sử và đăng ký tăng ca.
- Khi deploy BrowserRouter, web server phải fallback mọi `/workspace/*` về `index.html`.
- Các module bảo trì, Excel và AI không thuộc gói sửa này vì chưa có API backend tương ứng.
