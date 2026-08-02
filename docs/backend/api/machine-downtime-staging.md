# MachineDowntimeStaging API

Lưu từng lần dừng máy chi tiết thuộc một `ProductionReportStaging`.

Base URL: `http://localhost:8080/factory-management/api/v1/machine-downtime-staging`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo lần dừng | POST | `/machine-downtime-staging` |
| Danh sách hoạt động | GET | `/machine-downtime-staging/all` |
| Lấy theo ID | GET | `/machine-downtime-staging/{id}` |
| Lọc theo báo cáo | GET | `/machine-downtime-staging/report/{reportId}` |
| Lọc theo máy | GET | `/machine-downtime-staging/machine/{machineId}` |
| Cập nhật từng trường | PUT | `/machine-downtime-staging/{id}` |
| Xóa mềm | DELETE | `/machine-downtime-staging/{id}` |

## Tạo dữ liệu dừng máy

```http
POST http://localhost:8080/factory-management/api/v1/machine-downtime-staging
Content-Type: application/json
```

```json
{
  "productionReportStagingId": 1,
  "machineId": 1,
  "downtimeReasonId": 1,
  "startTime": "2026-07-12T08:15:00",
  "endTime": "2026-07-12T08:45:00",
  "description": "Máy dừng do lỗi trục chính",
  "active": true
}
```

Không gửi `durationMinutes`. Backend tự tính:

```text
durationMinutes = endTime - startTime = 30
```

Response mẫu:

```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "productionReportStagingId": 1,
    "reportStatus": "DRAFT",
    "machineId": 1,
    "machineCode": "CNC-001",
    "machineName": "Máy CNC số 01",
    "downtimeReasonId": 1,
    "downtimeReasonCode": "MACHINE_BREAKDOWN",
    "downtimeReasonName": "Hỏng máy",
    "downtimeReasonType": "UNPLANNED",
    "startTime": "2026-07-12T08:15:00",
    "endTime": "2026-07-12T08:45:00",
    "durationMinutes": 30,
    "description": "Máy dừng do lỗi trục chính",
    "active": true
  }
}
```

## Lấy và lọc dữ liệu

```http
GET /factory-management/api/v1/machine-downtime-staging/all
GET /factory-management/api/v1/machine-downtime-staging/1
GET /factory-management/api/v1/machine-downtime-staging/report/1
GET /factory-management/api/v1/machine-downtime-staging/machine/1
```

## Cập nhật thời gian

```http
PUT /factory-management/api/v1/machine-downtime-staging/1
Content-Type: application/json
```

```json
{
  "endTime": "2026-07-12T09:00:00"
}
```

Backend tự tính lại `durationMinutes`.

## Xóa mềm và khôi phục

```http
DELETE /factory-management/api/v1/machine-downtime-staging/1
```

```http
PUT /factory-management/api/v1/machine-downtime-staging/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

## Quy tắc

- Báo cáo, máy và lý do dừng phải tồn tại.
- Máy phải đúng bằng máy của báo cáo cha.
- `endTime` phải sau `startTime` ít nhất một phút.
- Không được tạo, cập nhật, xóa mềm hoặc khôi phục nếu báo cáo đã `APPROVED`
  hoặc `LOCKED`.
- `durationMinutes` do backend tính, không nhận từ request.
