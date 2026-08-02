# Machine API

Machine là thực thể trung tâm để các module Downtime, bảo trì, sản lượng, lỗi,
OEE và dashboard tham chiếu trong các bước tiếp theo.

Base URL: `http://localhost:8080/factory-management/api/v1/machines`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo máy | POST | `/machines` |
| Danh sách hoạt động | GET | `/machines/all` |
| Lấy theo ID | GET | `/machines/{id}` |
| Lọc theo Team | GET | `/machines/team/{teamId}` |
| Lọc theo loại máy | GET | `/machines/machine-type/{machineTypeId}` |
| Lọc theo trạng thái | GET | `/machines/status/{status}` |
| Cập nhật từng trường | PUT | `/machines/{id}` |
| Xóa mềm | DELETE | `/machines/{id}` |

## Trạng thái vận hành

```text
IDLE
RUNNING
STOPPED
MAINTENANCE
BREAKDOWN
```

## Tạo máy

Giả sử đã có `machineTypeId=1` và `teamId=1`:

```http
POST http://localhost:8080/factory-management/api/v1/machines
Content-Type: application/json
```

```json
{
  "machineTypeId": 1,
  "teamId": 1,
  "code": "MCH001",
  "name": "Máy CNC số 1",
  "serialNumber": "SN-CNC-001",
  "status": "RUNNING",
  "installationDate": "2023-02-15",
  "description": "Máy CNC chính của tổ CNC số 1",
  "active": true
}
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "code": "MCH001",
    "name": "Máy CNC số 1",
    "serialNumber": "SN-CNC-001",
    "installationDate": "2023-02-15",
    "description": "Máy CNC chính của tổ CNC số 1",
    "status": "RUNNING",
    "active": true,
    "machineTypeId": 1,
    "machineTypeCode": "CNC",
    "machineTypeName": "Máy CNC",
    "teamId": 1,
    "teamCode": "TEAM-A",
    "teamName": "Team A",
    "productionLineId": 1,
    "productionLineCode": "LINE-A",
    "departmentId": 1,
    "departmentCode": "PRODUCTION",
    "factoryId": 1,
    "factoryCode": "BD"
  }
}
```

Nếu không gửi `status`, máy mới mặc định là `IDLE`.

## Lấy và lọc máy

```http
GET http://localhost:8080/factory-management/api/v1/machines/all
GET http://localhost:8080/factory-management/api/v1/machines/1
GET http://localhost:8080/factory-management/api/v1/machines/team/1
GET http://localhost:8080/factory-management/api/v1/machines/machine-type/1
GET http://localhost:8080/factory-management/api/v1/machines/status/RUNNING
```

Giá trị trạng thái trong URL phải viết hoa đúng theo danh sách enum.

## Cập nhật trạng thái vận hành

```http
PUT http://localhost:8080/factory-management/api/v1/machines/1
Content-Type: application/json
```

```json
{
  "status": "RUNNING"
}
```

## Chuyển máy sang Team khác

```json
{
  "teamId": 2
}
```

## Thay đổi loại máy

```json
{
  "machineTypeId": 2
}
```

## Xóa mềm và khôi phục

```http
DELETE http://localhost:8080/factory-management/api/v1/machines/1
```

```http
PUT http://localhost:8080/factory-management/api/v1/machines/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

Mã máy là duy nhất toàn hệ thống. Máy chỉ được tạo hoặc khôi phục khi
MachineType, Team, ProductionLine, Department và Factory liên quan đều đang
hoạt động. `serialNumber` không bắt buộc nhưng phải duy nhất nếu được cung cấp.
`installationDate` không được ở tương lai.
