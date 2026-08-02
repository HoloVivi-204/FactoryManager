# Demo Runbook FactoryManager

## 1. Phạm vi demo

Runbook này khởi động và trình diễn implementation hiện tại:

```text
login → master data → staging report + details → submit/request change/approve
→ official report được tạo → dashboard cơ bản
```

Đây không phải demo closure/correction/KPI snapshot đầy đủ theo PRD. Không dùng từ “MVP hoàn chỉnh” trong
phần trình bày.

Tên `Team Leader`, `Production Manager`, `Factory Manager` và workspace tương ứng trong runbook là
**Current/legacy** vì code chưa migration. Khi nói về Target, dùng ba cấp
`Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`; Ca trưởng/Tổ trưởng chỉ là chức danh
nhân sự và navigation dùng các module chuẩn ở PRD 4.4.

`ReportsPage` không thuộc happy-path demo hiện tại: frontend gọi route scoped search chưa tồn tại ở backend.

## 2. Điều kiện máy chạy demo

- Java 17.
- PostgreSQL có database local dành riêng cho demo.
- Node.js tương thích toolchain hiện tại và pnpm.
- Port 8080 cho backend; frontend bắt buộc dùng `http://localhost:5173` theo CORS hiện tại.
- Không dùng dữ liệu hoặc credential production.

## 3. Biến môi trường

Backend, trong terminal PowerShell:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/factorymanagement'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<demo-db-password>'
$env:JWT_SIGNER_KEY = '<random-demo-secret-at-least-64-bytes>'
$env:ADMIN_BOOTSTRAP_ENABLED = 'false'
```

Nếu database demo rỗng, bootstrap đúng một lần:

```powershell
$env:ADMIN_BOOTSTRAP_ENABLED = 'true'
$env:ADMIN_USERNAME = '<demo-admin-username>'
$env:ADMIN_PASSWORD = '<unique-strong-demo-password>'
$env:ADMIN_EMPLOYEE_CODE = '<demo-admin-employee-code>'
```

Khởi động một lần, đăng nhập/đổi mật khẩu, dừng app và đặt lại
`ADMIN_BOOTSTRAP_ENABLED=false`. Không dùng fallback credential trong source.

Frontend:

```powershell
$env:VITE_API_URL = 'http://localhost:8080/factory-management/api/v1'
```

AI để tắt trừ khi demo riêng capability ngoài MVP:

```powershell
$env:AI_ENABLED = 'false'
```

## 4. Preflight kỹ thuật

Từ repository root:

```powershell
cd factory-management
.\mvnw.cmd -DskipTests package
```

Trong frontend:

```powershell
cd ..\factory-management-frontend
pnpm.cmd install
pnpm.cmd run build
```

Gate hiện biết:

- Backend package có thể build khi skip test.
- `mvnw.cmd test` cần PostgreSQL sẵn sàng vì chưa có test profile tự cô lập.
- Frontend lint hiện còn lỗi; phải ghi vào final status, không bỏ qua trong release sign-off.

## 5. Khởi động

Terminal backend:

```powershell
cd factory-management
.\mvnw.cmd spring-boot:run
```

Kiểm tra backend trả HTTP response tại:

```text
http://localhost:8080/factory-management/api/v1
```

Terminal frontend:

```powershell
cd ..\factory-management-frontend
pnpm.cmd dev -- --host localhost --port 5173 --strictPort
```

Mở `http://localhost:5173`. Nếu port này đang bận, `--strictPort` phải dừng startup; giải phóng port rồi chạy
lại. Không dùng port Vite tự chọn khác vì backend chỉ allow CORS cho port `5173`.

## 6. Chuẩn bị dữ liệu

Chuẩn bị các account test riêng:

- Admin để quản lý master data.
- Team Leader Current gắn Team A.
- Production hoặc Factory Manager Current có quyền duyệt Team A.

Chuẩn bị:

- Factory → Department → Production Line → Team A.
- Shift.
- Machine Type → Machine thuộc Team A.
- Downtime Reason và Quality Error Type.
- Mã dữ liệu có prefix demo và ngày chạy để tránh đụng business key.

Không reset hoặc xóa database bằng command tự động trước demo. Nếu cần môi trường sạch, khôi phục từ
snapshot demo đã được kiểm tra.

## 7. Browser story

### Story A — Master data và scope

1. Login bằng Admin.
2. Mở danh mục Factory/Line/Team/Machine.
3. Chỉ ra quan hệ máy thuộc team.
4. Mở User/Role/Data Scope nếu dữ liệu demo đã chuẩn bị.

Thông điệp: backend có nền tảng RBAC/data scope, nhưng company context/effective dating target còn thiếu.

### Story B — Nhập báo cáo ca

1. Login Team Leader.
2. Mở `Nhập báo cáo ca`.
3. Chọn production date, shift, team, machine.
4. Nhập planned, actual, defect, working/downtime.
5. Thêm downtime và quality details.
6. Lưu Draft rồi Submit.

Thông điệp: staging tách khỏi official; validation hiện chủ yếu là rule số và quan hệ.

### Story C — Review

1. Login manager.
2. Mở hàng đợi approval.
3. Request change một lần để minh họa feedback.
4. Team Leader sửa/submit lại.
5. Manager approve.

Thông điệp bắt buộc: approve hiện tạo official cho từng record, chưa phải close theo ngày/scope của PRD.

### Story D — Dashboard

1. Mở dashboard/tổng quan trong đúng scope.
2. Xác nhận request `/production-reports/dashboard/my-scope` thành công.
3. Chỉ ra production quantities và KPI hiện tại.

Thông điệp bắt buộc: KPI hiện hardcode và dashboard aggregate trực tiếp; KPI Dictionary/snapshot/
completeness/freshness là target chưa hoàn tất.

Không mở `ReportsPage` trong happy path. Trang này đang gọi
`GET /production-reports/search/my-scope`, nhưng backend chỉ có `/production-reports/search` cho một số role
và không có scoped search tương ứng. Nếu request dashboard lỗi hoặc thiếu dữ liệu, current UI có thể hiển thị
`0`; không trình bày số đó như dữ liệu nghiệp vụ.

### Story E — Import gap minh bạch

Nếu cần trình bày import:

1. Tải Excel template.
2. Preview file toàn bộ hợp lệ.
3. Không gọi scenario 95/5 là đã hỗ trợ.
4. Giải thích row error hiện làm cả import không persist; traceability ghi Partial.

## 8. Smoke check sau story

- Team Leader không thấy dữ liệu ngoài scope bằng UI thông thường.
- Manager approve thành công; chỉ đối chiếu report count/quantity sau khi request dashboard thành công.
- Dashboard không hiển thị Draft.
- Không mở hoặc mô tả `ReportsPage` như capability đang hoạt động.
- Mutation lỗi có message an toàn và request ID trong log/response.
- Refresh frontend không mất auth bất thường.

Data-scope update sang team khác là gap bảo mật đã biết; không trình diễn bằng dữ liệu nhạy cảm.

## 9. Khôi phục khi demo lỗi

| Triệu chứng | Kiểm tra |
|---|---|
| Frontend báo không kết nối backend | Backend terminal, port 8080, `VITE_API_URL` |
| Frontend không start vì port 5173 bận | Giải phóng port 5173; không bỏ `--strictPort` hoặc chuyển port |
| Backend không start | PostgreSQL, DB URL/user/password, startup script error |
| 401 | Token hết hạn, account disabled/locked, signer key đổi giữa lần chạy |
| 403 | Role/data scope và target record |
| Duplicate staging | Dùng ngày/shift/team/machine khác hoặc record Draft đã chuẩn bị |
| Import lỗi | Đúng `.xlsx`, kích thước ≤ 10 MB, sheet/header/mã master |
| Reports hiển thị lỗi | Known blocker `/search/my-scope`; không đưa trang này vào happy-path demo |
| Dashboard trống hoặc toàn `0` | Kiểm tra response API trước; missing/error hiện có thể bị formatter đổi thành zero |

Không sửa DB thủ công trong lúc trình diễn. Nếu mất trạng thái tin cậy, dừng story và ghi `Blocked`.

## 10. Kết thúc

- Dừng frontend/backend bằng `Ctrl+C`.
- Xóa biến môi trường khỏi terminal bằng cách đóng terminal demo.
- Không commit `.env`, log, screenshot có token hoặc file Excel chứa dữ liệu thật.
- Cập nhật [FINAL_STATUS](FINAL_STATUS.md) nếu kết quả preflight khác snapshot hiện tại.
