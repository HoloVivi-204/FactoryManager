# FactoryManager Backend

Spring Boot REST API cho repository FactoryManager. Backend hiện cung cấp authentication/RBAC, master data,
staging báo cáo sản xuất, import Excel, production report/dashboard cơ bản và một số module mở rộng.

PRD/TDD của hệ thống hiệu suất nhà máy nằm ở thư mục gốc repository:

- [PRD](../PRD_He_thong_hieu_suat_nha_may.md)
- [TDD](../TDD_He_thong_hieu_suat_nha_may.md)
- [Architecture](../ARCHITECTURE.md)
- [API Contracts](../API_Contracts.md)
- [Must traceability](../docs/release/RELEASE_MUST_TRACEABILITY.md)

Tài liệu trên mô tả cả trạng thái đích và gap. Không suy ra backend đã đạt MVP chỉ từ việc endpoint hiện hữu.

## Stack

- Java 17
- Spring Boot 4.1
- Spring MVC, Validation, Security và OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL
- Apache POI cho Excel
- Maven Wrapper

## Chạy local

PostgreSQL phải sẵn sàng trước khi khởi động. Cung cấp cấu hình bằng biến môi trường, không commit `.env`.

```powershell
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

Ứng dụng không có fallback cho `JWT_SIGNER_KEY` hoặc `ADMIN_PASSWORD`. Nếu thiếu `JWT_SIGNER_KEY` đủ dài,
backend dừng khởi động để tránh phát token bằng secret mặc định.

Nếu database local chưa có tài khoản, bật `ADMIN_BOOTSTRAP_ENABLED=true` đúng lần khởi tạo đầu tiên và cung
cấp `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_EMPLOYEE_CODE` bằng biến môi trường riêng. Sau đó đổi mật khẩu
và tắt bootstrap.

## Kiểm tra

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Test profile hiện dùng H2 in-memory và JWT signer key synthetic riêng trong `src/test/resources`.
Không cần PostgreSQL local cho `contextLoads`.

## Cấu trúc nghiệp vụ hiện tại

```text
Factory
└── Department
    └── ProductionLine
        └── Team
            ├── Employee
            └── Machine
```

`Shift`, `MachineType`, `DowntimeReason`, `QualityErrorType` và `Material` là các danh mục hỗ trợ.
Data model hiện chưa có `companyId` xuyên suốt như PRD yêu cầu.

## Tài liệu cạnh code

### Nền tảng và bảo mật

- [Configuration](docs/configuration.md)
- [Authentication](docs/authentication.md)
- [Authorization](docs/authorization.md)
- [Database migrations hiện tại](docs/database-migrations.md)

### Master data

- [Factories](docs/factories.md)
- [Departments](docs/departments.md)
- [Production lines](docs/production-lines.md)
- [Teams](docs/teams.md)
- [Employees](docs/employees.md)
- [Shifts](docs/shifts.md)
- [Machine types](docs/machine-types.md)
- [Machines](docs/machines.md)
- [Downtime reasons](docs/downtime-reasons.md)
- [Quality error types](docs/quality-error-types.md)
- [Materials](docs/materials.md)

### Production data

- [Production report staging](docs/production-report-staging.md)
- [Machine downtime staging](docs/machine-downtime-staging.md)
- [Quality report staging](docs/quality-report-staging.md)
- [Material issue staging](docs/material-issue-staging.md)
- [Employee actual staging](docs/employee-actual-staging.md)
- [Staging report Excel](docs/staging-report-excel.md)
- [Production reports và dashboard](docs/production-reports.md)
- [Production planning](docs/production-planning.md)

Các trang trên mô tả implementation hiện tại. Khi khác code/controller, code là nguồn chuẩn cho hiện trạng;
khi khác PRD/TDD, khác biệt phải được ghi là gap.

### Capability ngoài MVP PRD

- [Finance](docs/finance.md)
- [AI chatbot](docs/ai-chatbot.md)

HR, inventory, finance, maintenance, chatbot và KPI cá nhân không thuộc MVP PRD hiệu suất nhà máy.
Không ưu tiên mở rộng chúng trước các Must về import partial, validation/conflict, closure/correction và KPI
snapshot.

## Quy ước response hiện tại

Backend hiện trả envelope:

```json
{
  "code": 1000,
  "message": "Success",
  "result": {}
}
```

TDD đề xuất contract `success/data/error` khác với implementation. Xem
[API Contracts](../API_Contracts.md) trước khi thay đổi để có kế hoạch compatibility rõ ràng.

## Giới hạn đã biết

- Import Excel dừng toàn batch khi có row error, chưa đạt scenario 95/5.
- Approve một staging record chưa phải closure theo production date/scope.
- Chưa có correction workspace và official version history.
- KPI/OEE đang tính trực tiếp, chưa theo KPI Dictionary có version.
- Dashboard đọc production report trực tiếp, chưa dùng KPI snapshot.
- Migration hiện dùng `schema.sql` cùng Hibernate `ddl-auto=update`.
- Audit và notification chưa bao phủ đủ workflow lõi.
