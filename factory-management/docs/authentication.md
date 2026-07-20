# JWT Authentication (OAuth2 Resource Server + Nimbus)

Backend sử dụng JWT HS512 theo kiến trúc:

```text
Login → AuthService sinh JWT
Request Bearer → OAuth2 Resource Server
→ CustomJwtDecoder → introspect → NimbusJwtDecoder
→ SecurityContext
```

JWT có các claim:

- `sub`: username
- `iss`: `FactoryManagement`
- `jti`: mã duy nhất của token
- `scope`: danh sách role dạng `ROLE_ADMIN ROLE_EMPLOYEE`
- `userId`
- `employeeId`
- `tokenVersion`

## Cấu hình

```env
JWT_SIGNER_KEY=replace_with_a_random_hs512_secret_at_least_64_characters_long
JWT_VALID_DURATION=900000
JWT_REFRESH_DURATION=604800000
```

## Tài khoản Admin tự động

Khi ứng dụng khởi động, nếu cơ sở dữ liệu chưa có tài khoản mang role `ADMIN`, backend tự tạo:

- một Employee hệ thống không bắt buộc `teamId`;
- một tài khoản có role `ADMIN` liên kết với Employee hệ thống đó.

Cấu hình trong `.env`:

```env
ADMIN_BOOTSTRAP_ENABLED=true
ADMIN_USERNAME=admin
ADMIN_PASSWORD=change_this_admin_password
ADMIN_EMPLOYEE_CODE=SYSTEM-ADMIN
```

Nếu không khai báo các biến trên, tài khoản mặc định cho môi trường phát triển là `admin` / `Admin@123456`. Hãy đổi mật khẩu ngay sau lần đăng nhập đầu tiên. Những lần khởi động sau không tạo trùng nếu hệ thống đã có một tài khoản `ADMIN`.

## Đăng ký

Admin không cần đăng ký thủ công. Các tài khoản đăng ký qua API mặc định nhận role `EMPLOYEE`; sau đó Admin có thể phân quyền phù hợp.

```http
POST /factory-management/api/v1/auth/register
Content-Type: application/json

{
  "employeeId": 1,
  "username": "admin",
  "password": "Admin@123"
}
```

## Đăng nhập

```http
POST /factory-management/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123"
}
```

Kết quả chứa `result.token`. API bảo vệ sử dụng:

```http
Authorization: Bearer <token>
```

## Introspect

```http
POST /factory-management/api/v1/auth/introspect
Content-Type: application/json

{
  "token": "<token>"
}
```

Kết quả:

```json
{
  "code": 1000,
  "result": { "valid": true }
}
```

## Refresh

Cùng một JWT có thể refresh trong `JWT_REFRESH_DURATION`. Token cũ được đưa vào bảng `invalid_token`, sau đó backend cấp JWT mới.

```http
POST /factory-management/api/v1/auth/refresh
Content-Type: application/json

{
  "token": "<token>"
}
```

## Logout

Logout lưu `jti` vào `invalid_token`, vì vậy token không thể sử dụng lại.

```http
POST /factory-management/api/v1/auth/logout
Content-Type: application/json

{
  "token": "<token>"
}
```

## Thông tin tài khoản hiện tại

```http
GET /factory-management/api/v1/auth/me
Authorization: Bearer <token>
```

## Đổi mật khẩu

```http
PUT /factory-management/api/v1/auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "Admin@123",
  "newPassword": "Admin@456"
}
```

Đổi mật khẩu, đổi role, khóa hoặc vô hiệu hóa tài khoản sẽ tăng `tokenVersion`; toàn bộ JWT cũ mất hiệu lực ngay cả khi chưa hết hạn.

## Quản lý role

Chỉ `ADMIN` được gọi:

```http
GET /factory-management/api/v1/users/all
PUT /factory-management/api/v1/users/{id}/roles
PUT /factory-management/api/v1/users/{id}/enabled/{enabled}
PUT /factory-management/api/v1/users/{id}/locked/{locked}
```

Ví dụ gán role:

```json
{
  "roles": ["PRODUCTION_MANAGER"]
}
```

## Phê duyệt báo cáo

Employee phê duyệt được lấy từ `sub` trong JWT, client không truyền `approvedByEmployeeId`.

```http
POST /factory-management/api/v1/production-report-staging/1/approve
Authorization: Bearer <manager-token>
Content-Type: application/json

{
  "remark": "Số liệu hợp lệ"
}
```
