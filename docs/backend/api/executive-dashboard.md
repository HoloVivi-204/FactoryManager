# Dashboard điều hành

Dashboard này dành cho `ADMIN` và `DIRECTOR`, tập trung vào kết quả toàn công ty hoặc một nhà máy. API chỉ trả dữ liệu tổng hợp và **không trả danh tính máy đang hỏng hoặc nhân viên cụ thể**.

## API

```http
GET /api/v1/executive-dashboard
    ?fromDate=2026-07-01
    &toDate=2026-07-31
    &factoryId=1
Authorization: Bearer <access-token>
```

- `fromDate`, `toDate`: không bắt buộc. Mặc định là từ đầu tháng hiện tại đến hôm nay.
- `factoryId`: không bắt buộc. Không truyền thì xem toàn công ty; nếu truyền thì nhà máy phải đang hoạt động.
- Khoảng thời gian tối đa: 731 ngày.
- Khoảng tối đa 62 ngày trả xu hướng theo ngày; dài hơn trả theo tháng.

## Nguồn dữ liệu

- Doanh thu, chi phí, lợi nhuận và công nợ: chỉ lấy `financial_record.status = POSTED`.
- Sản lượng, năng suất, chất lượng và OEE: chỉ lấy `production_report` chính thức, không lấy staging.
- Năng suất: `goodQuantity / (workingMinutes - downtimeMinutes) * 60`.
- OEE, Availability và Performance được bình quân gia quyền theo thời gian làm việc.
- Bảo trì quá hạn chỉ trả tổng số lịch/phiếu quá hạn, không trả máy cụ thể.

## Cấu trúc phản hồi

```json
{
  "result": {
    "fromDate": "2026-07-01",
    "toDate": "2026-07-31",
    "asOfDate": "2026-07-15",
    "scopeType": "COMPANY",
    "factoryId": null,
    "scopeName": "Toàn công ty",
    "trendGranularity": "DAY",
    "kpis": {
      "financial": {
        "revenue": 2500000000.00,
        "expense": 1900000000.00,
        "profit": 600000000.00,
        "profitMarginPercent": 24.00,
        "overdueReceivable": 120000000.00,
        "overdueReceivableCount": 2,
        "overduePayable": 0.00,
        "overduePayableCount": 0,
        "postedRecordCount": 35
      },
      "production": {
        "officialReportCount": 40,
        "plannedQuantity": 50000,
        "actualQuantity": 47000,
        "goodQuantity": 46200,
        "defectQuantity": 800,
        "workingMinutes": 19200,
        "downtimeMinutes": 1200,
        "planAttainmentPercent": 94.00,
        "productivityPerHour": 154.00,
        "defectRatePercent": 1.70,
        "qualityPercent": 98.30,
        "availabilityPercent": 92.50,
        "performancePercent": 91.30,
        "oeePercent": 82.70
      }
    },
    "trends": [],
    "factories": [],
    "risks": [],
    "decisions": [],
    "generatedAt": "2026-07-15T16:00:00"
  }
}
```

## Ngưỡng cảnh báo mặc định

- Lợi nhuận âm.
- Có công nợ phải thu hoặc phải trả quá hạn.
- Hoàn thành kế hoạch dưới 90%.
- Tỷ lệ lỗi trên 3%.
- OEE dưới 75%.
- Có lịch hoặc phiếu bảo trì quá hạn.

Mỗi phần tử `risks` có mã, mức độ, chỉ số và ngưỡng. Phần `decisions` cung cấp hành động điều hành đề xuất tương ứng để frontend hiển thị thành danh sách “Điểm cần quyết định”.
