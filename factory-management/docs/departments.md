# Department API

Base URL: `http://localhost:8080/factory-management/api/v1/departments`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/departments` |
| Danh mục loại phòng ban | GET | `/departments/types` |
| Danh sách hoạt động | GET | `/departments/all` |
| Lọc theo Factory | GET | `/departments/factory/{factoryId}` |
| Lấy theo ID | GET | `/departments/{id}` |
| Cập nhật từng trường | PUT | `/departments/{id}` |
| Xóa mềm | DELETE | `/departments/{id}` |

## Danh mục loại phòng ban cố định

`GET /departments/types` trả về sáu loại phòng ban để giao diện hiển thị cho Admin lựa chọn:

| departmentType | Hậu tố mã | Tên chuẩn | Mô tả mặc định |
| --- | --- | --- | --- |
| `PRODUCTION` | `SX` | Xưởng sản xuất | Phụ trách tổ chức, vận hành và giám sát hoạt động sản xuất. |
| `QUALITY` | `QA` | Phòng quản lý chất lượng | Kiểm soát chất lượng nguyên vật liệu, quy trình sản xuất và thành phẩm. |
| `MAINTENANCE` | `BT` | Phòng bảo trì | Bảo trì, sửa chữa máy móc và thiết bị của nhà máy. |
| `WAREHOUSE` | `KHO` | Bộ phận kho | Quản lý nhập, xuất, lưu trữ và tồn kho vật tư, thành phẩm. |
| `FINANCE` | `TC` | Phòng tài chính kế toán | Quản lý chi phí, doanh thu, công nợ và báo cáo tài chính. |
| `HUMAN_RESOURCES` | `NS` | Phòng nhân sự | Quản lý tuyển dụng, hồ sơ, chấm công và chính sách nhân sự. |

## Tạo Department

```json
{
  "factoryId": 1,
  "departmentType": "PRODUCTION",
  "description": "Bộ phận trực tiếp sản xuất tại nhà máy Bình Dương",
  "active": true
}
```

Admin không nhập `code` và `name`. Backend tự dùng tên chuẩn của `departmentType` và sinh mã theo công thức:

```text
{FACTORY_CODE}-{SUFFIX}
```

Ví dụ Factory có mã `BD` và loại `PRODUCTION` sẽ tạo Department có mã `BD-SX`, tên `Xưởng sản xuất`.

`description` là trường tùy chọn. Khi không truyền hoặc truyền chuỗi trống, backend dùng mô tả mặc định trong danh mục.
Trong cùng một Factory, mỗi `departmentType` chỉ được tạo một lần.

## Cập nhật Department

Chỉ gửi những trường cần thay đổi. Nếu đổi Factory hoặc loại phòng ban, backend tự sinh lại `code` và `name`:

```json
{
  "factoryId": 2,
  "departmentType": "QUALITY",
  "description": "Kiểm soát chất lượng tại nhà máy Đồng Nai",
  "active": true
}
```
