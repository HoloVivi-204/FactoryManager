# Tài liệu FactoryManager

Thư mục này chứa tài liệu dự án. Root repository chỉ giữ README điều hướng; các tài liệu chi tiết nằm trong `docs/`.

## Đọc theo nhu cầu

| Nhu cầu | Đọc theo thứ tự |
|---|---|
| Hiểu sản phẩm | [product-overview.md](product-overview.md) → [prd.md](prd.md) |
| Hiểu thiết kế kỹ thuật | [technical-design.md](technical-design.md) → [decisions.md](decisions.md) → [architecture.md](architecture.md) |
| Làm backend hoặc API | [api-contracts.md](api-contracts.md) → [backend/api/](backend/api/) |
| Làm frontend | [ui/screen-map.md](ui/screen-map.md) → [ui/design-system.md](ui/design-system.md) |
| Chọn việc tiếp theo | [decisions.md](decisions.md) → [backlog.md](backlog.md) |
| Chuẩn bị test/demo | [test-guide.md](test-guide.md) → [release/demo-runbook.md](release/demo-runbook.md) |
| Đánh giá release | [release/must-traceability.md](release/must-traceability.md) → [release/final-status.md](release/final-status.md) |
| Triển khai | [release/deployment-runbook.md](release/deployment-runbook.md) |

## Product / scope

- [product-overview.md](product-overview.md)
- [prd.md](prd.md)
- [backlog.md](backlog.md)
- [feature-matrix.csv](feature-matrix.csv)
- [../content/skeleton.yaml](../content/skeleton.yaml)
- [../content/briefs/kpi-content-brief.md](../content/briefs/kpi-content-brief.md)

## Architecture / decisions

- [architecture.md](architecture.md)
- [technical-design.md](technical-design.md)
- [api-contracts.md](api-contracts.md)
- [decisions.md](decisions.md)
- [exec-plans.md](exec-plans.md)

## Backend reference

- [backend/api/](backend/api/)
- [backend/test-data.md](backend/test-data.md)
- [backend/examples/staging-report-template.xlsx](backend/examples/staging-report-template.xlsx)
- [backend/examples/staging-report-date-range-sample.xlsx](backend/examples/staging-report-date-range-sample.xlsx)

## UI / release

- [ui/design-system.md](ui/design-system.md)
- [ui/screen-map.md](ui/screen-map.md)
- [release/demo-runbook.md](release/demo-runbook.md)
- [release/deployment-runbook.md](release/deployment-runbook.md)
- [release/must-traceability.md](release/must-traceability.md)
- [release/final-status.md](release/final-status.md)

## Thứ tự authority

1. PRD quyết định phạm vi, hành vi người dùng, business rule và acceptance.
2. TDD quyết định kiến trúc kỹ thuật khi không trái PRD.
3. `decisions.md`, KPI/API contract, schema/migration và fixture đã duyệt cụ thể hóa PRD/TDD.
4. Code, test và migration hiện hành là evidence trạng thái implementation.
5. README, runbook và release evidence hỗ trợ điều hướng, không thay thế PRD/TDD.

Khi tài liệu và code lệch nhau, ghi nhận gap thay vì tự coi code hiện tại là đúng.
