# Employee API

Base URL: `http://localhost:8080/factory-management/api/v1/employees`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/employees` |
| Danh sách hoạt động | GET | `/employees/all` |
| Lọc theo Team | GET | `/employees/team/{teamId}` |
| Lấy theo ID | GET | `/employees/{id}` |
| Cập nhật từng trường | PUT | `/employees/{id}` |
| Xóa mềm | DELETE | `/employees/{id}` |

## Tạo Employee

```http
POST http://localhost:8080/factory-management/api/v1/employees
```

```json
{
  "teamId": 1,
  "code": "EMP001",
  "fullName": "Nguyễn Văn An",
  "position": "Công nhân vận hành",
  "hireDate": "2026-07-01",
  "active": true
}
```

Mã Employee là duy nhất toàn hệ thống. Ngày vào làm không được ở tương lai.

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/employees/1
```

```json
{
  "position": "Tổ trưởng sản xuất"
}
```

Chuyển Employee sang Team khác:

```json
{
  "teamId": 2
}
```

Khôi phục Employee đã xóa mềm:

```json
{
  "active": true
}
```

Nếu Employee đang là leader rồi bị xóa mềm hoặc chuyển Team, liên kết leader
cũ sẽ tự động được gỡ.
