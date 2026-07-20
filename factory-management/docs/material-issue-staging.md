# MaterialIssueStaging API

Lưu các sự cố nguyên vật liệu phát sinh trong một báo cáo sản xuất tạm.

Base URL: `http://localhost:8080/factory-management/api/v1/material-issue-staging`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo sự cố | POST | `/material-issue-staging` |
| Danh sách hoạt động | GET | `/material-issue-staging/all` |
| Lấy theo ID | GET | `/material-issue-staging/{id}` |
| Lọc theo báo cáo | GET | `/material-issue-staging/report/{reportId}` |
| Lọc theo vật liệu | GET | `/material-issue-staging/material/{materialId}` |
| Lọc theo loại | GET | `/material-issue-staging/type/{issueType}` |
| Cập nhật từng trường | PUT | `/material-issue-staging/{id}` |
| Xóa mềm | DELETE | `/material-issue-staging/{id}` |

## Issue type

```text
SHORTAGE             = Thiếu vật tư
LATE_DELIVERY        = Giao chậm
WRONG_SPECIFICATION  = Sai quy cách
DAMAGED              = Hư hỏng
QUALITY_FAILED       = Không đạt chất lượng
OTHER                = Khác
```

## Tạo sự cố vật tư

```http
POST http://localhost:8080/factory-management/api/v1/material-issue-staging
Content-Type: application/json
```

```json
{
  "productionReportStagingId": 1,
  "materialId": 1,
  "issueType": "SHORTAGE",
  "quantity": 120.5,
  "unit": "KG",
  "description": "Thiếu thép tấm cho công đoạn gia công buổi sáng",
  "active": true
}
```

`quantity` được lưu với tối đa 3 chữ số thập phân. `unit` là snapshot tại thời
điểm ghi nhận nên được lưu trực tiếp trong bản ghi sự cố.

## Lấy và lọc

```http
GET /factory-management/api/v1/material-issue-staging/all
GET /factory-management/api/v1/material-issue-staging/1
GET /factory-management/api/v1/material-issue-staging/report/1
GET /factory-management/api/v1/material-issue-staging/material/1
GET /factory-management/api/v1/material-issue-staging/type/SHORTAGE
```

## Cập nhật từng trường

```http
PUT /factory-management/api/v1/material-issue-staging/1
Content-Type: application/json
```

```json
{
  "quantity": 100.25,
  "description": "Đã bổ sung một phần vật tư"
}
```

## Xóa mềm và khôi phục

```http
DELETE /factory-management/api/v1/material-issue-staging/1
```

```http
PUT /factory-management/api/v1/material-issue-staging/1
Content-Type: application/json
```

```json
{
  "active": true
}
```

## Quy tắc

- Báo cáo cha và Material phải tồn tại; Material phải đang active.
- `issueType` bắt buộc và phải là một giá trị enum hợp lệ.
- `quantity > 0`; `unit` bắt buộc.
- Cho phép nhiều sự cố cùng Material và cùng loại trong một báo cáo.
- Không được tạo, cập nhật, chuyển báo cáo, xóa mềm hoặc khôi phục nếu báo cáo
  cũ hoặc mới đã `APPROVED`/`LOCKED`.
