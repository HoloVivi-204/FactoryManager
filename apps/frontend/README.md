# FactoryManager Frontend

Frontend là module React của FactoryManager. UI hiện có login, workspace theo role, master data admin, nhập/duyệt báo cáo sản xuất, dashboard cơ bản và một số module mở rộng ngoài MVP.

## Vị trí trong repo

```text
FactoryManager/
└── apps/
    └── frontend/
        ├── src/
        ├── package.json
        ├── pnpm-lock.yaml
        └── README.md
```

Tài liệu liên quan:

- [Root README](../../README.md)
- [PRD](../../docs/prd.md)
- [Design system](../../docs/ui/design-system.md)
- [Screen map](../../docs/ui/screen-map.md)
- [Test guide](../../docs/test-guide.md)
- [Final status](../../docs/release/final-status.md)

## Stack

- React 19
- TypeScript 5.8
- Vite 6
- React Router 7
- Recharts
- ESLint 9
- pnpm

## Chạy local

```powershell
cd apps/frontend
pnpm.cmd install
$env:VITE_API_URL = 'http://localhost:8080/factory-management/api/v1'
pnpm.cmd dev -- --host localhost --port 5173 --strictPort
```

Nếu không đặt `VITE_API_URL`, app dùng URL trên làm mặc định. Backend hiện allow CORS từ `http://localhost:5173` và `http://127.0.0.1:5173`.

## Kiểm tra

```powershell
cd apps/frontend
pnpm.cmd run lint
pnpm.cmd run build
```

Repo chưa có script test frontend. Lint và production build là hai gate frontend hiện có.

## Cấu trúc source

```text
src/
├── app/          # app shell, layout, route composition
├── features/     # feature modules theo nghiệp vụ/UI flow
├── shared/       # API client, UI primitive, type dùng chung
└── styles/       # global styles
```

## Lưu ý contract

- Base URL lấy từ `VITE_API_URL`.
- Bearer token được gắn vào request authenticated.
- Client hiện đọc envelope `{ code, message, result }`.
- HTTP 401 xóa phiên local và phát sự kiện unauthorized.
- HTTP 403 hiển thị lỗi quyền.
- Upload Excel dùng `multipart/form-data`, giới hạn backend hiện tại là 10 MB.
- `ReportsPage` hiện gọi `GET /production-reports/search/my-scope`, nhưng backend chưa có route này; không dùng trang đó làm evidence demo cho tới khi consumer và controller được đồng bộ.

Không thay response envelope chỉ ở frontend. Mọi thay đổi contract phải được ghi trong [API contracts](../../docs/api-contracts.md) và triển khai có compatibility plan.
