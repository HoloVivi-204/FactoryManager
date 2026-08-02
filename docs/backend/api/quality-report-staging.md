# QualityReportStaging API

Lưu từng loại lỗi sản phẩm phát sinh trong một báo cáo sản xuất tạm.

Base URL: `http://localhost:8080/factory-management/api/v1/quality-report-staging`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo lỗi chi tiết | POST | `/quality-report-staging` |
| Danh sách hoạt động | GET | `/quality-report-staging/all` |
| Lấy theo ID | GET | `/quality-report-staging/{id}` |
| Lọc theo báo cáo | GET | `/quality-report-staging/report/{reportId}` |
| Lọc theo loại lỗi | GET | `/quality-report-staging/error-type/{errorTypeId}` |
| Cập nhật từng trường | PUT | `/quality-report-staging/{id}` |
| Xóa mềm | DELETE | `/quality-report-staging/{id}` |

## Khóa chống trùng

```text
production_report_staging_id + quality_error_type_id
```

Một loại lỗi chỉ xuất hiện một lần trong cùng báo cáo. Muốn tăng số lượng thì
cập nhật bản ghi hiện có.

## Tạo lỗi chi tiết

```http
POST http://localhost:8080/factory-management/api/v1/quality-report-staging
Content-Type: application/json
```

```json
{
  "productionReportStagingId": 1,
  "qualityErrorTypeId": 1,
  "quantity": 5,
  "description": "Phát hiện 5 sản phẩm lỗi hàn tại công đoạn kiểm tra cuối",
  "active": true
}
```

Response chứa code, tên và severity của loại lỗi.

## Lấy và lọc

```http
GET /factory-management/api/v1/quality-report-staging/all
GET /factory-management/api/v1/quality-report-staging/1
GET /factory-management/api/v1/quality-report-staging/report/1
GET /factory-management/api/v1/quality-report-staging/error-type/1
```

## Cập nhật số lượng

```http
PUT /factory-management/api/v1/quality-report-staging/1
Content-Type: application/json
```

```json
{
  "quantity": 7
}
```

## Xóa mềm và khôi phục

```http
DELETE /factory-management/api/v1/quality-report-staging/1
```

```http
PUT /factory-management/api/v1/quality-report-staging/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

## Kiểm tra tổng lỗi

Nếu báo cáo cha có `defectQuantity=15`, chi tiết sau hợp lệ:

```text
Lỗi hàn:       5
Sai kích thước: 3
Trầy xước:      7
Tổng:          15
```

Không thể tạo/cập nhật/khôi phục chi tiết làm tổng active vượt 15. Đồng thời
không thể giảm `defectQuantity` của báo cáo cha xuống dưới tổng chi tiết.

## Quy tắc khác

- Báo cáo cha và QualityErrorType phải tồn tại.
- `quantity` phải lớn hơn 0.
- Không được tạo, cập nhật, xóa mềm hoặc khôi phục nếu báo cáo đã `APPROVED`
  hoặc `LOCKED`.
- Bản ghi inactive vẫn giữ unique; muốn sử dụng lại cùng loại lỗi thì khôi phục
  và cập nhật bản ghi cũ.
