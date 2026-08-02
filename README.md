# FactoryManager

FactoryManager là hệ thống quản lý dữ liệu hiệu suất nhà máy. Luồng mục tiêu là:

```text
nhập tay / import
→ staging + validation
→ chốt theo phạm vi
→ KPI snapshot
→ dashboard + drill-down + audit
```

Repo này đang ở trạng thái phát triển, chưa phải MVP hoàn chỉnh hoặc production-ready. Code hiện có đã có backend Spring Boot, frontend React, authentication/RBAC, master data, staging/import Excel và dashboard cơ bản. Các phần closure/correction/versioning, KPI Dictionary, snapshot và một số acceptance Must vẫn là gap.

## Cấu trúc repository

```text
FactoryManager/
├── apps/
│   ├── backend/        # Spring Boot REST API
│   └── frontend/       # React + TypeScript + Vite
├── docs/
│   ├── README.md       # mục lục tài liệu
│   ├── prd.md
│   ├── technical-design.md
│   ├── architecture.md
│   ├── api-contracts.md
│   ├── decisions.md
│   ├── backlog.md
│   ├── feature-matrix.csv
│   ├── backend/
│   ├── release/
│   └── ui/
├── content/
│   ├── skeleton.yaml
│   └── briefs/
└── README.md
```

Quy ước:

- `apps/backend` là module backend.
- `apps/frontend` là module frontend.
- `docs` chứa tài liệu sản phẩm, kỹ thuật, runbook và evidence.
- `content` chứa skeleton/nội dung nghiệp vụ phục vụ mô tả.
- Root chỉ giữ file điều hướng và cấu hình repository.

Tên thư mục app là tên vai trò trong repo; runtime context path của backend vẫn giữ `/factory-management`.

## Tài liệu chính

- [Mục lục tài liệu](docs/README.md)
- [Mô tả dự án](docs/product-overview.md)
- [PRD](docs/prd.md)
- [TDD](docs/technical-design.md)
- [Kiến trúc](docs/architecture.md)
- [Hợp đồng API](docs/api-contracts.md)
- [Quyết định](docs/decisions.md)
- [Backlog](docs/backlog.md)
- [Feature matrix](docs/feature-matrix.csv)
- [Runbook demo](docs/release/demo-runbook.md)
- [Trạng thái release](docs/release/final-status.md)

PRD/TDD mô tả trạng thái đích. Code, config và test mô tả trạng thái hiện tại. Nếu hai phía lệch nhau, coi đó là gap, không tự suy ra code đã đạt requirement.

## Stack hiện tại

- Backend: Java 17, Spring Boot 4.1, Spring Security, JPA, PostgreSQL, Apache POI.
- Frontend: React 19, TypeScript 5.8, Vite 6, React Router, Recharts.
- API base mặc định: `http://localhost:8080/factory-management/api/v1`.

## Chạy local

### Backend

```powershell
cd apps/backend

$env:DB_URL = 'jdbc:postgresql://localhost:5432/factorymanagement'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<local-password>'
$env:JWT_SIGNER_KEY = '<random-secret-at-least-64-bytes>'
$env:ADMIN_BOOTSTRAP_ENABLED = 'false'

.\mvnw.cmd spring-boot:run
```

Backend chạy ở:

```text
http://localhost:8080/factory-management
```

Không commit `.env`, secret, credential hoặc dữ liệu production thật.

### Frontend

```powershell
cd apps/frontend
pnpm.cmd install
$env:VITE_API_URL = 'http://localhost:8080/factory-management/api/v1'
pnpm.cmd dev -- --host localhost --port 5173 --strictPort
```

Frontend chạy ở:

```text
http://localhost:5173
```

## Kiểm tra

Backend:

```powershell
cd apps/backend
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Frontend:

```powershell
cd apps/frontend
pnpm.cmd run lint
pnpm.cmd run build
```

## Giới hạn quan trọng hiện tại

- Import Excel chưa hỗ trợ đúng partial success 95/5 và conflict workflow.
- Chưa có closure/correction/versioning theo PRD.
- KPI còn tính trực tiếp trong service, chưa theo KPI Dictionary có version.
- Dashboard chưa đọc từ KPI snapshot và chưa có drill-down contributor đầy đủ.
- Một số module như finance, maintenance, chatbot và KPI cá nhân nằm ngoài Must hoặc cần capability riêng.
- Migration, test isolation, security hardening và observability chưa đạt release gate.

Thứ tự khắc phục được theo dõi trong [backlog](docs/backlog.md).
