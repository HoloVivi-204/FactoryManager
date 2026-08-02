# FactoryManager Backend

Backend là module Spring Boot REST API của FactoryManager.

## Vị trí trong repo

```text
FactoryManager/
└── apps/
    └── backend/
        ├── src/
        ├── pom.xml
        ├── mvnw
        └── README.md
```

Tài liệu cấp dự án:

- [Root README](../../README.md)
- [PRD](../../docs/prd.md)
- [TDD](../../docs/technical-design.md)
- [Architecture](../../docs/architecture.md)
- [API contracts](../../docs/api-contracts.md)
- [Must traceability](../../docs/release/must-traceability.md)

Tài liệu cạnh backend:

- [API reference](../../docs/backend/api/)
- [Test data](../../docs/backend/test-data.md)
- [Excel template](../../docs/backend/examples/staging-report-template.xlsx)
- [Excel date-range sample](../../docs/backend/examples/staging-report-date-range-sample.xlsx)

## Stack

- Java 17
- Spring Boot 4.1
- Spring MVC, Validation, Security và OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL
- Apache POI cho Excel
- Maven Wrapper

## Chạy local

```powershell
cd apps/backend

$env:DB_URL = 'jdbc:postgresql://localhost:5432/factorymanagement'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<local-password>'
$env:JWT_SIGNER_KEY = '<random-secret-at-least-64-bytes>'
$env:ADMIN_BOOTSTRAP_ENABLED = 'false'

.\mvnw.cmd spring-boot:run
```

Base URL:

```text
http://localhost:8080/factory-management/api/v1
```

Ứng dụng không có fallback an toàn cho production secret. Khi chạy shared/staging/production, bắt buộc cấp secret qua biến môi trường, không ghi vào source.

## Kiểm tra

```powershell
cd apps/backend
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Test profile dùng cấu hình synthetic trong `src/test/resources`.

## Module hiện có

- `common`: response envelope, error handling, security, config.
- `modules/auth`: authentication, user, role, data scope.
- `modules/masterdata`: factory, department, line, team, employee, machine, material và danh mục hỗ trợ.
- `modules/production`: staging report, import Excel, approve production report, planning.
- `modules/dashboard`: dashboard tổng quan hiện tại.
- `modules/audit`: audit event hiện tại.
- `modules/ai`, `modules/finance`, `modules/hr`, `modules/maintenance`, `modules/inventory`: capability mở rộng, chưa mặc định là Must của MVP.

## Giới hạn đã biết

- Approve một staging record chưa phải closure theo production date/scope.
- Chưa có correction workspace và official version history.
- KPI/OEE đang tính trực tiếp, chưa theo KPI Dictionary có version.
- Dashboard đọc production report trực tiếp, chưa dùng KPI snapshot.
- Migration hiện dùng `schema.sql` cùng Hibernate `ddl-auto=update`.
- Audit và notification chưa bao phủ đủ workflow lõi.
