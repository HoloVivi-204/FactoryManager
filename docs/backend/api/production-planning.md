# Kế hoạch và lệnh sản xuất

Mô-đun này quản lý ba lớp dữ liệu trước khi phát sinh báo cáo sản xuất:

```text
Product -> ProductionPlan -> ProductionOrder -> ProductionReportStaging (tích hợp sau)
```

## 1. Sản phẩm (`product`)

Các trường chính: `code`, `name`, `unit`, `standardCycleSeconds`, `active`.
Mã sản phẩm là duy nhất toàn hệ thống. Xóa qua API là xóa mềm (`active = false`).

API:

```text
GET    /api/v1/products?keyword=&active=&page=0&size=50
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

JSON mẫu:

```json
{
  "code": "SP-CNC-001",
  "name": "Chi tiết trục CNC",
  "unit": "PCS",
  "standardCycleSeconds": 42.5,
  "active": true
}
```

## 2. Kế hoạch sản xuất (`production_plan`)

Kế hoạch thuộc một nhà máy, một dây chuyền và một sản phẩm. Backend tự sinh
`planNo`. Trạng thái đi theo đúng thứ tự:

```text
DRAFT -> APPROVED -> CLOSED
```

- Chỉ kế hoạch `DRAFT` được sửa hoặc xóa mềm.
- Không thể thay cơ cấu/kỳ/sản phẩm nếu kế hoạch đã có lệnh sản xuất.
- Không thể giảm sản lượng kế hoạch xuống thấp hơn tổng sản lượng đã phân bổ.
- Chỉ đóng kế hoạch khi mọi lệnh còn hoạt động đã `COMPLETED` hoặc `CANCELLED`.

API:

```text
GET    /api/v1/production-plans
GET    /api/v1/production-plans/{id}
POST   /api/v1/production-plans
PUT    /api/v1/production-plans/{id}
POST   /api/v1/production-plans/{id}/approve
POST   /api/v1/production-plans/{id}/close
DELETE /api/v1/production-plans/{id}
```

JSON mẫu:

```json
{
  "periodStart": "2026-07-15",
  "periodEnd": "2026-07-21",
  "factoryId": 1,
  "productionLineId": 1,
  "productId": 1,
  "plannedQuantity": 12000,
  "note": "Kế hoạch tuần 29"
}
```

## 3. Lệnh sản xuất (`production_order`)

Lệnh chỉ được tạo từ kế hoạch `APPROVED`. Tổ phải thuộc dây chuyền của kế hoạch,
máy phải thuộc đúng tổ, thời gian phải nằm trong kỳ kế hoạch và tổng sản lượng
các lệnh không được vượt sản lượng kế hoạch.

```text
DRAFT -> RELEASED -> IN_PROGRESS -> COMPLETED
   |          |             |
   +----------+-------------+-> CANCELLED
```

API:

```text
GET    /api/v1/production-orders
GET    /api/v1/production-orders/{id}
POST   /api/v1/production-orders
PUT    /api/v1/production-orders/{id}
PUT    /api/v1/production-orders/{id}/status
DELETE /api/v1/production-orders/{id}
```

JSON tạo lệnh:

```json
{
  "productionPlanId": 1,
  "teamId": 1,
  "machineId": 1,
  "shiftId": 1,
  "scheduledStart": "2026-07-15",
  "scheduledEnd": "2026-07-16",
  "plannedQuantity": 1800,
  "note": "Ưu tiên đơn hàng tháng 7"
}
```

JSON đổi trạng thái:

```json
{
  "status": "RELEASED"
}
```

## Phân quyền và phạm vi dữ liệu

- `ADMIN`, `FACTORY_MANAGER`, `PRODUCTION_MANAGER`: tạo, sửa, duyệt/chuyển trạng thái và xóa mềm.
- `DIRECTOR`, `DEPARTMENT_MANAGER`, `TEAM_LEADER`: chỉ đọc.
- Danh sách kế hoạch/lệnh được giới hạn theo `UserDataScope` và cơ cấu tổ mà người dùng được phép xem.
- Tất cả thao tác thay đổi đều ghi vào `audit_event`; các bảng có `@Version` để phát hiện cập nhật đồng thời.

Hiện tại lệnh sản xuất chưa tự gắn vào `production_report_staging`; liên kết đó nên được bổ sung khi chốt thiết kế nhập báo cáo theo lệnh.
