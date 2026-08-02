# MachineType API

Base URL: `http://localhost:8080/factory-management/api/v1/machine-types`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo loại máy | POST | `/machine-types` |
| Danh sách đang hoạt động | GET | `/machine-types/all` |
| Lấy theo ID | GET | `/machine-types/{id}` |
| Cập nhật từng trường | PUT | `/machine-types/{id}` |
| Xóa mềm | DELETE | `/machine-types/{id}` |

## Tạo loại máy

```http
POST http://localhost:8080/factory-management/api/v1/machine-types
Content-Type: application/json
```

```json
{
  "code": "CNC",
  "name": "Máy CNC",
  "description": "Máy gia công điều khiển số",
  "active": true
}
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "code": "CNC",
    "name": "Máy CNC",
    "description": "Máy gia công điều khiển số",
    "active": true
  }
}
```

## Lấy danh sách và lấy theo ID

```http
GET http://localhost:8080/factory-management/api/v1/machine-types/all
GET http://localhost:8080/factory-management/api/v1/machine-types/1
```

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/machine-types/1
Content-Type: application/json
```

```json
{
  "name": "Máy CNC 5 trục"
}
```

Xóa nội dung mô tả bằng `{ "description": "" }`.

## Xóa mềm và khôi phục

```http
DELETE http://localhost:8080/factory-management/api/v1/machine-types/1
```

```http
PUT http://localhost:8080/factory-management/api/v1/machine-types/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

Mã loại máy là duy nhất toàn hệ thống, được trim và chuyển thành chữ hoa.
