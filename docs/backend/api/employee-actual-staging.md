# EmployeeActualStaging API

Lưu nhân viên thực tế tham gia trong một báo cáo ca tạm.

Base URL: `http://localhost:8080/factory-management/api/v1/employee-actual-staging`

| Chức năng | Method | URL |
| --- | --- | --- |
| Thêm nhân sự | POST | `/employee-actual-staging` |
| Danh sách hoạt động | GET | `/employee-actual-staging/all` |
| Lấy theo ID | GET | `/employee-actual-staging/{id}` |
| Lọc theo báo cáo | GET | `/employee-actual-staging/report/{reportId}` |
| Lọc theo nhân viên | GET | `/employee-actual-staging/employee/{employeeId}` |
| Cập nhật từng trường | PUT | `/employee-actual-staging/{id}` |
| Xóa mềm | DELETE | `/employee-actual-staging/{id}` |

## Attendance status

```text
PRESENT      = Có mặt
ABSENT       = Vắng mặt
LATE         = Đi muộn
LEAVE_EARLY  = Về sớm
ON_LEAVE     = Nghỉ phép
```

## Assignment type

```text
NORMAL       = Làm việc đúng tổ
TRANSFERRED  = Điều chuyển tạm thời
SUPPORT      = Hỗ trợ từ tổ khác
OVERTIME     = Làm tăng ca
```

## Thêm nhân viên thực tế

```http
POST http://localhost:8080/factory-management/api/v1/employee-actual-staging
Content-Type: application/json
```

```json
{
  "productionReportStagingId": 1,
  "employeeId": 1,
  "workingMinutes": 480,
  "overtimeMinutes": 60,
  "attendanceStatus": "PRESENT",
  "assignmentType": "OVERTIME",
  "description": "Nhân viên làm thêm 1 giờ để hoàn thành kế hoạch",
  "active": true
}
```

Response chứa cả Team gốc của Employee để nhận biết người hỗ trợ hoặc điều
chuyển từ tổ khác.

## Nhân viên vắng mặt

```json
{
  "productionReportStagingId": 1,
  "employeeId": 2,
  "workingMinutes": 0,
  "overtimeMinutes": 0,
  "attendanceStatus": "ABSENT",
  "assignmentType": "NORMAL",
  "description": "Vắng đột xuất",
  "active": true
}
```

## Lấy và lọc

```http
GET /factory-management/api/v1/employee-actual-staging/all
GET /factory-management/api/v1/employee-actual-staging/1
GET /factory-management/api/v1/employee-actual-staging/report/1
GET /factory-management/api/v1/employee-actual-staging/employee/1
```

## Cập nhật từng trường

```http
PUT /factory-management/api/v1/employee-actual-staging/1
Content-Type: application/json
```

```json
{
  "overtimeMinutes": 90,
  "assignmentType": "OVERTIME"
}
```

## Xóa mềm và khôi phục

```http
DELETE /factory-management/api/v1/employee-actual-staging/1
```

```http
PUT /factory-management/api/v1/employee-actual-staging/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

## Quy tắc

- Báo cáo và Employee phải tồn tại; Employee cùng toàn bộ cây tổ chức phải
  đang active.
- `workingMinutes` và `overtimeMinutes` không được âm.
- Employee `ABSENT` bắt buộc có `workingMinutes=0`.
- Một Employee chỉ xuất hiện một lần trong cùng report, kể cả bản ghi inactive.
- Employee từ Team khác được phép tham gia và nên dùng `TRANSFERRED` hoặc
  `SUPPORT` để phản ánh đúng nghiệp vụ.
- Không được tạo, cập nhật, chuyển báo cáo, xóa mềm hoặc khôi phục nếu báo cáo
  cũ hoặc mới đã `APPROVED`/`LOCKED`.
