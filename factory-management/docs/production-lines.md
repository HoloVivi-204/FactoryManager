# ProductionLine API

Base URL: `http://localhost:8080/factory-management/api/v1/production-lines`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/production-lines` |
| Danh sách hoạt động | GET | `/production-lines/all` |
| Lọc theo Department | GET | `/production-lines/department/{departmentId}` |
| Lấy theo ID | GET | `/production-lines/{id}` |
| Cập nhật từng trường | PUT | `/production-lines/{id}` |
| Xóa mềm | DELETE | `/production-lines/{id}` |

## Tạo ProductionLine

```json
{
  "code": "LINE001",
  "name": "Dây chuyền CNC số 1",
  "description": "Dây chuyền gia công sản phẩm bằng máy CNC",
  "departmentId": 1,
  "active": true
}
```

Mã ProductionLine là duy nhất trong cùng một Department.
