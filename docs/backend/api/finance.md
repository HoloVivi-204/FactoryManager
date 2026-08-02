# Phân hệ tài chính quản trị nhà máy

Phân hệ này phục vụ quản trị doanh thu, chi phí và lợi nhuận nội bộ. Đây không phải sổ cái kế toán và chưa bao gồm hóa đơn, thuế hay công nợ pháp lý.

## Phân quyền

- `ADMIN`, `FINANCE`: tạo và cập nhật bản ghi.
- `ADMIN`: ngừng sử dụng danh mục.
- `ADMIN`, `DIRECTOR`, `FINANCE`, `FACTORY_MANAGER`: xem dữ liệu trong phạm vi được cấp.
- Tài khoản cấp nhà máy/phòng ban/dây chuyền phải có `UserDataScope` phù hợp.

## Danh mục tài chính

```http
POST   /api/v1/financial-categories
GET    /api/v1/financial-categories/all
PUT    /api/v1/financial-categories/{id}
DELETE /api/v1/financial-categories/{id}
```

Ví dụ chi phí nguyên vật liệu:

```json
{
  "code": "MATERIAL_COST",
  "name": "Chi phí nguyên vật liệu",
  "entryType": "EXPENSE",
  "expenseGroup": "MATERIAL",
  "description": "Chi phí vật tư sử dụng cho sản xuất",
  "active": true
}
```

`entryType`: `REVENUE`, `EXPENSE`.

`expenseGroup`: `MATERIAL`, `LABOR`, `UTILITIES`, `MAINTENANCE`, `LOSS`, `COST_OF_GOODS_SOLD`, `OTHER`.

## Bản ghi tài chính

```http
POST   /api/v1/financial-records
PUT    /api/v1/financial-records/{id}
DELETE /api/v1/financial-records/{id}
GET    /api/v1/financial-records/search
GET    /api/v1/financial-records/dashboard
```

```json
{
  "recordDate": "2026-07-14",
  "categoryId": 1,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 1,
  "amount": 12500000.00,
  "paidAmount": 5000000.00,
  "dueDate": "2026-07-30",
  "counterparty": "Công ty cung cấp thép ABC",
  "referenceNo": "INV-2026-0714-01",
  "description": "Thép sử dụng cho Line A",
  "active": true
}
```

Tìm kiếm:

```http
GET /api/v1/financial-records/search?fromDate=2026-07-01&toDate=2026-07-31&factoryId=1&type=EXPENSE
```

Dashboard:

```http
GET /api/v1/financial-records/dashboard?fromDate=2026-07-01&toDate=2026-07-31&factoryId=1
```

Dashboard trả tổng doanh thu, tổng chi phí, lợi nhuận, phải thu, phải trả và chi phí theo từng nhóm.

## Kho

```http
POST   /api/v1/warehouses
GET    /api/v1/warehouses/all
PUT    /api/v1/warehouses/{id}
DELETE /api/v1/warehouses/{id}
```

```json
{
  "code": "WH-BD-01",
  "name": "Kho vật tư Bình Dương",
  "factoryId": 1,
  "description": "Kho nguyên vật liệu sản xuất",
  "active": true
}
```

## Nhập, xuất và điều chỉnh kho

```http
POST   /api/v1/inventory/transactions
GET    /api/v1/inventory/transactions
DELETE /api/v1/inventory/transactions/{id}
GET    /api/v1/inventory/stocks
```

Loại giao dịch:

- `INBOUND`: nhập kho.
- `OUTBOUND`: xuất kho.
- `ADJUSTMENT_IN`: điều chỉnh tăng.
- `ADJUSTMENT_OUT`: điều chỉnh giảm.

```json
{
  "transactionDate": "2026-07-14",
  "transactionType": "INBOUND",
  "warehouseId": 1,
  "materialId": 1,
  "quantity": 1200.5,
  "unitCost": 25000,
  "referenceNo": "PNK-2026-0001",
  "description": "Nhập thép cho kế hoạch tháng 7"
}
```

Tra cứu tồn kho:

```http
GET /api/v1/inventory/stocks?warehouseId=1
```

Hệ thống chặn xuất hoặc điều chỉnh giảm nếu số lượng lớn hơn tồn kho hiện tại.

## Trình tự nhập liệu dành cho kế toán

1. Admin hoặc kế toán tạo kho.
2. Admin tạo danh mục vật tư; kế toán tạo danh mục tài chính.
3. Kế toán ghi phiếu nhập/xuất trong `inventory/transactions`.
4. Chi phí hoặc doanh thu được ghi trong `financial-records` với đối tác, hạn thanh toán và số đã thanh toán.
5. Kế toán xem tồn kho tại `inventory/stocks`.
6. Kế toán xem doanh thu, chi phí, lợi nhuận và công nợ tại `financial-records/dashboard`.
