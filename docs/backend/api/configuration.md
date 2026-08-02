# Cấu hình dự án

Ứng dụng sử dụng Spring Boot, Spring Data JPA, PostgreSQL, MapStruct và Bean
Validation.

## PostgreSQL

Tạo file `.env` tại thư mục gốc:

```properties
DB_URL=jdbc:postgresql://localhost:5432/factorymanagement
DB_USERNAME=postgres
DB_PASSWORD=your_postgresql_password
```

Base URL của API:

```text
http://localhost:8080/factory-management/api/v1
```

Các API create yêu cầu đầy đủ trường bắt buộc. Các API update chỉ thay đổi
trường xuất hiện trong JSON. DELETE là xóa mềm bằng `active=false`.
