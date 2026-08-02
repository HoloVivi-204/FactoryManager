# DowntimeReason API

Danh mục này chuẩn hóa lý do dừng máy để dữ liệu Downtime, Pareto và dashboard
không bị tách nhóm vì khác cách nhập chữ.

Base URL: `http://localhost:8080/factory-management/api/v1/downtime-reasons`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo lý do | POST | `/downtime-reasons` |
| Danh sách đang hoạt động | GET | `/downtime-reasons/all` |
| Lọc theo loại lý do | GET | `/downtime-reasons/type/{reasonType}` |
| Lấy theo ID | GET | `/downtime-reasons/{id}` |
| Cập nhật từng trường | PUT | `/downtime-reasons/{id}` |
| Xóa mềm | DELETE | `/downtime-reasons/{id}` |

## Tạo lý do dừng máy

```http
POST http://localhost:8080/factory-management/api/v1/downtime-reasons
Content-Type: application/json
```

```json
{
  "code": "MACHINE_BREAKDOWN",
  "name": "Hỏng máy",
  "description": "Máy dừng do sự cố hoặc hư hỏng thiết bị",
  "reasonType": "UNPLANNED",
  "active": true
}
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "code": "MACHINE_BREAKDOWN",
    "name": "Hỏng máy",
    "description": "Máy dừng do sự cố hoặc hư hỏng thiết bị",
    "reasonType": "UNPLANNED",
    "active": true
  }
}
```

## Dữ liệu gợi ý

```json
[
  { "code": "POWER_OUTAGE", "name": "Mất điện", "reasonType": "UNPLANNED" },
  { "code": "MATERIAL_SHORTAGE", "name": "Thiếu nguyên vật liệu", "reasonType": "UNPLANNED" },
  { "code": "WAITING_OPERATOR", "name": "Chờ nhân công", "reasonType": "UNPLANNED" },
  { "code": "PLANNED_MAINTENANCE", "name": "Bảo trì định kỳ", "reasonType": "PLANNED" },
  { "code": "WAITING_QC", "name": "Chờ kiểm tra chất lượng", "reasonType": "UNPLANNED" }
]
```

Mỗi phần tử cần được gửi riêng qua API `POST`; mảng trên chỉ là danh sách dữ
liệu gợi ý, endpoint hiện không phải API tạo hàng loạt.

## Lấy danh sách và lấy theo ID

```http
GET http://localhost:8080/factory-management/api/v1/downtime-reasons/all
GET http://localhost:8080/factory-management/api/v1/downtime-reasons/1
GET http://localhost:8080/factory-management/api/v1/downtime-reasons/type/PLANNED
GET http://localhost:8080/factory-management/api/v1/downtime-reasons/type/UNPLANNED
```

`PLANNED` là dừng có kế hoạch. `UNPLANNED` là dừng ngoài kế hoạch. Giá trị enum
trong JSON và URL phải viết hoa.

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/downtime-reasons/1
Content-Type: application/json
```

```json
{
  "name": "Hỏng hoặc lỗi máy"
}
```

Đổi riêng loại lý do:

```json
{
  "reasonType": "PLANNED"
}
```

## Xóa mềm và khôi phục

```http
DELETE http://localhost:8080/factory-management/api/v1/downtime-reasons/1
```

```http
PUT http://localhost:8080/factory-management/api/v1/downtime-reasons/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

Mã lý do là duy nhất toàn hệ thống và được chuẩn hóa thành chữ hoa. Xóa mềm
chỉ ngừng cho chọn lý do trong dữ liệu mới; bản ghi lịch sử sau này vẫn có thể
tham chiếu đến lý do cũ.
