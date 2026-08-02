# QualityErrorType API

Danh mục chuẩn hóa loại lỗi sản phẩm để dashboard thống kê chính xác theo loại
lỗi, Team, ProductionLine và thời gian trong các bước tiếp theo.

Base URL: `http://localhost:8080/factory-management/api/v1/quality-error-types`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo loại lỗi | POST | `/quality-error-types` |
| Danh sách hoạt động | GET | `/quality-error-types/all` |
| Lọc theo severity | GET | `/quality-error-types/severity/{severity}` |
| Lấy theo ID | GET | `/quality-error-types/{id}` |
| Cập nhật từng trường | PUT | `/quality-error-types/{id}` |
| Xóa mềm | DELETE | `/quality-error-types/{id}` |

## Severity

```text
LOW      = lỗi nhẹ
MEDIUM   = lỗi trung bình
HIGH     = lỗi nghiêm trọng
CRITICAL = lỗi đặc biệt nghiêm trọng
```

## Tạo loại lỗi hàn

```http
POST http://localhost:8080/factory-management/api/v1/quality-error-types
Content-Type: application/json
```

```json
{
  "code": "WELD_ERROR",
  "name": "Lỗi hàn",
  "description": "Mối hàn không đạt tiêu chuẩn kỹ thuật",
  "severity": "HIGH",
  "active": true
}
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "code": "WELD_ERROR",
    "name": "Lỗi hàn",
    "description": "Mối hàn không đạt tiêu chuẩn kỹ thuật",
    "severity": "HIGH",
    "active": true
  }
}
```

## Lấy và lọc danh mục

```http
GET http://localhost:8080/factory-management/api/v1/quality-error-types/all
GET http://localhost:8080/factory-management/api/v1/quality-error-types/1
GET http://localhost:8080/factory-management/api/v1/quality-error-types/severity/HIGH
GET http://localhost:8080/factory-management/api/v1/quality-error-types/severity/CRITICAL
```

Severity trong JSON và URL phải viết hoa.

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/quality-error-types/1
Content-Type: application/json
```

```json
{
  "severity": "CRITICAL"
}
```

## Xóa mềm và khôi phục

```http
DELETE http://localhost:8080/factory-management/api/v1/quality-error-types/1
```

```http
PUT http://localhost:8080/factory-management/api/v1/quality-error-types/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

Mã loại lỗi là duy nhất toàn hệ thống. Xóa mềm chỉ ngừng cho chọn trong dữ liệu
mới; các bản ghi chất lượng lịch sử sau này vẫn có thể tham chiếu loại lỗi cũ.
