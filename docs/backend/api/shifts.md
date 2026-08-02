# Shift API

Base URL: `http://localhost:8080/factory-management/api/v1/shifts`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/shifts` |
| Danh sách hoạt động | GET | `/shifts/all` |
| Lấy theo ID | GET | `/shifts/{id}` |
| Cập nhật từng trường | PUT | `/shifts/{id}` |
| Xóa mềm | DELETE | `/shifts/{id}` |

## Tạo ca sáng

```json
{
  "code": "SHIFT-MORNING",
  "name": "Ca sáng",
  "startTime": "06:00",
  "endTime": "14:00",
  "active": true
}
```

## Tạo ca đêm

```json
{
  "code": "SHIFT-NIGHT",
  "name": "Ca đêm",
  "startTime": "22:00",
  "endTime": "06:00",
  "active": true
}
```

Thời gian dùng định dạng `HH:mm`. Ca qua ngày được chấp nhận, nhưng giờ bắt
đầu và kết thúc không được trùng nhau. Mã Shift là duy nhất toàn hệ thống.

Khôi phục Shift đã xóa mềm:

```json
{
  "active": true
}
```
