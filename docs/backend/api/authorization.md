# Phân quyền và phạm vi dữ liệu

> Phạm vi tài liệu: contract của implementation **Current/legacy**. Bảng role bên dưới phản ánh enum
> đang chạy, không phải mô hình Target đã duyệt. Target dùng
> `Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`; quyền quản trị/audit trực giao và
> Ca trưởng/Tổ trưởng là chức danh nhân sự. Việc ánh xạ/migration chưa được triển khai, xem
> [`../../prd.md`](../../prd.md) mục 5 và
> [`../../decisions.md`](../../decisions.md) `ADR-C03`.

## Vai trò

| Role | Phạm vi mặc định | Chức năng chính |
|---|---|---|
| `ADMIN` | Toàn hệ thống | Quản lý danh mục, tài khoản, role và toàn bộ báo cáo |
| `DIRECTOR` | Toàn công ty | Xem dashboard/báo cáo tổng hợp, xem và phê duyệt báo cáo |
| `FACTORY_MANAGER` | Nhà máy của Employee | Quản lý vận hành và phê duyệt trong nhà máy |
| `DEPARTMENT_MANAGER` | Phòng ban của Employee | Theo dõi và phê duyệt trong phòng ban |
| `FINANCE` | Dữ liệu official được cấp | Xem báo cáo chính thức phục vụ chi phí/tài chính |
| `PRODUCTION_MANAGER` | Dây chuyền của Employee | Theo dõi, trả sửa và phê duyệt báo cáo dây chuyền |
| `TEAM_LEADER` | Team của Employee | Nhập, sửa và gửi báo cáo ca cùng dữ liệu chi tiết |
| `EMPLOYEE` | Team/cá nhân | Xem dữ liệu thuộc phạm vi cá nhân; không phê duyệt |

## Cách xác định phạm vi

User không lưu lặp lại `factoryId`, `departmentId`, `productionLineId` hay `teamId`. Phạm vi được suy ra từ Employee:

```text
User → Employee → Team → ProductionLine → Department → Factory
```

`AuthorizationScope` được gọi từ `@PreAuthorize` để kiểm tra bản ghi đích có nằm trong phạm vi của tài khoản hay không.

Ví dụ:

```java
@PreAuthorize("@authorizationScope.canManageStaging(#id)")
```

```java
@PreAuthorize("@authorizationScope.canApproveStaging(#id)")
```

## Quy trình báo cáo

- Tạo/sửa/submit staging: `ADMIN`, `FACTORY_MANAGER`, `DEPARTMENT_MANAGER`, `PRODUCTION_MANAGER`, `TEAM_LEADER`, đồng thời phải đúng phạm vi.
- Thêm/sửa/xóa downtime, lỗi chất lượng, vật tư và nhân sự thực tế: phải có quyền sửa báo cáo cha.
- Request change/approve/lock: `ADMIN`, `DIRECTOR`, `FACTORY_MANAGER`, `DEPARTMENT_MANAGER`, `PRODUCTION_MANAGER`; quản lý cấp dưới phải đúng phạm vi.
- `TEAM_LEADER` không được tự phê duyệt báo cáo.
- `EMPLOYEE` không được thay đổi báo cáo staging.

## Master data

- Tạo/cập nhật/xóa mềm master data: chỉ `ADMIN`. Quản lý nhà máy được theo dõi dữ liệu vận hành nhưng không được thay đổi cấu trúc master data toàn hệ thống.
- Xóa mềm master data: chỉ `ADMIN`.
- API quản lý User/Role: chỉ `ADMIN`.

## Token và thay đổi quyền

Role nằm trong claim `scope`. Khi ADMIN đổi role, khóa hoặc vô hiệu hóa tài khoản, `tokenVersion` tăng lên nên JWT cũ mất hiệu lực ngay.

## Lưu ý

Các endpoint tổng hợp toàn hệ thống như `/all`, `/search`, `/dashboard` của báo cáo official hiện chỉ cho `ADMIN`, `DIRECTOR`, `FINANCE`. Quản lý cấp dưới sử dụng endpoint theo `teamId`, `machineId` hoặc bản ghi cụ thể để hệ thống kiểm tra phạm vi.
