# FactoryManager

FactoryManager là hệ thống quản lý dữ liệu hiệu suất nhà máy, hướng tới luồng dữ liệu có kiểm soát:
nhập liệu hoặc import → staging/validation → chốt theo phạm vi → KPI snapshot → dashboard và drill-down
có audit.

Repository hiện có một bản triển khai Spring Boot + PostgreSQL và React. Các chức năng nền như đăng nhập,
master data, staging, import Excel và dashboard đơn giản đã có, nhưng workflow đóng kỳ, correction/versioning,
KPI Dictionary, snapshot và một số acceptance Must vẫn chưa hoàn chỉnh. Không xem trạng thái hiện tại là
MVP hoặc production-ready.

## Đọc tài liệu

- [Mô tả dự án](MO_TA_DU_AN.md)
- [PRD](PRD_He_thong_hieu_suat_nha_may.md)
- [TDD](TDD_He_thong_hieu_suat_nha_may.md)
- [Kiến trúc hệ thống](ARCHITECTURE.md)
- [Hợp đồng API](API_Contracts.md)
- [Mục lục tài liệu](docs/index.md)
- [Traceability Must](docs/release/RELEASE_MUST_TRACEABILITY.md)
- [Trạng thái đã kiểm chứng](docs/release/FINAL_STATUS.md)

PRD/TDD mô tả trạng thái đích. Code, config và test mô tả trạng thái hiện tại. Khi hai phía khác nhau,
gap được ghi trong traceability và backlog; không mặc định code đã đáp ứng chỉ vì có endpoint cùng tên.

## Cấu trúc repository

```text
FactoryManager/
├── factory-management/           # Spring Boot REST API
├── factory-management-frontend/  # React + TypeScript + Vite
├── docs/                         # UI map và release evidence
├── content-briefs/               # Mẫu nghiệp vụ cần đặc tả sâu
└── *.md, *.yaml, *.csv           # Tài liệu nguồn và baseline
```

Stack hiện tại:

- Backend: Java 17, Spring Boot 4.1, Spring Security, JPA, PostgreSQL, Apache POI.
- Frontend: React 19, TypeScript 5.8, Vite 6, React Router, Recharts.
- API base mặc định: `http://localhost:8080/factory-management/api/v1`.

## Chạy local

### 1. PostgreSQL

Tạo database `factorymanagement`, sau đó cung cấp cấu hình bằng biến môi trường. Không ghi secret vào
repository hoặc commit `.env`.

Ví dụ PowerShell cho phiên terminal hiện tại:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/factorymanagement'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<local-password>'
$env:JWT_SIGNER_KEY = '<random-secret-at-least-64-bytes>'
$env:ADMIN_BOOTSTRAP_ENABLED = 'false'
```

Cấu hình hiện tại còn giá trị fallback dành cho local. Không dùng các fallback đó khi triển khai shared,
staging hoặc production.

Với database local rỗng, có thể bật bootstrap đúng một lần bằng `ADMIN_BOOTSTRAP_ENABLED=true` và đồng thời
đặt `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_EMPLOYEE_CODE` bằng giá trị local riêng. Sau khi đăng nhập và
đổi mật khẩu, tắt bootstrap cho các lần chạy sau. Không dùng credential fallback trong source.

### 2. Backend

```powershell
cd factory-management
.\mvnw.cmd spring-boot:run
```

Backend chạy ở `http://localhost:8080/factory-management`.

### 3. Frontend

Mở terminal khác:

```powershell
cd factory-management-frontend
pnpm.cmd install
$env:VITE_API_URL = 'http://localhost:8080/factory-management/api/v1'
pnpm.cmd dev -- --host localhost --port 5173 --strictPort
```

Mở `http://localhost:5173`. Backend hiện chỉ cho phép CORS từ
`localhost:5173` và `127.0.0.1:5173`; nếu port này bận, giải phóng port hoặc
thay đổi CORS có chủ đích trước khi chạy. Tài khoản phải được tạo từ bootstrap
có kiểm soát hoặc dữ liệu local; không lưu credential thật trong tài liệu.

## Kiểm tra

Backend:

```powershell
cd factory-management
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

`test` hiện cần PostgreSQL tại URL cấu hình vì repository chưa có test profile tự cô lập. Nếu database không
chạy, test `contextLoads` sẽ lỗi kết nối; đây là giới hạn đã biết, không phải một test suite nghiệp vụ đầy đủ.

Frontend:

```powershell
cd factory-management-frontend
pnpm.cmd run lint
pnpm.cmd run build
```

Tại lần kiểm tra gần nhất, production build thành công nhưng lint còn lỗi. Xem
[FINAL_STATUS](docs/release/FINAL_STATUS.md) để biết bằng chứng và gap mới nhất.

## Luồng demo hiện tại

Luồng có thể trình diễn:

1. Đăng nhập.
2. Quản trị master data.
3. Tổ trưởng tạo báo cáo staging và dữ liệu downtime/chất lượng/vật tư/nhân sự đi kèm.
4. Gửi duyệt và quản lý approve thành production report.
5. Xem dashboard cơ bản theo scope sau khi xác nhận request dashboard trả thành công.

Luồng này chưa tương đương closure workflow trong PRD. Đặc biệt, approve từng record chưa cung cấp
coverage theo ngày/scope, reopen/reclose hoặc official version history.

Tên role/menu trong demo là Current (`DIRECTOR`, `FACTORY_MANAGER`, `PRODUCTION_MANAGER`,
`TEAM_LEADER`...). Target tài liệu dùng ba cấp `Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`,
coi Ca trưởng/Tổ trưởng là chức danh nhân sự và dùng các tab module chuẩn trong PRD 4.4. Chưa đổi code nên
không được trình bày tên target như đã triển khai.

## Giới hạn quan trọng

- Import Excel chưa hỗ trợ đúng partial success 95/5 và conflict workflow.
- Chưa có `businessDataCode`, phân loại Current/Historical Backfill/Late Arrival/Correction,
  cảnh báo gần trùng từ 90% đến dưới 100% hoặc reconciliation run Excel–web/OCR.
- Chưa có `companyId` xuyên suốt dữ liệu.
- Chưa có closure/correction/versioning theo PRD.
- KPI được tính trực tiếp trong service, chưa theo KPI Dictionary có version.
- Dashboard chưa đọc từ KPI snapshot và chưa có drill-down contributor đầy đủ.
- Dashboard chưa có catalog tối thiểu tám view cốt lõi với shared filter/status contract.
- Trang `Reports` hiện gọi `GET /production-reports/search/my-scope`, nhưng
  backend không có route này; không dùng trang đó làm bằng chứng demo cho tới
  khi client/server contract được sửa.
- Tổng quan hiện có thể biến lỗi hoặc dữ liệu thiếu thành `0`; chỉ trình bày KPI
  sau khi xác nhận request backend thành công và payload thật sự có dữ liệu.
- Nhân sự/vật tư chỉ thuộc lõi ở phạm vi vận hành; chấm công/lương/KPI cá nhân, tài chính,
  kho đầy đủ, bảo trì đầy đủ và chatbot nằm ngoài Must hoặc cần capability riêng.
- Migration, test isolation, security hardening và observability chưa đạt release gate.

Thứ tự khắc phục được quản lý trong
[Backlog hệ thống hiệu suất nhà máy](Backlog_He_thong_hieu_suat_nha_may.md).
