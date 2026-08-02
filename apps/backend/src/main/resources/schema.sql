-- Đồng bộ schema cũ: Employee có thể chưa được phân vào Team khi mới tiếp nhận.
-- PostgreSQL giữ NOT NULL từ phiên bản Entity cũ và Hibernate ddl-auto=update
-- không tự động loại bỏ constraint này.
ALTER TABLE IF EXISTS employee
    ALTER COLUMN team_id DROP NOT NULL;
@@

-- Đồng bộ CHECK constraint của enum Role. Hibernate ddl-auto=update không tự
-- mở rộng constraint cũ khi bổ sung role mới như FINANCE.
ALTER TABLE IF EXISTS user_role
    DROP CONSTRAINT IF EXISTS user_role_role_check;
@@

ALTER TABLE IF EXISTS user_role
    ADD CONSTRAINT user_role_role_check CHECK (role IN (
        'ADMIN',
        'DIRECTOR',
        'FACTORY_MANAGER',
        'DEPARTMENT_MANAGER',
        'FINANCE',
        'PRODUCTION_MANAGER',
        'TEAM_LEADER',
        'EMPLOYEE'
    ));
@@

-- Các cột công nợ được thêm sau khi bảng financial_record có thể đã có dữ liệu.
ALTER TABLE IF EXISTS financial_record
    ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0;
@@
ALTER TABLE IF EXISTS financial_record
    ADD COLUMN IF NOT EXISTS due_date DATE;
@@
ALTER TABLE IF EXISTS financial_record
    ADD COLUMN IF NOT EXISTS counterparty VARCHAR(255);
@@
ALTER TABLE IF EXISTS financial_record
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID';
@@

-- Đồng bộ Department cũ với danh mục loại phòng ban cố định.
-- Cột được thêm ở dạng nullable trước để ứng dụng vẫn khởi động được nếu còn dữ liệu cũ chưa nhận diện được.
ALTER TABLE IF EXISTS department
    ADD COLUMN IF NOT EXISTS department_type VARCHAR(50);
@@

ALTER TABLE IF EXISTS department
    ALTER COLUMN code TYPE VARCHAR(100);
@@

DO $$
BEGIN
    IF to_regclass('public.department') IS NOT NULL THEN
        UPDATE department
        SET department_type = CASE
            WHEN UPPER(code) = 'SX' OR UPPER(code) LIKE '%-SX' OR LOWER(name) LIKE '%sản xuất%' THEN 'PRODUCTION'
            WHEN UPPER(code) = 'QA' OR UPPER(code) LIKE '%-QA' OR LOWER(name) LIKE '%chất lượng%' THEN 'QUALITY'
            WHEN UPPER(code) = 'BT' OR UPPER(code) LIKE '%-BT' OR LOWER(name) LIKE '%bảo trì%' THEN 'MAINTENANCE'
            WHEN UPPER(code) = 'KHO' OR UPPER(code) LIKE '%-KHO' OR LOWER(name) LIKE '%kho%' THEN 'WAREHOUSE'
            WHEN UPPER(code) = 'TC' OR UPPER(code) LIKE '%-TC' OR LOWER(name) LIKE '%tài chính%' THEN 'FINANCE'
            WHEN UPPER(code) = 'NS' OR UPPER(code) LIKE '%-NS' OR LOWER(name) LIKE '%nhân sự%' THEN 'HUMAN_RESOURCES'
            ELSE department_type
        END
        WHERE department_type IS NULL;

        UPDATE department AS d
        SET code = UPPER(f.code) || '-' || CASE d.department_type
                WHEN 'PRODUCTION' THEN 'SX'
                WHEN 'QUALITY' THEN 'QA'
                WHEN 'MAINTENANCE' THEN 'BT'
                WHEN 'WAREHOUSE' THEN 'KHO'
                WHEN 'FINANCE' THEN 'TC'
                WHEN 'HUMAN_RESOURCES' THEN 'NS'
            END,
            name = CASE d.department_type
                WHEN 'PRODUCTION' THEN 'Xưởng sản xuất'
                WHEN 'QUALITY' THEN 'Phòng quản lý chất lượng'
                WHEN 'MAINTENANCE' THEN 'Phòng bảo trì'
                WHEN 'WAREHOUSE' THEN 'Bộ phận kho'
                WHEN 'FINANCE' THEN 'Phòng tài chính kế toán'
                WHEN 'HUMAN_RESOURCES' THEN 'Phòng nhân sự'
            END,
            description = COALESCE(NULLIF(BTRIM(d.description), ''), CASE d.department_type
                WHEN 'PRODUCTION' THEN 'Phụ trách tổ chức, vận hành và giám sát hoạt động sản xuất.'
                WHEN 'QUALITY' THEN 'Kiểm soát chất lượng nguyên vật liệu, quy trình sản xuất và thành phẩm.'
                WHEN 'MAINTENANCE' THEN 'Bảo trì, sửa chữa máy móc và thiết bị của nhà máy.'
                WHEN 'WAREHOUSE' THEN 'Quản lý nhập, xuất, lưu trữ và tồn kho vật tư, thành phẩm.'
                WHEN 'FINANCE' THEN 'Quản lý chi phí, doanh thu, công nợ và báo cáo tài chính.'
                WHEN 'HUMAN_RESOURCES' THEN 'Quản lý tuyển dụng, hồ sơ, chấm công và chính sách nhân sự.'
            END)
        FROM factory AS f
        WHERE d.factory_id = f.id
          AND d.department_type IS NOT NULL
          AND NOT EXISTS (
              SELECT 1
              FROM department AS duplicate
              WHERE duplicate.factory_id = d.factory_id
                AND duplicate.department_type = d.department_type
                AND duplicate.id <> d.id
          );

        IF NOT EXISTS (
            SELECT 1
            FROM department
            WHERE department_type IS NULL
        ) THEN
            ALTER TABLE department
                ALTER COLUMN department_type SET NOT NULL;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'department_department_type_check'
        ) THEN
            ALTER TABLE department
                ADD CONSTRAINT department_department_type_check CHECK (department_type IN (
                    'PRODUCTION',
                    'QUALITY',
                    'MAINTENANCE',
                    'WAREHOUSE',
                    'FINANCE',
                    'HUMAN_RESOURCES'
                ));
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uk_department_factory_type'
        ) AND NOT EXISTS (
            SELECT 1
            FROM department
            WHERE department_type IS NOT NULL
            GROUP BY factory_id, department_type
            HAVING COUNT(*) > 1
        ) THEN
            ALTER TABLE department
                ADD CONSTRAINT uk_department_factory_type UNIQUE (factory_id, department_type);
        END IF;
    END IF;
END $$;
@@
