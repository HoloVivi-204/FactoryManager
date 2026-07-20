# Factory Management

REST API quản lý cơ cấu và nhân sự nhà máy bằng Spring Boot.

## Cấu trúc quản lý

```text
Factory
└── Department
    └── ProductionLine
        └── Team
            └── Employee
```

`Shift` và `MachineType` hiện là các danh mục độc lập.

## Tài liệu

- [Cấu hình dự án](docs/configuration.md)
- [Factory API](docs/factories.md)
- [Department API](docs/departments.md)
- [ProductionLine API](docs/production-lines.md)
- [Team API và leader](docs/teams.md)
- [Employee API](docs/employees.md)
- [Shift API](docs/shifts.md)
- [MachineType API](docs/machine-types.md)
- [Machine API](docs/machines.md)
- [DowntimeReason API](docs/downtime-reasons.md)
- [QualityErrorType API](docs/quality-error-types.md)
- [Material API](docs/materials.md)
- [ProductionReportStaging API](docs/production-report-staging.md)
- [MachineDowntimeStaging API](docs/machine-downtime-staging.md)
- [QualityReportStaging API](docs/quality-report-staging.md)
- [MaterialIssueStaging API](docs/material-issue-staging.md)
- [EmployeeActualStaging API](docs/employee-actual-staging.md)
- [ProductionReport API và Dashboard](docs/production-reports.md)
- [Chatbot AI có kiểm soát quyền dữ liệu](docs/ai-chatbot.md)

## Base URL

```text
http://localhost:8080/factory-management/api/v1
```

## Quy ước chung

- Create yêu cầu đầy đủ các trường bắt buộc.
- Update hỗ trợ gửi từng trường; trường không gửi sẽ giữ nguyên.
- DELETE thực hiện xóa mềm bằng `active=false`.
- Có thể khôi phục dữ liệu đã xóa mềm bằng update `{ "active": true }`.
- Các mã `code` được trim và chuẩn hóa thành chữ hoa.
