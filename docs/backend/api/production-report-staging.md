# ProductionReportStaging API

Bảng lưu báo cáo ca tạm trước khi quản lý phê duyệt và chốt thành dữ liệu chính
thức. Mỗi báo cáo thuộc một máy cụ thể.

Base URL: `http://localhost:8080/factory-management/api/v1/production-report-staging`

## Khóa chống trùng

```text
report_date + shift_id + team_id + machine_id
```

## Tạo báo cáo nháp

```http
POST http://localhost:8080/factory-management/api/v1/production-report-staging
Content-Type: application/json
```

```json
{
  "reportDate": "2026-07-12",
  "shiftId": 1,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 1,
  "teamId": 1,
  "leaderEmployeeId": 1,
  "machineId": 1,
  "plannedQuantity": 1200,
  "actualQuantity": 1165,
  "defectQuantity": 15,
  "workingMinutes": 480,
  "downtimeMinutes": 35,
  "note": "Máy dừng 35 phút để thay dao"
}
```

Hệ thống tự đặt `status=DRAFT` và tính:

```text
goodQuantity = actualQuantity - defectQuantity = 1150
```

Không gửi `goodQuantity` hoặc `status` trong request tạo.

## API đọc và lọc

```http
GET /factory-management/api/v1/production-report-staging/all
GET /factory-management/api/v1/production-report-staging/1
GET /factory-management/api/v1/production-report-staging/date/2026-07-12
GET /factory-management/api/v1/production-report-staging/team/1
GET /factory-management/api/v1/production-report-staging/machine/1
GET /factory-management/api/v1/production-report-staging/status/DRAFT
```

## Cập nhật báo cáo DRAFT

Chỉ báo cáo `DRAFT` được cập nhật. Trường không gửi được giữ nguyên và
`goodQuantity` được tính lại.

```http
PUT /factory-management/api/v1/production-report-staging/1
Content-Type: application/json
```

```json
{
  "actualQuantity": 1170,
  "defectQuantity": 10,
  "downtimeMinutes": 30
}
```

## Luồng trạng thái

```text
DRAFT --submit--> SUBMITTED --approve--> APPROVED --lock--> LOCKED
                          |
                          +--request-change--> CHANGE_REQUESTED
                                                |
                                                +--return-to-draft--> DRAFT
```

```http
PUT /factory-management/api/v1/production-report-staging/1/submit
PUT /factory-management/api/v1/production-report-staging/1/request-change
PUT /factory-management/api/v1/production-report-staging/1/return-to-draft
POST /factory-management/api/v1/production-report-staging/1/approve
PUT /factory-management/api/v1/production-report-staging/1/lock
```

Approve yêu cầu người duyệt và sẽ tự sinh ProductionReport chính thức:

```json
{
  "approvedByEmployeeId": 10,
  "remark": "Báo cáo hợp lệ, cho phép chốt số liệu"
}
```

## Quy tắc nghiệp vụ

- Tất cả danh mục liên quan phải tồn tại và đang hoạt động khi tạo/cập nhật.
- Department phải thuộc Factory; ProductionLine thuộc Department; Team thuộc
  ProductionLine; leader và Machine phải thuộc Team.
- Số lượng và số phút không được âm.
- `defectQuantity <= actualQuantity`.
- `downtimeMinutes <= workingMinutes`.
- Không có API DELETE vì báo cáo trong quy trình duyệt cần giữ dấu vết; LOCKED
  không được chỉnh sửa thông thường.
- Các bảng MachineDowntimeStaging, QualityReportStaging, MaterialIssueStaging
  và EmployeeActualStaging sau này sẽ tham chiếu ID của bảng này.
