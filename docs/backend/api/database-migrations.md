# Database migrations

## Employee không bắt buộc thuộc Team

Schema cũ có thể vẫn giữ `employee.team_id NOT NULL` dù Entity hiện cho phép nhân viên chưa được phân vào Team.

Migration tương ứng:

```sql
ALTER TABLE employee
    ALTER COLUMN team_id DROP NOT NULL;
```

Project chạy câu lệnh idempotent này từ `src/main/resources/schema.sql` khi khởi động. Sau khi cập nhật code, cần dừng và khởi động lại Spring Boot trước khi tạo Employee không có `teamId`.

## Đồng bộ enum Role

PostgreSQL có thể giữ `user_role_role_check` từ phiên bản Role cũ. Khi thêm role mới như `FINANCE`, Hibernate `ddl-auto=update` không tự mở rộng constraint và database sẽ từ chối bản ghi.

Migration xóa constraint cũ và tạo lại với toàn bộ role hiện tại:

```sql
ALTER TABLE user_role DROP CONSTRAINT IF EXISTS user_role_role_check;

ALTER TABLE user_role ADD CONSTRAINT user_role_role_check CHECK (role IN (
    'ADMIN', 'DIRECTOR', 'FACTORY_MANAGER', 'DEPARTMENT_MANAGER',
    'FINANCE', 'PRODUCTION_MANAGER', 'TEAM_LEADER', 'EMPLOYEE'
));
```
