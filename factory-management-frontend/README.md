# FactoryManager Frontend

Ứng dụng React cho FactoryManager. Frontend hiện có login, workspace theo role, master data admin, nhập/duyệt
báo cáo sản xuất, dashboard cơ bản và một số module mở rộng ngoài MVP. Route UI `ReportsPage` có tồn tại
nhưng chức năng liệt kê báo cáo official theo scope đang bị hỏng do lệch API.

Tài liệu sản phẩm và UI:

- [Root README](../README.md)
- [PRD](../PRD_He_thong_hieu_suat_nha_may.md)
- [Design System](../DESIGN_SYSTEM.md)
- [Screen Map](../docs/ui/SCREEN_MAP.md)
- [Hướng dẫn test](../HUONG_DAN_TEST.md)

## Stack

- React 19
- TypeScript 5.8
- Vite 6
- React Router 7
- Recharts
- ESLint 9
- pnpm

## Cài và chạy

```powershell
pnpm.cmd install
$env:VITE_API_URL = 'http://localhost:8080/factory-management/api/v1'
pnpm.cmd dev -- --host localhost --port 5173 --strictPort
```

Nếu không đặt `VITE_API_URL`, ứng dụng dùng URL trên làm mặc định.
Backend hiện chỉ allow CORS từ `http://localhost:5173` và `http://127.0.0.1:5173`. Nếu port `5173` đang bận,
`--strictPort` phải dừng startup; giải phóng port rồi chạy lại, không chuyển sang port Vite tự chọn.

## Kiểm tra

```powershell
pnpm.cmd run lint
pnpm.cmd run build
```

Repository chưa có script test frontend. Production build và lint là hai gate hiện có; xem
[Final status](../docs/release/FINAL_STATUS.md) để biết kết quả gần nhất.

## Routing hiện tại

Ứng dụng dùng route:

```text
/workspace/:roleSlug/:pageKey
```

Khi chưa đăng nhập, mọi route render `LoginPage`. Khi đã đăng nhập:

- Role và page key được kiểm tra trước khi render workspace.
- Navigation chỉ hiển thị page được khai báo cho role hiện tại.
- Backend vẫn phải kiểm tra role/data scope; redirect hoặc ẩn menu không phải security control.

Chi tiết current/target route nằm trong [Screen Map](../docs/ui/SCREEN_MAP.md).

## API client

- Base URL lấy từ `VITE_API_URL`.
- Bearer token được gắn vào request authenticated.
- Client hiện đọc envelope `{ code, message, result }`.
- HTTP 401 xóa phiên local và phát sự kiện unauthorized.
- HTTP 403 hiển thị lỗi quyền.
- Upload Excel dùng `multipart/form-data`, giới hạn backend hiện tại là 10 MB.
- `ReportsPage` hiện gọi `GET /production-reports/search/my-scope`, nhưng backend chưa có route này; không dùng
  trang đó làm bằng chứng demo hoặc acceptance cho tới khi consumer và controller được đồng bộ.

Không thay response envelope chỉ ở frontend. Mọi thay đổi contract phải được ghi trong
[API Contracts](../API_Contracts.md) và triển khai có compatibility plan.

## Trạng thái UI

Current UI hỗ trợ:

- Loading/authentication state.
- Empty và error state ở nhiều table/panel.
- Toast cho kết quả mutation.
- Responsive grid ở các breakpoint 1000 px và 650 px.

Gap đáng chú ý:

- Mobile đang ẩn sidebar nhưng chưa có navigation thay thế.
- CSS còn nhiều block lặp và chưa dùng semantic token.
- Một số page trong navigation là placeholder hoặc nằm ngoài MVP.
- Chưa có accessibility test, component test hoặc E2E test.
- Dashboard chưa biểu diễn đầy đủ coverage, freshness, Partial, Adjusted và Stale.
- `RoleOverviewPage` có thể bỏ qua lỗi summary sau loading, còn formatter hiện đổi dữ liệu thiếu thành `0`/
  `0.00%`; phải xác nhận request API thành công trước khi diễn giải số zero là dữ liệu thật.

Component mới phải tuân thủ [Design System](../DESIGN_SYSTEM.md), đặc biệt quy tắc “missing khác zero” và
state contract loading/empty/error/partial/stale.
