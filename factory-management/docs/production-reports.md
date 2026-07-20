# ProductionReport API

ProductionReport là dữ liệu sản xuất chính thức dành cho Dashboard, KPI và
OEE. Không được tạo, sửa hoặc xóa trực tiếp qua API.

## Luồng sinh dữ liệu

```text
ProductionReportStaging DRAFT
→ SUBMITTED
→ POST approve
→ ProductionReport được tạo
→ staging chuyển APPROVED
```

## Approve và tạo báo cáo chính thức

```http
POST http://localhost:8080/factory-management/api/v1/production-report-staging/1/approve
Content-Type: application/json
```

```json
{
  "approvedByEmployeeId": 10,
  "remark": "Hoàn thành kế hoạch ca sáng"
}
```

Điều kiện: staging phải ở trạng thái `SUBMITTED` và người duyệt phải là
Employee đang hoạt động.

Backend tự sinh report number:

```text
PR202607120001
PR202607120002
```

Mỗi staging chỉ sinh được đúng một ProductionReport.

## Công thức KPI

```text
Availability = (workingMinutes - downtimeMinutes) / workingMinutes × 100
Performance  = actualQuantity / plannedQuantity × 100
Quality      = goodQuantity / actualQuantity × 100
OEE          = Availability × Performance × Quality / 10000
```

Nếu mẫu số bằng 0, chỉ số tương ứng bằng 0. Các tỷ lệ lưu với hai chữ số thập
phân.

## API chỉ đọc

Base URL: `http://localhost:8080/factory-management/api/v1/production-reports`

```http
GET /factory-management/api/v1/production-reports/all
GET /factory-management/api/v1/production-reports/1
GET /factory-management/api/v1/production-reports/report-no/PR202607120001
GET /factory-management/api/v1/production-reports/date/2026-07-12
GET /factory-management/api/v1/production-reports/team/1
GET /factory-management/api/v1/production-reports/machine/1
GET /factory-management/api/v1/production-reports/dashboard
```

## Tìm kiếm nhiều điều kiện

Tất cả query parameter đều không bắt buộc và có thể kết hợp:

```http
GET /factory-management/api/v1/production-reports/search?fromDate=2026-07-01&toDate=2026-07-12&factoryId=1&teamId=1&machineId=1
```

Chỉ tìm theo khoảng ngày:

```http
GET /factory-management/api/v1/production-reports/search?fromDate=2026-07-01&toDate=2026-07-12
```

Chỉ tìm theo Team và Machine:

```http
GET /factory-management/api/v1/production-reports/search?teamId=1&machineId=1
```

Quy tắc:

- `fromDate` và `toDate` dùng định dạng `yyyy-MM-dd`.
- Có thể chỉ gửi một đầu của khoảng ngày.
- Nếu không gửi tham số nào, API trả toàn bộ báo cáo.
- `fromDate` không được lớn hơn `toDate`.
- Kết quả sắp xếp theo ngày báo cáo và ID giảm dần.

Không có:

```text
POST   /production-reports
PUT    /production-reports/{id}
DELETE /production-reports/{id}
```

## Dashboard response

```json
{
  "code": 1000,
  "result": {
    "reportCount": 10,
    "plannedQuantity": 12000,
    "actualQuantity": 11650,
    "goodQuantity": 11500,
    "defectQuantity": 150,
    "downtimeMinutes": 350,
    "averageAvailability": 92.71,
    "averagePerformance": 97.08,
    "averageQuality": 98.71,
    "averageOee": 88.96
  }
}
```

Dashboard hiện tổng hợp toàn bộ ProductionReport. Bộ lọc dashboard theo khoảng
ngày/Factory/Team sẽ được bổ sung khi xây dựng phần báo cáo quản trị.

## Tính bất biến

- ProductionReport lưu snapshot khóa ngoại và số liệu tại thời điểm duyệt.
- `createdBy` là leader của staging; `approvedBy` lấy từ request duyệt.
- Không cập nhật trực tiếp. Sai sót sau chốt phải xử lý bằng Adjustment hoặc
  quy trình mở khóa có phân quyền trong bước sau.
- Các bảng MachineDowntime, QualityReport, MaterialIssue và EmployeeActual
  chính thức chưa được tạo/copy ở bước này; hiện tại ưu tiên hoàn thiện luồng
  staging → approve → ProductionReport.
