# Team API

Base URL: `http://localhost:8080/factory-management/api/v1/teams`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/teams` |
| Danh sách hoạt động | GET | `/teams/all` |
| Lọc theo ProductionLine | GET | `/teams/production-line/{productionLineId}` |
| Lấy theo ID | GET | `/teams/{id}` |
| Cập nhật từng trường | PUT | `/teams/{id}` |
| Xóa mềm | DELETE | `/teams/{id}` |
| Gán leader | PUT | `/teams/{id}/leader` |
| Gỡ leader | DELETE | `/teams/{id}/leader` |

## Tạo Team

```http
POST http://localhost:8080/factory-management/api/v1/teams
```

```json
{
  "code": "TEAM001",
  "name": "Tổ CNC số 1",
  "description": "Tổ phụ trách dây chuyền CNC số 1",
  "productionLineId": 1,
  "active": true
}
```

Mã Team là duy nhất trong cùng một ProductionLine.

## Cập nhật từng trường

```http
PUT http://localhost:8080/factory-management/api/v1/teams/1
```

```json
{
  "name": "Team A ca sáng"
}
```

Khôi phục Team đã xóa mềm:

```json
{
  "active": true
}
```

## Leader

Employee phải đang hoạt động và thuộc chính Team cần gán.

```http
PUT http://localhost:8080/factory-management/api/v1/teams/1/leader
```

```json
{
  "employeeId": 1
}
```

Gỡ leader:

```http
DELETE http://localhost:8080/factory-management/api/v1/teams/1/leader
```
