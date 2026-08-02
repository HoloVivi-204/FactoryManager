# Nhập dữ liệu báo cáo ca từ Excel

> Phạm vi tài liệu: luồng Excel của implementation **Current/legacy**. `TEAM_LEADER` và đường dẫn
> “Tổ trưởng” bên dưới vẫn là tên đang chạy, không phải role/tab Target. Target cho Nhân viên vận hành
> có quyền nhập trong scope; Ca trưởng/Tổ trưởng chỉ là chức danh trong Quản lý nhân sự. Luồng Current
> cũng chưa có `businessDataCode`, phân lớp thời gian, cảnh báo gần trùng hoặc đối soát Excel–web–OCR.

Trong implementation Current, chức năng này dành cho tài khoản `TEAM_LEADER`/Tổ trưởng nhập dữ liệu
lịch sử vào các bảng staging. Dữ liệu sau khi nhập luôn ở trạng thái `DRAFT`; hệ thống không tự submit,
approve hoặc tạo báo cáo chính thức.

## Quy trình trên web

1. Mở trang **Tổ trưởng → Báo cáo trong ca**.
2. Bấm **Tải file Excel mẫu**. File mẫu chứa danh mục mã mà tài khoản được phép sử dụng.
3. Điền một hoặc nhiều sheet nghiệp vụ. Có thể chỉ điền `NHAN_SU` nếu báo cáo sản lượng tương ứng đã tồn tại ở trạng thái `DRAFT`.
4. Chọn file và bấm **1. Kiểm tra file**.
5. Sửa tất cả lỗi theo sheet, dòng và cột mà giao diện hiển thị.
6. Khi file hợp lệ, bấm **2. Nhập vào báo cáo DRAFT**.
7. Mở các báo cáo nháp vừa nhập để kiểm tra rồi gửi duyệt theo quy trình bình thường.

Giới hạn: file `.xlsx`, tối đa 10 MB và 10.000 dòng dữ liệu mỗi lần.

## Khóa liên kết báo cáo

Các sheet chi tiết liên kết với báo cáo cha bằng 4 trường:

```text
reportDate + shiftCode + teamCode + machineCode
```

Nếu chưa có báo cáo cha, phải thêm dòng tương ứng vào sheet `SAN_LUONG` trong cùng file. Nếu báo cáo cha đã tồn tại nhưng không còn là `DRAFT`, backend sẽ từ chối cập nhật.

## Sheet SAN_LUONG

| Cột | Bắt buộc | Ghi chú |
|---|---:|---|
| reportDate | Có | `yyyy-MM-dd`, được phép nhập ngày quá khứ, không cho ngày tương lai |
| shiftCode | Có | Lấy từ `DM_CA` |
| factoryCode | Có | Lấy từ `DM_TO_MAY` |
| departmentCode | Có | Phải thuộc đúng nhà máy |
| productionLineCode | Có | Phải thuộc đúng phòng ban |
| teamCode | Có | Phải thuộc phạm vi của tài khoản |
| leaderEmployeeCode | Không | Để trống thì dùng leader đã gán cho Team; Team chưa có leader thì bắt buộc nhập |
| machineCode | Có | Máy phải thuộc đúng Team |
| plannedQuantity | Có | Số nguyên, không âm |
| actualQuantity | Có | Số nguyên, không âm |
| defectQuantity | Có | Không vượt `actualQuantity` |
| workingMinutes | Có | Số nguyên, không âm |
| downtimeMinutes | Có | Không vượt `workingMinutes` |
| note | Không | Ghi chú báo cáo |

`goodQuantity` được backend tự tính bằng `actualQuantity - defectQuantity`.

## Sheet NHAN_SU

| Cột | Bắt buộc | Ghi chú |
|---|---:|---|
| reportDate, shiftCode, teamCode, machineCode | Có | Khóa báo cáo cha |
| employeeCode | Có | Lấy từ `DM_NHAN_SU` |
| workingMinutes | Có | Không âm; phải bằng 0 nếu `ABSENT` |
| overtimeMinutes | Có | Không âm |
| attendanceStatus | Có | `PRESENT`, `ABSENT`, `LATE`, `LEAVE_EARLY`, `ON_LEAVE` |
| assignmentType | Có | `NORMAL`, `TRANSFERRED`, `SUPPORT`, `OVERTIME` |
| description | Không | Ghi chú |

Một nhân viên chỉ xuất hiện một lần trong cùng báo cáo. Nhân viên tổ khác phải dùng `SUPPORT` hoặc `TRANSFERRED`, không dùng `NORMAL`.

## Sheet DUNG_MAY

| Cột | Bắt buộc | Ghi chú |
|---|---:|---|
| reportDate, shiftCode, teamCode, machineCode | Có | Khóa báo cáo cha |
| downtimeReasonCode | Có | Lấy từ `DM_NGUYEN_NHAN` |
| startTime | Có | `yyyy-MM-dd HH:mm` |
| endTime | Có | Phải lớn hơn `startTime` |
| description | Không | Ghi chú sự cố |

`durationMinutes` do backend tự tính.

## Sheet CHAT_LUONG

| Cột | Bắt buộc | Ghi chú |
|---|---:|---|
| reportDate, shiftCode, teamCode, machineCode | Có | Khóa báo cáo cha |
| qualityErrorTypeCode | Có | Lấy từ `DM_LOAI_LOI` |
| quantity | Có | Số nguyên lớn hơn 0 |
| description | Không | Ghi chú lỗi |

Một loại lỗi chỉ có một dòng trong cùng báo cáo. Tổng lỗi chi tiết sau khi nhập không được vượt `defectQuantity`.

## Sheet VAT_TU

| Cột | Bắt buộc | Ghi chú |
|---|---:|---|
| reportDate, shiftCode, teamCode, machineCode | Có | Khóa báo cáo cha |
| materialCode | Có | Lấy từ `DM_VAT_TU` |
| issueType | Có | `SHORTAGE`, `LATE_DELIVERY`, `WRONG_SPECIFICATION`, `DAMAGED`, `QUALITY_FAILED`, `OTHER` |
| quantity | Có | Số lớn hơn 0 |
| unit | Không | Để trống thì lấy đơn vị hiện tại của Material |
| description | Không | Ghi chú |

## API

```http
GET  /api/v1/staging-report-excel/template
POST /api/v1/staging-report-excel/preview  multipart/form-data: file
POST /api/v1/staging-report-excel/import   multipart/form-data: file
```

Các vai trò được sử dụng: `ADMIN`, `FACTORY_MANAGER`, `DEPARTMENT_MANAGER`, `PRODUCTION_MANAGER`, `TEAM_LEADER`. Mọi dòng đều được kiểm tra lại theo phạm vi dữ liệu của tài khoản; không thể sửa file để nhập sang tổ khác.

## Cách xử lý nhập lại

- Báo cáo trùng khóa ngày/ca/tổ/máy: cập nhật nếu đang `DRAFT`.
- Nhân sự trùng báo cáo + nhân viên: cập nhật bản hiện có.
- Chất lượng trùng báo cáo + loại lỗi: cập nhật bản hiện có.
- Dừng máy trùng báo cáo + lý do + thời gian bắt đầu/kết thúc: cập nhật bản hiện có.
- Vật tư trùng báo cáo + vật tư + loại sự cố: cập nhật bản hiện có.
- Nếu có bất kỳ lỗi kiểm tra nào, toàn bộ file không được lưu.
