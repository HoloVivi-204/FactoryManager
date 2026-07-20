# Material API

Danh mục nguyên vật liệu dùng làm dữ liệu gốc cho tồn kho, nhập/xuất kho, BOM
và tiêu hao sản xuất trong các bước tiếp theo.

Base URL: `http://localhost:8080/factory-management/api/v1/materials`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo vật liệu | POST | `/materials` |
| Danh sách hoạt động | GET | `/materials/all` |
| Lấy theo ID | GET | `/materials/{id}` |
| Cập nhật từng trường | PUT | `/materials/{id}` |
| Xóa mềm | DELETE | `/materials/{id}` |

## Tạo nguyên vật liệu

```http
POST http://localhost:8080/factory-management/api/v1/materials
Content-Type: application/json
```

```json
{
  "code": "MAT001",
  "name": "Thép tấm",
  "unit": "KG",
  "description": "Thép tấm dùng cho công đoạn gia công CNC",
  "active": true
}
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "code": "STEEL-304",
    "name": "Thép không gỉ 304",
    "unit": "kg",
    "description": "Thép 304 dùng cho sản phẩm cơ khí",
    "active": true
  }
}
```

## Lấy danh sách và lấy theo ID

```http
GET http://localhost:8080/factory-management/api/v1/materials/all
GET http://localhost:8080/factory-management/api/v1/materials/1
```

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/materials/1
Content-Type: application/json
```

```json
{
  "unit": "tấn"
}
```

Xóa mô tả bằng `{ "description": "" }`.

## Xóa mềm và khôi phục

```http
DELETE http://localhost:8080/factory-management/api/v1/materials/1
```

```http
PUT http://localhost:8080/factory-management/api/v1/materials/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

Mã Material là duy nhất toàn hệ thống và được chuẩn hóa thành chữ hoa. `unit`
được lưu dạng chuỗi để hỗ trợ các đơn vị như `kg`, `m`, `m²`, `L`, `cái`,
`tấm`.
