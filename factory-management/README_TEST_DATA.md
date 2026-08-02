# Bộ dữ liệu mẫu kiểm thử Factory Management

> Bộ fixture này phục vụ implementation **Current/legacy**. Các mã `DIRECTOR`, `FACTORY_MANAGER`,
> `PRODUCTION_MANAGER`, `TEAM_LEADER` và tên workspace trong tài liệu phải giữ nguyên cho đến khi code
> được migration. Chúng không thay thế mô hình Target
> `Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`; trong Target, Ca trưởng/Tổ trưởng là
> chức danh nhân sự, không phải security role. Không dùng fixture này làm bằng chứng rằng migration role,
> navigation hoặc phân quyền Target đã hoàn tất.

File Excel mẫu đã điền sẵn dữ liệu ngày `2026-07-12`: [MAU_NHAP_BAO_CAO_CA.xlsx](MAU_NHAP_BAO_CAO_CA.xlsx).

File này sử dụng các mã danh mục trong chính tài liệu này. Sau khi tạo dữ liệu nền và đăng nhập bằng `totruong.a1`, bạn có thể tải file lên màn hình Báo cáo trong ca để kiểm tra luồng import.

File này chứa dữ liệu mẫu để nhập lại hệ thống sau khi **xóa và tạo mới hoàn toàn database**.
Không chạy các JSON trong file này trên database đang có dữ liệu thật.

## 1. Quy ước sử dụng

Base URL:

```text
http://localhost:8080/factory-management/api/v1
```

Header cho tất cả API, trừ `/auth/login` và `/auth/register`:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Các danh sách JSON trong tài liệu chỉ là cách gom dữ liệu cho dễ đọc. API hiện tại không hỗ trợ
tạo hàng loạt, vì vậy hãy gửi **từng object một**, theo đúng thứ tự từ trên xuống.

Sau khi tạo mới database và khởi động backend:

- backend tự tạo Employee hệ thống `SYSTEM-ADMIN` với `employeeId = 1`;
- backend tự tạo User `admin` với `userId = 1`;
- các Employee tự tạo trong tài liệu này bắt đầu từ ID 2;
- ID của Factory, Department, Line, Team và các bảng khác bắt đầu từ 1.

Nếu ID trong response thực tế khác dự kiến, luôn ưu tiên giá trị `result.id` backend trả về.

## 2. Đăng nhập Admin

```http
POST /auth/login
```

```json
{
  "username": "admin",
  "password": "Admin@123456"
}
```

Lưu `result.token` vào biến Postman `token`. Có thể thêm script ở tab Tests:

```javascript
const body = pm.response.json();
pm.environment.set("token", body.result.token);
```

## 3. Nhà máy

Gửi từng object đến:

```http
POST /factories
```

```json
[
  {
    "code": "BD",
    "name": "Nhà máy Bình Dương",
    "address": "Khu công nghiệp Sóng Thần, thành phố Dĩ An, Bình Dương",
    "active": true
  },
  {
    "code": "DN",
    "name": "Nhà máy Đồng Nai",
    "address": "Khu công nghiệp Amata, thành phố Biên Hòa, Đồng Nai",
    "active": true
  }
]
```

ID dự kiến:

| ID | Code | Tên |
| --- | --- | --- |
| 1 | BD | Nhà máy Bình Dương |
| 2 | DN | Nhà máy Đồng Nai |

## 4. Phòng ban

Gửi từng object đến:

```http
POST /departments
```

Không nhập `code` và `name`. Chọn `departmentType`; backend tự lấy tên chuẩn và sinh mã theo công thức
`{FACTORY_CODE}-{SUFFIX}`. Trường `description` có thể dùng để bổ sung mô tả theo từng nhà máy; nếu bỏ trống,
backend sử dụng mô tả mặc định của loại phòng ban.

Có thể lấy danh mục loại phòng ban cố định bằng:

```http
GET /departments/types
```

```json
[
  {
    "factoryId": 1,
    "departmentType": "PRODUCTION",
    "description": "Bộ phận trực tiếp sản xuất tại nhà máy Bình Dương",
    "active": true
  },
  {
    "factoryId": 1,
    "departmentType": "QUALITY",
    "description": "Kiểm soát chất lượng nguyên liệu và thành phẩm",
    "active": true
  },
  {
    "factoryId": 1,
    "departmentType": "MAINTENANCE",
    "description": "Bảo trì máy móc và thiết bị nhà máy",
    "active": true
  },
  {
    "factoryId": 1,
    "departmentType": "WAREHOUSE",
    "description": "Quản lý nhập, xuất và tồn kho",
    "active": true
  },
  {
    "factoryId": 1,
    "departmentType": "FINANCE",
    "description": "Quản lý chi phí, doanh thu và công nợ",
    "active": true
  },
  {
    "factoryId": 2,
    "departmentType": "PRODUCTION",
    "description": "Bộ phận trực tiếp sản xuất tại nhà máy Đồng Nai",
    "active": true
  },
  {
    "factoryId": 2,
    "departmentType": "QUALITY",
    "description": "Kiểm soát chất lượng tại nhà máy Đồng Nai",
    "active": true
  }
]
```

ID dự kiến:

| ID | Nhà máy | Code | Tên |
| --- | --- | --- | --- |
| 1 | BD | BD-SX | Xưởng sản xuất |
| 2 | BD | BD-QA | Phòng quản lý chất lượng |
| 3 | BD | BD-BT | Phòng bảo trì |
| 4 | BD | BD-KHO | Bộ phận kho |
| 5 | BD | BD-TC | Phòng tài chính kế toán |
| 6 | DN | DN-SX | Xưởng sản xuất |
| 7 | DN | DN-QA | Phòng quản lý chất lượng |

## 5. Dây chuyền sản xuất

Gửi từng object đến:

```http
POST /production-lines
```

```json
[
  {
    "departmentId": 1,
    "code": "LINE-A",
    "name": "Dây chuyền CNC A",
    "description": "Dây chuyền gia công CNC chính tại Bình Dương",
    "active": true
  },
  {
    "departmentId": 1,
    "code": "LINE-B",
    "name": "Dây chuyền dập B",
    "description": "Dây chuyền dập và tạo hình kim loại",
    "active": true
  },
  {
    "departmentId": 6,
    "code": "LINE-A",
    "name": "Dây chuyền CNC Đồng Nai",
    "description": "Dây chuyền gia công CNC tại Đồng Nai",
    "active": true
  }
]
```

ID dự kiến: `1 = BD/LINE-A`, `2 = BD/LINE-B`, `3 = DN/LINE-A`.

## 6. Tổ sản xuất

Gửi từng object đến:

```http
POST /teams
```

```json
[
  {
    "productionLineId": 1,
    "code": "TEAM-A1",
    "name": "Tổ CNC A1",
    "description": "Tổ vận hành ca chính dây chuyền CNC A",
    "active": true
  },
  {
    "productionLineId": 1,
    "code": "TEAM-A2",
    "name": "Tổ CNC A2",
    "description": "Tổ vận hành ca hai dây chuyền CNC A",
    "active": true
  },
  {
    "productionLineId": 2,
    "code": "TEAM-B1",
    "name": "Tổ dập B1",
    "description": "Tổ vận hành dây chuyền dập B",
    "active": true
  },
  {
    "productionLineId": 3,
    "code": "TEAM-DN-A1",
    "name": "Tổ CNC Đồng Nai A1",
    "description": "Tổ vận hành dây chuyền CNC Đồng Nai",
    "active": true
  }
]
```

ID dự kiến: `1 = TEAM-A1`, `2 = TEAM-A2`, `3 = TEAM-B1`, `4 = TEAM-DN-A1`.

## 7. Nhân viên

Gửi từng object đến:

```http
POST /employees
```

```json
[
  {
    "teamId": null,
    "code": "DIR001",
    "fullName": "Trần Quốc Hùng",
    "position": "Giám đốc điều hành",
    "hireDate": "2020-01-15",
    "active": true
  },
  {
    "teamId": null,
    "code": "FM-BD-001",
    "fullName": "Nguyễn Hoàng Nam",
    "position": "Quản lý nhà máy Bình Dương",
    "hireDate": "2021-03-10",
    "active": true
  },
  {
    "teamId": null,
    "code": "DM-SX-001",
    "fullName": "Lê Minh Tuấn",
    "position": "Trưởng bộ phận sản xuất",
    "hireDate": "2021-06-01",
    "active": true
  },
  {
    "teamId": null,
    "code": "FIN001",
    "fullName": "Nguyễn Minh Anh",
    "position": "Nhân viên tài chính kế toán",
    "hireDate": "2022-01-15",
    "active": true
  },
  {
    "teamId": 1,
    "code": "PM001",
    "fullName": "Phạm Đức Long",
    "position": "Quản lý sản xuất Line A",
    "hireDate": "2022-05-09",
    "active": true
  },
  {
    "teamId": 1,
    "code": "TL-A1-001",
    "fullName": "Võ Thành Công",
    "position": "Tổ trưởng CNC A1",
    "hireDate": "2023-02-01",
    "active": true
  },
  {
    "teamId": 1,
    "code": "EMP-A1-001",
    "fullName": "Nguyễn Văn An",
    "position": "Công nhân vận hành CNC",
    "hireDate": "2024-01-10",
    "active": true
  },
  {
    "teamId": 1,
    "code": "EMP-A1-002",
    "fullName": "Trần Văn Bình",
    "position": "Công nhân vận hành CNC",
    "hireDate": "2024-02-12",
    "active": true
  },
  {
    "teamId": 2,
    "code": "TL-A2-001",
    "fullName": "Lê Thị Thu Hà",
    "position": "Tổ trưởng CNC A2",
    "hireDate": "2023-04-15",
    "active": true
  },
  {
    "teamId": 2,
    "code": "EMP-A2-001",
    "fullName": "Hoàng Văn Dũng",
    "position": "Công nhân vận hành CNC",
    "hireDate": "2024-03-18",
    "active": true
  },
  {
    "teamId": 3,
    "code": "TL-B1-001",
    "fullName": "Đặng Quốc Bảo",
    "position": "Tổ trưởng dây chuyền dập",
    "hireDate": "2022-11-20",
    "active": true
  },
  {
    "teamId": 3,
    "code": "EMP-B1-001",
    "fullName": "Bùi Minh Khoa",
    "position": "Công nhân vận hành máy dập",
    "hireDate": "2024-04-08",
    "active": true
  },
  {
    "teamId": null,
    "code": "FM-DN-001",
    "fullName": "Đỗ Thành Đạt",
    "position": "Quản lý nhà máy Đồng Nai",
    "hireDate": "2021-08-16",
    "active": true
  },
  {
    "teamId": 4,
    "code": "TL-DN-A1-001",
    "fullName": "Ngô Thanh Tùng",
    "position": "Tổ trưởng CNC Đồng Nai",
    "hireDate": "2023-07-03",
    "active": true
  },
  {
    "teamId": 4,
    "code": "EMP-DN-A1-001",
    "fullName": "Phan Văn Hải",
    "position": "Công nhân vận hành CNC",
    "hireDate": "2024-05-06",
    "active": true
  },
  {
    "teamId": null,
    "code": "QA-BD-001",
    "fullName": "Đinh Thị Mai",
    "position": "Nhân viên kiểm soát chất lượng",
    "hireDate": "2023-09-11",
    "active": true
  }
]
```

ID Employee dự kiến:

| ID | Code | Vai trò nghiệp vụ |
| --- | --- | --- |
| 1 | SYSTEM-ADMIN | Admin hệ thống tự tạo |
| 2 | DIR001 | Giám đốc |
| 3 | FM-BD-001 | Quản lý nhà máy Bình Dương |
| 4 | DM-SX-001 | Trưởng bộ phận sản xuất |
| 5 | FIN001 | Tài chính kế toán |
| 6 | PM001 | Quản lý sản xuất Line A |
| 7 | TL-A1-001 | Tổ trưởng Team A1 |
| 8 | EMP-A1-001 | Nhân viên Team A1 |
| 9 | EMP-A1-002 | Nhân viên Team A1 |
| 10 | TL-A2-001 | Tổ trưởng Team A2 |
| 11 | EMP-A2-001 | Nhân viên Team A2 |
| 12 | TL-B1-001 | Tổ trưởng Team B1 |
| 13 | EMP-B1-001 | Nhân viên Team B1 |
| 14 | FM-DN-001 | Quản lý nhà máy Đồng Nai |
| 15 | TL-DN-A1-001 | Tổ trưởng Team Đồng Nai |
| 16 | EMP-DN-A1-001 | Nhân viên Team Đồng Nai |
| 17 | QA-BD-001 | Nhân viên QA |

## 8. Gán tổ trưởng

```http
PUT /teams/1/leader
```

```json
{ "employeeId": 7 }
```

```http
PUT /teams/2/leader
```

```json
{ "employeeId": 10 }
```

```http
PUT /teams/3/leader
```

```json
{ "employeeId": 12 }
```

```http
PUT /teams/4/leader
```

```json
{ "employeeId": 15 }
```

## 9. Ca làm việc

Gửi từng object đến `POST /shifts`:

```json
[
  {
    "code": "CA-SANG",
    "name": "Ca sáng",
    "startTime": "06:00",
    "endTime": "14:00",
    "active": true
  },
  {
    "code": "CA-CHIEU",
    "name": "Ca chiều",
    "startTime": "14:00",
    "endTime": "22:00",
    "active": true
  },
  {
    "code": "CA-DEM",
    "name": "Ca đêm",
    "startTime": "22:00",
    "endTime": "06:00",
    "active": true
  }
]
```

ID dự kiến: `1 = Ca sáng`, `2 = Ca chiều`, `3 = Ca đêm`.

## 10. Loại máy

Gửi từng object đến `POST /machine-types`:

```json
[
  {
    "code": "CNC",
    "name": "Máy CNC",
    "description": "Máy gia công điều khiển số",
    "active": true
  },
  {
    "code": "PRESS",
    "name": "Máy dập",
    "description": "Máy dập và tạo hình kim loại",
    "active": true
  },
  {
    "code": "PACKING",
    "name": "Máy đóng gói",
    "description": "Máy đóng gói thành phẩm",
    "active": true
  },
  {
    "code": "WELDING",
    "name": "Máy hàn tự động",
    "description": "Máy hàn tự động trong dây chuyền",
    "active": true
  }
]
```

ID dự kiến: `1 = CNC`, `2 = PRESS`, `3 = PACKING`, `4 = WELDING`.

## 11. Máy móc

Gửi từng object đến `POST /machines`:

```json
[
  {
    "machineTypeId": 1,
    "teamId": 1,
    "code": "BD-CNC-01",
    "name": "Máy CNC Bình Dương 01",
    "serialNumber": "SN-BD-CNC-001",
    "status": "RUNNING",
    "installationDate": "2023-02-15",
    "description": "Máy CNC chính của tổ A1",
    "active": true
  },
  {
    "machineTypeId": 1,
    "teamId": 1,
    "code": "BD-CNC-02",
    "name": "Máy CNC Bình Dương 02",
    "serialNumber": "SN-BD-CNC-002",
    "status": "IDLE",
    "installationDate": "2023-06-10",
    "description": "Máy CNC dự phòng của tổ A1",
    "active": true
  },
  {
    "machineTypeId": 4,
    "teamId": 2,
    "code": "BD-WELD-01",
    "name": "Máy hàn tự động 01",
    "serialNumber": "SN-BD-WELD-001",
    "status": "RUNNING",
    "installationDate": "2024-01-08",
    "description": "Máy hàn tự động của tổ A2",
    "active": true
  },
  {
    "machineTypeId": 2,
    "teamId": 3,
    "code": "BD-PRESS-01",
    "name": "Máy dập Bình Dương 01",
    "serialNumber": "SN-BD-PRESS-001",
    "status": "RUNNING",
    "installationDate": "2022-11-20",
    "description": "Máy dập chính của tổ B1",
    "active": true
  },
  {
    "machineTypeId": 1,
    "teamId": 4,
    "code": "DN-CNC-01",
    "name": "Máy CNC Đồng Nai 01",
    "serialNumber": "SN-DN-CNC-001",
    "status": "RUNNING",
    "installationDate": "2024-03-12",
    "description": "Máy CNC chính tại Đồng Nai",
    "active": true
  }
]
```

ID dự kiến: `1 = BD-CNC-01`, `2 = BD-CNC-02`, `3 = BD-WELD-01`,
`4 = BD-PRESS-01`, `5 = DN-CNC-01`.

## 12. Lý do dừng máy

Gửi từng object đến `POST /downtime-reasons`:

```json
[
  {
    "code": "MACHINE_FAILURE",
    "name": "Hỏng máy",
    "description": "Máy hỏng đột xuất trong quá trình sản xuất",
    "reasonType": "UNPLANNED",
    "active": true
  },
  {
    "code": "POWER_OUTAGE",
    "name": "Mất điện",
    "description": "Nguồn điện nhà máy bị gián đoạn",
    "reasonType": "UNPLANNED",
    "active": true
  },
  {
    "code": "MATERIAL_SHORTAGE",
    "name": "Thiếu nguyên vật liệu",
    "description": "Dây chuyền phải chờ cấp vật tư",
    "reasonType": "UNPLANNED",
    "active": true
  },
  {
    "code": "PLANNED_MAINTENANCE",
    "name": "Bảo trì định kỳ",
    "description": "Dừng máy theo kế hoạch bảo trì",
    "reasonType": "PLANNED",
    "active": true
  },
  {
    "code": "WAITING_QC",
    "name": "Chờ kiểm tra chất lượng",
    "description": "Chờ nhân viên QA xác nhận chất lượng",
    "reasonType": "UNPLANNED",
    "active": true
  },
  {
    "code": "WAITING_OPERATOR",
    "name": "Chờ nhân công",
    "description": "Thiếu người vận hành tại vị trí máy",
    "reasonType": "UNPLANNED",
    "active": true
  }
]
```

ID dự kiến lần lượt từ 1 đến 6 theo thứ tự trên.

## 13. Loại lỗi chất lượng

Gửi từng object đến `POST /quality-error-types`:

```json
[
  {
    "code": "DIMENSION_ERROR",
    "name": "Sai kích thước",
    "description": "Kích thước sản phẩm vượt dung sai kỹ thuật",
    "severity": "HIGH",
    "active": true
  },
  {
    "code": "SURFACE_SCRATCH",
    "name": "Trầy xước bề mặt",
    "description": "Bề mặt sản phẩm bị trầy xước",
    "severity": "MEDIUM",
    "active": true
  },
  {
    "code": "WELD_ERROR",
    "name": "Lỗi mối hàn",
    "description": "Mối hàn không đạt tiêu chuẩn kỹ thuật",
    "severity": "HIGH",
    "active": true
  },
  {
    "code": "MATERIAL_DEFECT",
    "name": "Lỗi nguyên vật liệu",
    "description": "Sản phẩm lỗi do chất lượng vật liệu đầu vào",
    "severity": "MEDIUM",
    "active": true
  },
  {
    "code": "STRUCTURAL_CRACK",
    "name": "Nứt kết cấu",
    "description": "Sản phẩm xuất hiện vết nứt ảnh hưởng an toàn",
    "severity": "CRITICAL",
    "active": true
  }
]
```

ID dự kiến: `1 = DIMENSION_ERROR`, `2 = SURFACE_SCRATCH`, `3 = WELD_ERROR`,
`4 = MATERIAL_DEFECT`, `5 = STRUCTURAL_CRACK`.

## 14. Vật tư

Gửi từng object đến `POST /materials`:

```json
[
  {
    "code": "STEEL-304",
    "name": "Thép không gỉ 304",
    "unit": "KG",
    "description": "Thép 304 phục vụ gia công cơ khí",
    "active": true
  },
  {
    "code": "CUTTING-OIL",
    "name": "Dầu cắt gọt",
    "unit": "LIT",
    "description": "Dầu làm mát và bôi trơn máy CNC",
    "active": true
  },
  {
    "code": "PAINT-WHITE",
    "name": "Sơn công nghiệp màu trắng",
    "unit": "LIT",
    "description": "Sơn phủ bề mặt thành phẩm",
    "active": true
  },
  {
    "code": "BEARING-6205",
    "name": "Vòng bi 6205",
    "unit": "PCS",
    "description": "Phụ tùng thay thế và bảo trì máy",
    "active": true
  },
  {
    "code": "PACKING-BOX",
    "name": "Thùng đóng gói thành phẩm",
    "unit": "PCS",
    "description": "Bao bì đóng gói hàng xuất kho",
    "active": true
  },
  {
    "code": "ALUMINUM-6061",
    "name": "Nhôm hợp kim 6061",
    "unit": "KG",
    "description": "Nhôm dùng cho dây chuyền Đồng Nai",
    "active": true
  }
]
```

ID dự kiến lần lượt từ 1 đến 6 theo thứ tự trên.

## 15. Tạo tài khoản cho các vai trò

Gửi từng object đến:

```http
POST /auth/register
```

Đây là mật khẩu kiểm thử, không sử dụng trong môi trường thật.

```json
[
  { "employeeId": 2, "username": "giamdoc", "password": "Test@123456" },
  { "employeeId": 3, "username": "qlnhamay.bd", "password": "Test@123456" },
  { "employeeId": 4, "username": "truongbophan.sx", "password": "Test@123456" },
  { "employeeId": 5, "username": "ketoan01", "password": "Test@123456" },
  { "employeeId": 6, "username": "qlsanxuat.a", "password": "Test@123456" },
  { "employeeId": 7, "username": "totruong.a1", "password": "Test@123456" },
  { "employeeId": 8, "username": "nhanvien.a1", "password": "Test@123456" },
  { "employeeId": 14, "username": "qlnhamay.dn", "password": "Test@123456" },
  { "employeeId": 15, "username": "totruong.dn", "password": "Test@123456" },
  { "employeeId": 10, "username": "totruong.a2", "password": "Test@123456" }
]
```

User ID dự kiến:

| User ID | Username | Employee ID | Role cần cấp |
| --- | --- | --- | --- |
| 1 | admin | 1 | ADMIN |
| 2 | giamdoc | 2 | DIRECTOR |
| 3 | qlnhamay.bd | 3 | FACTORY_MANAGER |
| 4 | truongbophan.sx | 4 | DEPARTMENT_MANAGER |
| 5 | ketoan01 | 5 | FINANCE |
| 6 | qlsanxuat.a | 6 | PRODUCTION_MANAGER |
| 7 | totruong.a1 | 7 | TEAM_LEADER |
| 8 | nhanvien.a1 | 8 | EMPLOYEE |
| 9 | qlnhamay.dn | 14 | FACTORY_MANAGER |
| 10 | totruong.dn | 15 | TEAM_LEADER |
| 11 | totruong.a2 | 10 | TEAM_LEADER |

## 16. Cấp role

API cấp role thay thế toàn bộ role hiện tại. Gọi lần lượt bằng token Admin:

```http
PUT /users/2/roles
```

```json
{ "roles": ["DIRECTOR"] }
```

```http
PUT /users/3/roles
```

```json
{ "roles": ["FACTORY_MANAGER"] }
```

```http
PUT /users/4/roles
```

```json
{ "roles": ["DEPARTMENT_MANAGER"] }
```

```http
PUT /users/5/roles
```

```json
{ "roles": ["FINANCE"] }
```

```http
PUT /users/6/roles
```

```json
{ "roles": ["PRODUCTION_MANAGER"] }
```

```http
PUT /users/7/roles
```

```json
{ "roles": ["TEAM_LEADER"] }
```

```http
PUT /users/9/roles
```

```json
{ "roles": ["FACTORY_MANAGER"] }
```

```http
PUT /users/10/roles
```

```json
{ "roles": ["TEAM_LEADER"] }
```

```http
PUT /users/11/roles
```

```json
{ "roles": ["TEAM_LEADER"] }
```

User 8 giữ role mặc định `EMPLOYEE`. Sau khi đổi role, token cũ của chính tài khoản đó mất hiệu lực;
hãy đăng nhập lại khi muốn kiểm thử bằng tài khoản vừa được phân quyền.

## 17. Cấp phạm vi dữ liệu

Gọi bằng token Admin.

Quản lý nhà máy Bình Dương, user 3:

```http
POST /users/3/data-scopes
```

```json
{ "scopeType": "FACTORY", "scopeId": 1 }
```

Trưởng bộ phận sản xuất Bình Dương, user 4:

```http
POST /users/4/data-scopes
```

```json
{ "scopeType": "DEPARTMENT", "scopeId": 1 }
```

Kế toán, user 5, được xem cả hai nhà máy. Gửi hai request:

```http
POST /users/5/data-scopes
```

```json
{ "scopeType": "FACTORY", "scopeId": 1 }
```

```json
{ "scopeType": "FACTORY", "scopeId": 2 }
```

Quản lý sản xuất Line A, user 6:

```http
POST /users/6/data-scopes
```

```json
{ "scopeType": "PRODUCTION_LINE", "scopeId": 1 }
```

Tổ trưởng A1, user 7:

```http
POST /users/7/data-scopes
```

```json
{ "scopeType": "TEAM", "scopeId": 1 }
```

Quản lý nhà máy Đồng Nai, user 9:

```http
POST /users/9/data-scopes
```

```json
{ "scopeType": "FACTORY", "scopeId": 2 }
```

Tổ trưởng Đồng Nai, user 10:

```http
POST /users/10/data-scopes
```

```json
{ "scopeType": "TEAM", "scopeId": 4 }
```

Tổ trưởng A2, user 11:

```http
POST /users/11/data-scopes
```

```json
{ "scopeType": "TEAM", "scopeId": 2 }
```

## 18. Báo cáo sản xuất số 1 - hoàn tất và duyệt

### 18.1. Tạo báo cáo tạm

```http
POST /production-report-staging
```

```json
{
  "reportDate": "2026-07-10",
  "shiftId": 1,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 1,
  "teamId": 1,
  "leaderEmployeeId": 7,
  "machineId": 1,
  "plannedQuantity": 1200,
  "actualQuantity": 1165,
  "defectQuantity": 15,
  "workingMinutes": 480,
  "downtimeMinutes": 35,
  "note": "Ca sáng Line A - đơn hàng cơ khí tháng 7"
}
```

Staging ID dự kiến: `1`. Backend tự tính `goodQuantity = 1150`.

### 18.2. Dừng máy của báo cáo 1

Gửi từng object đến `POST /machine-downtime-staging`. Không truyền `durationMinutes` vì backend tự tính.

```json
[
  {
    "productionReportStagingId": 1,
    "machineId": 1,
    "downtimeReasonId": 1,
    "startTime": "2026-07-10T08:15:00",
    "endTime": "2026-07-10T08:35:00",
    "description": "Máy dừng do lỗi cảm biến trục chính",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "machineId": 1,
    "downtimeReasonId": 3,
    "startTime": "2026-07-10T10:20:00",
    "endTime": "2026-07-10T10:35:00",
    "description": "Chờ bổ sung thép 304 từ kho",
    "active": true
  }
]
```

Tổng dừng máy chi tiết: `20 + 15 = 35 phút`.

### 18.3. Lỗi chất lượng của báo cáo 1

Gửi từng object đến `POST /quality-report-staging`:

```json
[
  {
    "productionReportStagingId": 1,
    "qualityErrorTypeId": 1,
    "quantity": 5,
    "description": "Năm sản phẩm sai kích thước sau công đoạn CNC",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "qualityErrorTypeId": 2,
    "quantity": 4,
    "description": "Bốn sản phẩm bị trầy xước bề mặt",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "qualityErrorTypeId": 3,
    "quantity": 6,
    "description": "Sáu sản phẩm có mối hàn không đạt",
    "active": true
  }
]
```

Tổng lỗi chi tiết: `5 + 4 + 6 = 15`, bằng `defectQuantity` của báo cáo cha.

### 18.4. Sự cố vật tư của báo cáo 1

Gửi từng object đến `POST /material-issue-staging`:

```json
[
  {
    "productionReportStagingId": 1,
    "materialId": 1,
    "issueType": "SHORTAGE",
    "quantity": 120.5,
    "unit": "KG",
    "description": "Thiếu thép 304 trong đầu ca",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "materialId": 2,
    "issueType": "QUALITY_FAILED",
    "quantity": 20,
    "unit": "LIT",
    "description": "Một phần dầu cắt gọt không đạt chất lượng",
    "active": true
  }
]
```

### 18.5. Nhân sự thực tế của báo cáo 1

Gửi từng object đến `POST /employee-actual-staging`:

```json
[
  {
    "productionReportStagingId": 1,
    "employeeId": 7,
    "workingMinutes": 480,
    "overtimeMinutes": 30,
    "attendanceStatus": "PRESENT",
    "assignmentType": "OVERTIME",
    "description": "Tổ trưởng ở lại bàn giao thêm 30 phút",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "employeeId": 8,
    "workingMinutes": 480,
    "overtimeMinutes": 0,
    "attendanceStatus": "PRESENT",
    "assignmentType": "NORMAL",
    "description": "Làm đủ ca",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "employeeId": 9,
    "workingMinutes": 450,
    "overtimeMinutes": 0,
    "attendanceStatus": "LATE",
    "assignmentType": "NORMAL",
    "description": "Đi muộn 30 phút",
    "active": true
  },
  {
    "productionReportStagingId": 1,
    "employeeId": 13,
    "workingMinutes": 120,
    "overtimeMinutes": 0,
    "attendanceStatus": "PRESENT",
    "assignmentType": "SUPPORT",
    "description": "Hỗ trợ từ tổ B1 trong hai giờ",
    "active": true
  }
]
```

### 18.6. Submit và approve báo cáo 1

```http
PUT /production-report-staging/1/submit
```

Không gửi body.

```http
POST /production-report-staging/1/approve
```

```json
{
  "remark": "Số liệu ca ngày 10/07 đã được đối chiếu và phê duyệt"
}
```

Sau bước này, backend tạo `ProductionReport` chính thức có ID dự kiến `1`.

## 19. Báo cáo sản xuất số 2 - kiểm thử yêu cầu sửa rồi duyệt

### 19.1. Tạo staging ID 2

```http
POST /production-report-staging
```

```json
{
  "reportDate": "2026-07-11",
  "shiftId": 2,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 1,
  "teamId": 2,
  "leaderEmployeeId": 10,
  "machineId": 3,
  "plannedQuantity": 1100,
  "actualQuantity": 1000,
  "defectQuantity": 100,
  "workingMinutes": 480,
  "downtimeMinutes": 50,
  "note": "Ca chiều tổ A2 - dữ liệu chờ quản lý kiểm tra"
}
```

`plannedQuantity = 1100`, `actualQuantity = 1000`, `defectQuantity = 100` là hợp lệ.
Backend hiểu `actualQuantity` đã bao gồm sản phẩm tốt và lỗi, nên `goodQuantity = 900`.

### 19.2. Chi tiết của báo cáo 2

```http
POST /machine-downtime-staging
```

```json
{
  "productionReportStagingId": 2,
  "machineId": 3,
  "downtimeReasonId": 5,
  "startTime": "2026-07-11T15:10:00",
  "endTime": "2026-07-11T16:00:00",
  "description": "Chờ QA xác nhận mối hàn đầu ca",
  "active": true
}
```

Gửi từng object đến `POST /quality-report-staging`:

```json
[
  {
    "productionReportStagingId": 2,
    "qualityErrorTypeId": 3,
    "quantity": 60,
    "description": "Sáu mươi sản phẩm lỗi mối hàn",
    "active": true
  },
  {
    "productionReportStagingId": 2,
    "qualityErrorTypeId": 2,
    "quantity": 30,
    "description": "Ba mươi sản phẩm trầy bề mặt",
    "active": true
  }
]
```

Tổng lỗi chi tiết là 90, nhỏ hơn `defectQuantity = 100`; code hiện tại vẫn chấp nhận.

```http
POST /material-issue-staging
```

```json
{
  "productionReportStagingId": 2,
  "materialId": 3,
  "issueType": "LATE_DELIVERY",
  "quantity": 100,
  "unit": "LIT",
  "description": "Sơn giao trễ so với kế hoạch",
  "active": true
}
```

Gửi từng object đến `POST /employee-actual-staging`:

```json
[
  {
    "productionReportStagingId": 2,
    "employeeId": 10,
    "workingMinutes": 480,
    "overtimeMinutes": 0,
    "attendanceStatus": "PRESENT",
    "assignmentType": "NORMAL",
    "description": "Tổ trưởng làm đủ ca",
    "active": true
  },
  {
    "productionReportStagingId": 2,
    "employeeId": 11,
    "workingMinutes": 480,
    "overtimeMinutes": 60,
    "attendanceStatus": "PRESENT",
    "assignmentType": "OVERTIME",
    "description": "Tăng ca một giờ xử lý hàng lỗi",
    "active": true
  }
]
```

### 19.3. Workflow yêu cầu sửa

```http
PUT /production-report-staging/2/submit
```

```http
PUT /production-report-staging/2/request-change
```

```json
{
  "comment": "Kiểm tra lại tổng thời gian dừng máy và bổ sung ghi chú"
}
```

```http
PUT /production-report-staging/2/return-to-draft
```

```http
PUT /production-report-staging/2
```

```json
{
  "downtimeMinutes": 55,
  "note": "Đã kiểm tra lại: 50 phút dừng máy và 5 phút thiết lập lại"
}
```

```http
PUT /production-report-staging/2/submit
```

```http
POST /production-report-staging/2/approve
```

```json
{
  "remark": "Đã bổ sung theo yêu cầu, đồng ý phê duyệt"
}
```

ProductionReport chính thức có ID dự kiến `2`.

## 20. Báo cáo số 3 - giữ ở trạng thái SUBMITTED

```http
POST /production-report-staging
```

```json
{
  "reportDate": "2026-07-12",
  "shiftId": 1,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 2,
  "teamId": 3,
  "leaderEmployeeId": 12,
  "machineId": 4,
  "plannedQuantity": 900,
  "actualQuantity": 880,
  "defectQuantity": 20,
  "workingMinutes": 480,
  "downtimeMinutes": 25,
  "note": "Báo cáo chờ quản lý sản xuất phê duyệt"
}
```

```http
POST /quality-report-staging
```

```json
{
  "productionReportStagingId": 3,
  "qualityErrorTypeId": 1,
  "quantity": 10,
  "description": "Mười sản phẩm sai kích thước sau dập",
  "active": true
}
```

```http
PUT /production-report-staging/3/submit
```

Không approve báo cáo này để giao diện có một báo cáo đang chờ duyệt.

## 21. Báo cáo số 4 - nhà máy Đồng Nai

```http
POST /production-report-staging
```

```json
{
  "reportDate": "2026-07-12",
  "shiftId": 1,
  "factoryId": 2,
  "departmentId": 6,
  "productionLineId": 3,
  "teamId": 4,
  "leaderEmployeeId": 15,
  "machineId": 5,
  "plannedQuantity": 1000,
  "actualQuantity": 970,
  "defectQuantity": 10,
  "workingMinutes": 480,
  "downtimeMinutes": 15,
  "note": "Ca sáng nhà máy Đồng Nai"
}
```

```http
POST /machine-downtime-staging
```

```json
{
  "productionReportStagingId": 4,
  "machineId": 5,
  "downtimeReasonId": 2,
  "startTime": "2026-07-12T09:00:00",
  "endTime": "2026-07-12T09:15:00",
  "description": "Mất điện cục bộ 15 phút",
  "active": true
}
```

```http
POST /quality-report-staging
```

```json
{
  "productionReportStagingId": 4,
  "qualityErrorTypeId": 4,
  "quantity": 10,
  "description": "Mười sản phẩm lỗi do phôi nhôm đầu vào",
  "active": true
}
```

Gửi từng object đến `POST /employee-actual-staging`:

```json
[
  {
    "productionReportStagingId": 4,
    "employeeId": 15,
    "workingMinutes": 480,
    "overtimeMinutes": 0,
    "attendanceStatus": "PRESENT",
    "assignmentType": "NORMAL",
    "description": "Tổ trưởng làm đủ ca",
    "active": true
  },
  {
    "productionReportStagingId": 4,
    "employeeId": 16,
    "workingMinutes": 480,
    "overtimeMinutes": 0,
    "attendanceStatus": "PRESENT",
    "assignmentType": "NORMAL",
    "description": "Nhân viên vận hành làm đủ ca",
    "active": true
  }
]
```

```http
PUT /production-report-staging/4/submit
```

```http
POST /production-report-staging/4/approve
```

```json
{
  "remark": "Báo cáo Đồng Nai hợp lệ"
}
```

ProductionReport chính thức có ID dự kiến `3`.

## 22. Kiểm tra báo cáo chính thức và Dashboard

```http
GET /production-reports/all
GET /production-reports/1
GET /production-reports/1/details
GET /production-reports/date/2026-07-10
GET /production-reports/team/1
GET /production-reports/machine/1
GET /production-reports/search?fromDate=2026-07-01&toDate=2026-07-31
GET /production-reports/search?fromDate=2026-07-01&toDate=2026-07-31&factoryId=1
GET /production-reports/search?factoryId=1&teamId=1&machineId=1
GET /production-reports/dashboard
GET /production-reports/dashboard/my-scope
```

ProductionReport chính thức không có API POST, PUT hoặc DELETE trực tiếp. Nó chỉ được sinh ra khi
approve staging. Có thể khóa staging đã duyệt sau khi đã kiểm tra xong:

```http
PUT /production-report-staging/1/lock
PUT /production-report-staging/2/lock
PUT /production-report-staging/4/lock
```

## 23. Kho

Gửi từng object đến `POST /warehouses`:

```json
[
  {
    "code": "WH-BD-RAW",
    "name": "Kho nguyên vật liệu Bình Dương",
    "factoryId": 1,
    "description": "Kho vật tư đầu vào của nhà máy Bình Dương",
    "active": true
  },
  {
    "code": "WH-BD-FINISHED",
    "name": "Kho thành phẩm Bình Dương",
    "factoryId": 1,
    "description": "Kho thành phẩm chờ xuất hàng",
    "active": true
  },
  {
    "code": "WH-DN-RAW",
    "name": "Kho nguyên vật liệu Đồng Nai",
    "factoryId": 2,
    "description": "Kho vật tư đầu vào của nhà máy Đồng Nai",
    "active": true
  }
]
```

ID dự kiến: `1 = WH-BD-RAW`, `2 = WH-BD-FINISHED`, `3 = WH-DN-RAW`.

## 24. Giao dịch nhập, xuất và điều chỉnh kho

Gửi từng object theo đúng thứ tự đến `POST /inventory/transactions`:

```json
[
  {
    "transactionDate": "2026-07-01",
    "transactionType": "INBOUND",
    "warehouseId": 1,
    "materialId": 1,
    "quantity": 5000,
    "unitCost": 62000,
    "referenceNo": "PNK-BD-20260701-001",
    "description": "Nhập thép 304 phục vụ kế hoạch tháng 7"
  },
  {
    "transactionDate": "2026-07-05",
    "transactionType": "OUTBOUND",
    "warehouseId": 1,
    "materialId": 1,
    "quantity": 1250,
    "unitCost": 62000,
    "referenceNo": "PXK-BD-20260705-001",
    "description": "Xuất thép cho Line A"
  },
  {
    "transactionDate": "2026-07-08",
    "transactionType": "ADJUSTMENT_IN",
    "warehouseId": 1,
    "materialId": 1,
    "quantity": 20,
    "unitCost": 62000,
    "referenceNo": "KK-BD-20260708-001",
    "description": "Điều chỉnh tăng thép sau kiểm kê"
  },
  {
    "transactionDate": "2026-07-09",
    "transactionType": "ADJUSTMENT_OUT",
    "warehouseId": 1,
    "materialId": 1,
    "quantity": 10,
    "unitCost": 62000,
    "referenceNo": "KK-BD-20260709-001",
    "description": "Điều chỉnh giảm hao hụt thép"
  },
  {
    "transactionDate": "2026-07-01",
    "transactionType": "INBOUND",
    "warehouseId": 1,
    "materialId": 2,
    "quantity": 800,
    "unitCost": 85000,
    "referenceNo": "PNK-BD-20260701-002",
    "description": "Nhập dầu cắt gọt"
  },
  {
    "transactionDate": "2026-07-06",
    "transactionType": "OUTBOUND",
    "warehouseId": 1,
    "materialId": 2,
    "quantity": 120,
    "unitCost": 85000,
    "referenceNo": "PXK-BD-20260706-002",
    "description": "Xuất dầu cắt gọt cho tổ A1"
  },
  {
    "transactionDate": "2026-07-02",
    "transactionType": "INBOUND",
    "warehouseId": 1,
    "materialId": 3,
    "quantity": 600,
    "unitCost": 110000,
    "referenceNo": "PNK-BD-20260702-003",
    "description": "Nhập sơn công nghiệp"
  },
  {
    "transactionDate": "2026-07-07",
    "transactionType": "OUTBOUND",
    "warehouseId": 1,
    "materialId": 3,
    "quantity": 85,
    "unitCost": 110000,
    "referenceNo": "PXK-BD-20260707-003",
    "description": "Xuất sơn cho tổ A2"
  },
  {
    "transactionDate": "2026-07-03",
    "transactionType": "INBOUND",
    "warehouseId": 2,
    "materialId": 5,
    "quantity": 3000,
    "unitCost": 12000,
    "referenceNo": "PNK-BD-20260703-004",
    "description": "Nhập thùng đóng gói"
  },
  {
    "transactionDate": "2026-07-10",
    "transactionType": "OUTBOUND",
    "warehouseId": 2,
    "materialId": 5,
    "quantity": 1000,
    "unitCost": 12000,
    "referenceNo": "PXK-BD-20260710-004",
    "description": "Xuất thùng đóng gói thành phẩm"
  },
  {
    "transactionDate": "2026-07-01",
    "transactionType": "INBOUND",
    "warehouseId": 3,
    "materialId": 6,
    "quantity": 2500,
    "unitCost": 78000,
    "referenceNo": "PNK-DN-20260701-001",
    "description": "Nhập nhôm 6061 cho nhà máy Đồng Nai"
  },
  {
    "transactionDate": "2026-07-10",
    "transactionType": "OUTBOUND",
    "warehouseId": 3,
    "materialId": 6,
    "quantity": 500,
    "unitCost": 78000,
    "referenceNo": "PXK-DN-20260710-001",
    "description": "Xuất nhôm cho dây chuyền Đồng Nai"
  }
]
```

Tồn kho dự kiến:

| Kho | Vật tư | Tồn dự kiến |
| --- | --- | ---: |
| WH-BD-RAW | STEEL-304 | 3760 KG |
| WH-BD-RAW | CUTTING-OIL | 680 LIT |
| WH-BD-RAW | PAINT-WHITE | 515 LIT |
| WH-BD-FINISHED | PACKING-BOX | 2000 PCS |
| WH-DN-RAW | ALUMINUM-6061 | 2000 KG |

Kiểm tra:

```http
GET /inventory/transactions?fromDate=2026-07-01&toDate=2026-07-31
GET /inventory/transactions?warehouseId=1&materialId=1
GET /inventory/stocks
GET /inventory/stocks?warehouseId=1
```

## 25. Danh mục tài chính

Gửi từng object đến `POST /financial-categories`:

```json
[
  {
    "code": "PRODUCT_REVENUE",
    "name": "Doanh thu bán thành phẩm",
    "entryType": "REVENUE",
    "expenseGroup": null,
    "description": "Doanh thu bán sản phẩm sản xuất tại nhà máy",
    "active": true
  },
  {
    "code": "MATERIAL_COST",
    "name": "Chi phí nguyên vật liệu",
    "entryType": "EXPENSE",
    "expenseGroup": "MATERIAL",
    "description": "Chi phí mua và sử dụng vật tư",
    "active": true
  },
  {
    "code": "LABOR_COST",
    "name": "Chi phí nhân công",
    "entryType": "EXPENSE",
    "expenseGroup": "LABOR",
    "description": "Lương, phụ cấp và chi phí nhân công",
    "active": true
  },
  {
    "code": "ELECTRICITY_COST",
    "name": "Chi phí điện nước",
    "entryType": "EXPENSE",
    "expenseGroup": "UTILITIES",
    "description": "Chi phí điện, nước và tiện ích vận hành",
    "active": true
  },
  {
    "code": "MAINTENANCE_COST",
    "name": "Chi phí bảo trì",
    "entryType": "EXPENSE",
    "expenseGroup": "MAINTENANCE",
    "description": "Chi phí sửa chữa và bảo trì máy móc",
    "active": true
  },
  {
    "code": "SCRAP_LOSS",
    "name": "Chi phí hao hụt và phế phẩm",
    "entryType": "EXPENSE",
    "expenseGroup": "LOSS",
    "description": "Giá trị hao hụt và phế phẩm sản xuất",
    "active": true
  },
  {
    "code": "COGS",
    "name": "Giá vốn hàng bán",
    "entryType": "EXPENSE",
    "expenseGroup": "COST_OF_GOODS_SOLD",
    "description": "Giá vốn sản phẩm đã bán",
    "active": true
  },
  {
    "code": "OTHER_COST",
    "name": "Chi phí vận hành khác",
    "entryType": "EXPENSE",
    "expenseGroup": "OTHER",
    "description": "Các chi phí vận hành chưa thuộc nhóm khác",
    "active": true
  }
]
```

ID dự kiến lần lượt từ 1 đến 8.

## 26. Bản ghi tài chính

Gửi từng object đến `POST /financial-records`:

```json
[
  {
    "recordDate": "2026-07-10",
    "categoryId": 1,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 800000000,
    "paidAmount": 500000000,
    "dueDate": "2026-07-30",
    "counterparty": "Công ty Cơ khí Việt Thành",
    "referenceNo": "HD-BD-20260710-001",
    "description": "Doanh thu đơn hàng cơ khí tháng 7",
    "active": true
  },
  {
    "recordDate": "2026-07-12",
    "categoryId": 1,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 350000000,
    "paidAmount": 350000000,
    "dueDate": "2026-07-12",
    "counterparty": "Công ty Thiết bị Minh Phát",
    "referenceNo": "HD-BD-20260712-002",
    "description": "Doanh thu lô hàng đã thanh toán",
    "active": true
  },
  {
    "recordDate": "2026-07-01",
    "categoryId": 2,
    "factoryId": 1,
    "departmentId": 1,
    "productionLineId": 1,
    "amount": 310000000,
    "paidAmount": 250000000,
    "dueDate": "2026-07-25",
    "counterparty": "Công ty Thép ABC",
    "referenceNo": "INV-STEEL-20260701",
    "description": "Chi phí thép tháng 7 cho Line A",
    "active": true
  },
  {
    "recordDate": "2026-07-14",
    "categoryId": 3,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 180000000,
    "paidAmount": 180000000,
    "dueDate": "2026-07-14",
    "counterparty": "Nhân sự nhà máy Bình Dương",
    "referenceNo": "PAYROLL-BD-202607",
    "description": "Chi phí lương tháng 7",
    "active": true
  },
  {
    "recordDate": "2026-07-12",
    "categoryId": 4,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 45000000,
    "paidAmount": 0,
    "dueDate": "2026-07-25",
    "counterparty": "Điện lực Bình Dương",
    "referenceNo": "EVN-BD-202607",
    "description": "Chi phí điện sản xuất tháng 7",
    "active": true
  },
  {
    "recordDate": "2026-07-08",
    "categoryId": 5,
    "factoryId": 1,
    "departmentId": 3,
    "productionLineId": null,
    "amount": 25000000,
    "paidAmount": 25000000,
    "dueDate": "2026-07-08",
    "counterparty": "Công ty Bảo trì Kỹ thuật Việt",
    "referenceNo": "MNT-BD-20260708",
    "description": "Bảo trì định kỳ máy CNC",
    "active": true
  },
  {
    "recordDate": "2026-07-11",
    "categoryId": 6,
    "factoryId": 1,
    "departmentId": 1,
    "productionLineId": 1,
    "amount": 12000000,
    "paidAmount": 0,
    "dueDate": "2026-07-31",
    "counterparty": "Nội bộ nhà máy",
    "referenceNo": "LOSS-BD-20260711",
    "description": "Giá trị phế phẩm phát sinh trên Line A",
    "active": true
  },
  {
    "recordDate": "2026-07-12",
    "categoryId": 7,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 260000000,
    "paidAmount": 200000000,
    "dueDate": "2026-07-30",
    "counterparty": "Nội bộ nhà máy",
    "referenceNo": "COGS-BD-202607",
    "description": "Giá vốn hàng bán tháng 7",
    "active": true
  },
  {
    "recordDate": "2026-07-13",
    "categoryId": 8,
    "factoryId": 1,
    "departmentId": null,
    "productionLineId": null,
    "amount": 8000000,
    "paidAmount": 8000000,
    "dueDate": "2026-07-13",
    "counterparty": "Nhà cung cấp dịch vụ vệ sinh",
    "referenceNo": "OTHER-BD-20260713",
    "description": "Chi phí vệ sinh công nghiệp",
    "active": true
  },
  {
    "recordDate": "2026-07-12",
    "categoryId": 1,
    "factoryId": 2,
    "departmentId": null,
    "productionLineId": null,
    "amount": 200000000,
    "paidAmount": 100000000,
    "dueDate": "2026-07-30",
    "counterparty": "Công ty Nhôm Đông Á",
    "referenceNo": "HD-DN-20260712-001",
    "description": "Doanh thu lô sản phẩm nhôm Đồng Nai",
    "active": true
  },
  {
    "recordDate": "2026-07-01",
    "categoryId": 2,
    "factoryId": 2,
    "departmentId": 6,
    "productionLineId": 3,
    "amount": 80000000,
    "paidAmount": 50000000,
    "dueDate": "2026-07-28",
    "counterparty": "Công ty Nhôm 6061 Việt Nam",
    "referenceNo": "INV-AL-DN-20260701",
    "description": "Chi phí nhôm cho Line Đồng Nai",
    "active": true
  }
]
```

Dashboard tài chính riêng Nhà máy Bình Dương trong tháng 7 dự kiến:

| Chỉ số | Giá trị |
| --- | ---: |
| Tổng doanh thu | 1.150.000.000 |
| Tổng chi phí | 840.000.000 |
| Lợi nhuận | 310.000.000 |
| Phải thu | 300.000.000 |
| Phải trả | 177.000.000 |

Kiểm tra:

```http
GET /financial-records/search?fromDate=2026-07-01&toDate=2026-07-31
GET /financial-records/search?fromDate=2026-07-01&toDate=2026-07-31&factoryId=1&type=EXPENSE
GET /financial-records/search?departmentId=1&productionLineId=1
GET /financial-records/dashboard?fromDate=2026-07-01&toDate=2026-07-31&factoryId=1
GET /financial-records/dashboard?fromDate=2026-07-01&toDate=2026-07-31&factoryId=2
```

## 27. Kiểm thử đăng nhập theo vai trò

Tất cả tài khoản dưới đây dùng mật khẩu kiểm thử `Test@123456`:

| Username | Role | Phạm vi chính |
| --- | --- | --- |
| giamdoc | DIRECTOR | Toàn hệ thống |
| qlnhamay.bd | FACTORY_MANAGER | Nhà máy Bình Dương |
| truongbophan.sx | DEPARTMENT_MANAGER | Xưởng sản xuất Bình Dương |
| ketoan01 | FINANCE | Hai nhà máy |
| qlsanxuat.a | PRODUCTION_MANAGER | Line A Bình Dương |
| totruong.a1 | TEAM_LEADER | Team A1 |
| nhanvien.a1 | EMPLOYEE | Cá nhân và Team A1 |
| qlnhamay.dn | FACTORY_MANAGER | Nhà máy Đồng Nai |
| totruong.dn | TEAM_LEADER | Team Đồng Nai A1 |
| totruong.a2 | TEAM_LEADER | Team A2 |

Ví dụ đăng nhập kế toán:

```http
POST /auth/login
```

```json
{
  "username": "ketoan01",
  "password": "Test@123456"
}
```

Các kiểm tra nên thực hiện:

```http
# Kế toán
GET /warehouses/all
GET /inventory/stocks
GET /financial-records/dashboard?fromDate=2026-07-01&toDate=2026-07-31
GET /production-reports/all

# Quản lý nhà máy Bình Dương
GET /production-reports/dashboard/my-scope
GET /production-reports/team/1

# Quản lý sản xuất Line A
GET /production-reports/dashboard/my-scope
GET /production-report-staging/team/1

# Tổ trưởng A1
GET /production-report-staging/team/1
GET /production-reports/dashboard/my-scope

# Nhân viên
GET /auth/me
GET /employee-portal/dashboard?fromDate=2026-07-01&toDate=2026-07-31
```

Lưu ý: `WorkSchedule`, `AttendanceRecord`, `EmployeeKpi` và `Notification` hiện chưa có API quản trị
để tạo dữ liệu mẫu. Vì vậy trang cá nhân có thể chưa có lịch làm, chấm công, KPI hoặc thông báo sau
khi tạo database mới. Các API báo cáo sản xuất, kho và tài chính vẫn có đầy đủ dữ liệu theo file này.

## 28. Kiểm thử cập nhật từng trường và xóa mềm

### Cập nhật riêng chức vụ nhân viên

```http
PUT /employees/8
```

```json
{
  "position": "Kỹ thuật viên vận hành CNC bậc 2"
}
```

Các trường còn lại phải giữ nguyên.

### Cập nhật trạng thái máy

```http
PUT /machines/2
```

```json
{
  "status": "MAINTENANCE",
  "description": "Máy đang bảo trì thử nghiệm"
}
```

Khôi phục trạng thái vận hành:

```http
PUT /machines/2
```

```json
{
  "status": "IDLE"
}
```

### Xóa mềm và khôi phục lý do dừng máy

```http
DELETE /downtime-reasons/6
```

```http
PUT /downtime-reasons/6
```

```json
{
  "active": true
}
```

### Xóa mềm và khôi phục vật tư

```http
DELETE /materials/4
```

```http
PUT /materials/4
```

```json
{
  "active": true
}
```

### Khóa và mở lại tài khoản nhân viên

```http
PUT /users/8/locked/true
PUT /users/8/locked/false
PUT /users/8/enabled/false
PUT /users/8/enabled/true
```

### Admin đặt lại mật khẩu

```http
PUT /users/8/reset-password
```

```json
{
  "newPassword": "Employee@456"
}
```

## 29. Các trường hợp lỗi nên kiểm thử

### 29.1. Trùng mã Factory

Gửi lại `POST /factories`:

```json
{
  "code": "BD",
  "name": "Nhà máy bị trùng",
  "address": "Địa chỉ kiểm thử",
  "active": true
}
```

Kỳ vọng: backend từ chối vì mã Factory đã tồn tại.

### 29.2. Sai chuỗi tổ chức của báo cáo

Team 1 thuộc Factory 1 nhưng payload lại truyền Factory 2:

```json
{
  "reportDate": "2026-07-14",
  "shiftId": 1,
  "factoryId": 2,
  "departmentId": 1,
  "productionLineId": 1,
  "teamId": 1,
  "leaderEmployeeId": 7,
  "machineId": 1,
  "plannedQuantity": 100,
  "actualQuantity": 90,
  "defectQuantity": 5,
  "workingMinutes": 480,
  "downtimeMinutes": 10,
  "note": "Payload sai quan hệ"
}
```

Kỳ vọng: `INVALID_REPORT_ORGANIZATION`.

### 29.3. Defect lớn hơn Actual

```json
{
  "reportDate": "2026-07-14",
  "shiftId": 1,
  "factoryId": 1,
  "departmentId": 1,
  "productionLineId": 1,
  "teamId": 1,
  "leaderEmployeeId": 7,
  "machineId": 2,
  "plannedQuantity": 100,
  "actualQuantity": 50,
  "defectQuantity": 60,
  "workingMinutes": 480,
  "downtimeMinutes": 10,
  "note": "Defect lớn hơn Actual"
}
```

Kỳ vọng: backend từ chối vì `defectQuantity > actualQuantity`.

### 29.4. Tổng lỗi chi tiết vượt defect của báo cáo 3

Báo cáo 3 có `defectQuantity = 20` và đã có 10 lỗi. Gửi thêm:

```http
POST /quality-report-staging
```

```json
{
  "productionReportStagingId": 3,
  "qualityErrorTypeId": 2,
  "quantity": 11,
  "description": "Dữ liệu cố ý vượt tổng lỗi",
  "active": true
}
```

Kỳ vọng: `QUALITY_TOTAL_EXCEEDS_REPORT_DEFECT`.

### 29.5. Xuất kho lớn hơn tồn

```http
POST /inventory/transactions
```

```json
{
  "transactionDate": "2026-07-14",
  "transactionType": "OUTBOUND",
  "warehouseId": 1,
  "materialId": 1,
  "quantity": 999999,
  "unitCost": 62000,
  "referenceNo": "PXK-INVALID",
  "description": "Kiểm thử xuất vượt tồn"
}
```

Kỳ vọng: `INSUFFICIENT_INVENTORY`.

### 29.6. Số tiền đã trả lớn hơn tổng tiền

```http
POST /financial-records
```

```json
{
  "recordDate": "2026-07-14",
  "categoryId": 2,
  "factoryId": 1,
  "departmentId": null,
  "productionLineId": null,
  "amount": 1000000,
  "paidAmount": 2000000,
  "dueDate": "2026-07-30",
  "counterparty": "Đối tác kiểm thử",
  "referenceNo": "FIN-INVALID",
  "description": "Paid amount lớn hơn amount",
  "active": true
}
```

Kỳ vọng: `INVALID_PAID_AMOUNT`.

### 29.7. Team Leader cố approve báo cáo

Đăng nhập bằng `totruong.a1`, sau đó gọi:

```http
POST /production-report-staging/3/approve
```

```json
{}
```

Kỳ vọng: bị từ chối quyền. Team Leader được nhập và submit báo cáo trong phạm vi Team, nhưng không
được approve.

### 29.8. Approve trực tiếp báo cáo DRAFT

Tạo một staging mới nhưng chưa submit, sau đó gọi `/approve`. Kỳ vọng nhận lỗi trạng thái `1119`.
Luồng đúng luôn là:

```text
DRAFT → SUBMITTED → APPROVED → LOCKED
```

Nhánh yêu cầu sửa:

```text
SUBMITTED → CHANGE_REQUESTED → DRAFT → SUBMITTED
```

## 30. Danh sách enum dùng trong dữ liệu

```text
Role:
ADMIN, DIRECTOR, FACTORY_MANAGER, DEPARTMENT_MANAGER,
FINANCE, PRODUCTION_MANAGER, TEAM_LEADER, EMPLOYEE

DataScopeType:
FACTORY, DEPARTMENT, PRODUCTION_LINE, TEAM

DepartmentType:
PRODUCTION (SX - Xưởng sản xuất),
QUALITY (QA - Phòng quản lý chất lượng),
MAINTENANCE (BT - Phòng bảo trì),
WAREHOUSE (KHO - Bộ phận kho),
FINANCE (TC - Phòng tài chính kế toán),
HUMAN_RESOURCES (NS - Phòng nhân sự)

MachineOperationalStatus:
IDLE, RUNNING, STOPPED, MAINTENANCE, BREAKDOWN

DowntimeReasonType:
PLANNED, UNPLANNED

QualityErrorSeverity:
LOW, MEDIUM, HIGH, CRITICAL

MaterialIssueType:
SHORTAGE, LATE_DELIVERY, WRONG_SPECIFICATION,
DAMAGED, QUALITY_FAILED, OTHER

AttendanceStatus:
PRESENT, ABSENT, LATE, LEAVE_EARLY, ON_LEAVE

AssignmentType:
NORMAL, TRANSFERRED, SUPPORT, OVERTIME

ProductionReportStatus:
DRAFT, SUBMITTED, CHANGE_REQUESTED, APPROVED, LOCKED

InventoryTransactionType:
INBOUND, OUTBOUND, ADJUSTMENT_IN, ADJUSTMENT_OUT

FinancialEntryType:
REVENUE, EXPENSE

ExpenseGroup:
MATERIAL, LABOR, UTILITIES, MAINTENANCE,
LOSS, COST_OF_GOODS_SOLD, OTHER
```

## 31. Checklist nhập dữ liệu từ đầu

- [ ] Tạo database mới và khởi động backend.
- [ ] Đăng nhập tài khoản Admin bootstrap.
- [ ] Tạo Factory.
- [ ] Tạo Department.
- [ ] Tạo ProductionLine.
- [ ] Tạo Team.
- [ ] Tạo Employee.
- [ ] Gán leader cho Team.
- [ ] Tạo Shift, MachineType và Machine.
- [ ] Tạo DowntimeReason, QualityErrorType và Material.
- [ ] Đăng ký tài khoản, cấp role và data scope.
- [ ] Tạo báo cáo staging và bốn nhóm dữ liệu chi tiết.
- [ ] Submit, yêu cầu sửa, approve và kiểm tra báo cáo chính thức.
- [ ] Tạo Warehouse rồi nhập kho trước khi xuất kho.
- [ ] Tạo FinancialCategory và FinancialRecord.
- [ ] Đối chiếu Dashboard sản xuất, tồn kho và Dashboard tài chính.
- [ ] Đăng nhập lần lượt từng vai trò để kiểm tra giới hạn quyền.
- [ ] Chỉ chạy các request lỗi ở mục 29 sau khi dữ liệu hợp lệ đã tạo xong.
# Nhập dữ liệu lịch sử bằng Excel

Tổ trưởng có thể nhập nhiều ngày dữ liệu cũ tại màn hình **Báo cáo trong ca → Nhập dữ liệu cũ từ Excel**.

- Tải mẫu trực tiếp trên web để nhận đúng mã ca, tổ, máy, nhân viên, nguyên nhân, loại lỗi và vật tư hiện có.
- Có thể điền riêng sheet `NHAN_SU`, `DUNG_MAY`, `CHAT_LUONG` hoặc `VAT_TU` nếu báo cáo `SAN_LUONG` tương ứng đã tồn tại ở trạng thái `DRAFT`.
- Luôn bấm **Kiểm tra file** trước. Nếu có lỗi, giao diện chỉ rõ sheet, dòng và cột cần sửa và backend không lưu một phần dữ liệu.
- Chi tiết đầy đủ: [docs/staging-report-excel.md](docs/staging-report-excel.md).
