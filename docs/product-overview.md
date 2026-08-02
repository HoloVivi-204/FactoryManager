# Mô tả dự án Factory Performance

## 1. Mục đích tài liệu

Factory Performance là phần sản phẩm theo dõi hiệu suất vận hành nhà máy trong repository
FactoryManager. Sản phẩm tập trung vào một chuỗi dữ liệu có kiểm soát:

```text
nhập tay hoặc import
  → staging
  → kiểm tra và xử lý xung đột
  → chốt theo ngày/phạm vi
  → dữ liệu official có phiên bản
  → KPI snapshot
  → dashboard và drill-down về nguồn
```

Tài liệu này mô tả cả hai lớp nhưng không trộn lẫn:

- **Target** là hành vi phải đạt theo PRD v2.5 và thiết kế kỹ thuật đích theo TDD v1.5.
- **Current** là những gì có bằng chứng trong source code tại thời điểm đọc repository.
- Một class, endpoint hoặc màn hình tồn tại chưa đủ để kết luận một yêu cầu đã hoàn tất.
  Trạng thái chi tiết và bằng chứng nằm trong
  [feature-matrix.csv](feature-matrix.csv).

## 2. Nguồn chuẩn và thứ tự ưu tiên

1. [PRD v2.5](prd.md) quyết định nhu cầu người dùng,
   phạm vi, business rule và acceptance criteria.
2. [TDD v1.5](technical-design.md) quyết định hướng triển khai kỹ thuật.
3. Quyết định đã duyệt trong `docs/decisions.md` làm rõ các điểm PRD/TDD còn mở.
4. Code và test là bằng chứng cho trạng thái hiện tại, không tự thay đổi target.
5. File CSV là baseline traceability; backlog là thứ tự thực hiện, không phải nguồn định nghĩa
   sản phẩm.

Nếu code khác PRD, ghi nhận là gap. Nếu TDD khác PRD, phải chốt quyết định trước khi dùng TDD
làm cơ sở implementation.

## 3. Vấn đề cần giải quyết

Dữ liệu vận hành đang có nguy cơ phân tán giữa phiếu giấy, tin nhắn, file Excel và báo cáo của
từng tổ. Khi không có state model và nguồn sự thật chung:

- dữ liệu thiếu dễ bị hiểu thành số không;
- cùng một ca/máy có thể xuất hiện nhiều giá trị mà không biết nguồn nào thắng;
- dữ liệu lịch sử nhập bù có thể bị nhầm với dữ liệu hiện tại hoặc tạo bản ghi gần trùng;
- Excel từ máy quản lý, dữ liệu web và phiếu giấy có thể lệch nhau nhưng chưa có màn hình đối soát;
- số liệu vừa nhập có thể bị dùng như số liệu chính thức;
- sửa sau chốt có thể làm mất lịch sử;
- KPI cấp trên có thể cũ nhưng vẫn trông như đã cập nhật;
- người quản lý thấy KPI thấp nhưng không truy ngược được ca, máy, nguyên nhân và người xác nhận.

Factory Performance giải quyết vấn đề bằng provenance, staging, validation, closing, versioning,
snapshot và drill-down.

## 4. Giá trị sản phẩm

### Người nhập dữ liệu

- Nhập đúng ngày sản xuất, ca, tổ, máy và trạng thái vận hành trong phạm vi được giao.
- Thấy lỗi cụ thể trước khi gửi.
- Biết dữ liệu đang ở Draft, cần sửa, chờ chốt hay đã trở thành official.
- Biết record là Current, Historical Backfill, Late Arrival hay Correction và thấy cảnh báo gần trùng trước khi gửi.

### Quản lý vận hành

- Thấy máy/ca/tổ nào còn thiếu hoặc đang chặn chốt.
- Xử lý Warning và conflict bằng quyết định có lý do.
- Chốt phần đủ điều kiện mà không trình bày phần còn thiếu như kỳ hoàn chỉnh.
- Đối soát Excel–web, xem OCR như bằng chứng thứ yếu và resolve mismatch có lý do.

### Quản lý điều hành

- Xem KPI theo nhà máy, dây chuyền, tổ, máy, ca và thời gian.
- So sánh kỳ trên cùng phạm vi và bộ lọc.
- Drill-down từ KPI về official record, source record và audit summary.
- Duyệt mở lại khi phát hiện sai sau chốt.
- Xem nhiều biểu đồ theo cùng scope/filter mà không nhầm empty/partial/stale thành zero.

### Quản trị dữ liệu và kiểm toán

- Quản lý cấu hình, danh mục có ngày hiệu lực, mapping, validation rule và KPI Dictionary.
- Tra actor, thời điểm, before/after, lý do cho các hành động quan trọng.
- Không sửa hoặc xóa audit log qua luồng nghiệp vụ thông thường.

## 5. Phạm vi target

### MVP bắt buộc

- Tài khoản cá nhân, role và data scope được kiểm tra ở server.
- Deployment single-tenant cho một công ty; dữ liệu target vẫn có `companyId`.
- Cấu hình ngày sản xuất, timezone, lịch ca, thuật ngữ, mapping và validation/KPI policy.
- Navigation theo module với nhãn chuẩn; ba cấp quyền vận hành không phụ thuộc label tab.
- Danh mục nhà máy, dây chuyền, tổ, máy, ca, sản phẩm, vật tư, chức danh/phân công, định mức và nguyên nhân dừng.
- Effective dating cho danh mục và cơ cấu được dùng trong báo cáo lịch sử.
- Nhập tay và import file; import có lỗi một phần và chống trùng theo từng logical record.
- Phân loại Current/Historical Backfill/Late Arrival/Correction, business data code, cảnh báo gần trùng từ 90% đến dưới 100% và đối soát Excel–web.
- Staging với Error, Warning, Information, conflict group và provenance.
- Chốt hoàn chỉnh, chốt một phần, mở lại một cấp, correction workspace và chốt lại.
- Official record có version; bản cũ vẫn tra cứu được.
- KPI Dictionary được duyệt, formula type được kiểm soát, snapshot theo scope/kỳ/version.
- Dashboard theo quyền, bộ lọc, so sánh kỳ, tối thiểu tám view cốt lõi, highlight vấn đề và drill-down.
- Thông báo trạng thái import và blocker tại màn hình chốt.
- Audit cho mọi thay đổi dữ liệu, quyền, cấu hình, chốt và correction.
- Test nghiệp vụ, idempotency, locking, outbox, performance và backup/restore theo gate TDD.

### Sau MVP hoặc chỉ bật khi có quyết định riêng

- OCR phiếu giấy làm bằng chứng đối soát thứ yếu.
- Quản lý kho đầy đủ và Bảo trì & lập lịch đầy đủ.
- Voice/NLU.
- Export dashboard ra PDF/ảnh.
- Kết nối trực tiếp ERP/MES/HRM.
- Dự báo và khuyến nghị nâng cao.

Các capability trên không được dùng để đánh trượt MVP khi chưa bật. Khi được bật, guardrail về
quyền và audit trở thành bắt buộc.

### Ngoài phạm vi Factory Performance

- Chấm công, lương, nghỉ phép, tăng ca và kỷ luật.
- KPI hoặc bảng xếp hạng cá nhân.
- Mua hàng, định giá/kế toán kho, tài chính, công nợ và giá vốn; vật tư vận hành vẫn thuộc lõi.
- Quản lý bảo trì đầy đủ.
- Chatbot hỏi đáp tự do.

Repository hiện có code cho một số domain trên. Chúng là code hiện hữu của FactoryManager,
không phải lý do để mở rộng scope PRD hoặc chen chúng vào backlog Factory Performance.

## 6. Vai trò target

| Vai trò | Trách nhiệm chính |
|---|---|
| Quản lý điều hành | Xem công ty/nhà máy, theo dõi coverage, điều tra KPI, duyệt reopen |
| Quản lý vận hành | Review staging/đối soát, xử lý Warning/conflict, chốt trong phạm vi |
| Nhân viên vận hành | Nhập dữ liệu, xác nhận không sản xuất, theo dõi tổ/ca/máy được giao |
| Quản trị dữ liệu | Danh mục, effective dating, mapping, validation rule, KPI Dictionary |
| Quản trị hệ thống | Tài khoản, role, scope và chính sách bảo mật |
| Người kiểm toán | Tra audit và đối chiếu dữ liệu theo phạm vi chỉ đọc |

Mentor/Product approver là stakeholder phê duyệt nghiệp vụ, không phải runtime role mặc định.

Ba vai trò đầu tạo thành hierarchy vận hành. Ca trưởng/Tổ trưởng là chức danh/phân công có hiệu lực
trong Quản lý nhân sự, không phải tab hoặc security role. Quản trị dữ liệu/hệ thống/kiểm toán là quyền chức năng trực giao.

Navigation target: `Tổng quan điều hành`, `Quản lý sản xuất`, `Quản lý vận hành`, `Quản lý nhân sự`,
`Quản lý vật tư`, `Quản lý kho`, `Bảo trì & lập lịch`, `Dữ liệu & đối soát`. Hai module kho/bảo trì
chỉ hiện khi capability được bật.

## 7. Mô hình nghiệp vụ target

### Phân biệt dữ liệu không có

Các trạng thái sau không được tự biến thành `0`:

- chưa nhập;
- đang xử lý import;
- không có kế hoạch;
- không sản xuất đã xác nhận;
- mất mạng/chờ nhập bù;
- có staging nhưng chưa chốt;
- đã chốt nhưng snapshot đang cập nhật;
- xử lý thất bại.

### Chốt và completeness

- Error luôn chặn chốt.
- Warning chỉ được override bởi người có quyền và phải có lý do.
- Complete chỉ hợp lệ khi mọi scope con bắt buộc đã đủ dữ liệu hoặc được xác nhận không sản xuất.
- Partial phải nêu coverage và danh sách scope còn thiếu.
- Nếu capability đóng ngoại lệ chưa được release, kỳ thiếu giữ `pending_review`.

### Sửa sau chốt

```text
official current
  → reopen request
  → quản lý nhà máy duyệt
  → correction workspace
  → reclose tạo official version mới
  → version cũ thành superseded
  → snapshot trực tiếp và mọi ancestor thành stale
  → tính lại bottom-up
```

Dashboard tiếp tục dùng official version gần nhất trong lúc correction và phải gắn nhãn
“đang điều chỉnh”.

### Dữ liệu lịch sử và đối soát nguồn

```text
Excel máy quản lý + dữ liệu web
  → normalize + businessDataCode
  → Current/Historical Backfill/Late Arrival/Correction
  → exact dedup
  → warning gần trùng từ 90% đến dưới 100% (AI hỗ trợ, không tự merge)
  → Matched/Mismatch/Missing
  → tùy chọn OCR giấy thứ yếu
  → human resolution + audit
  → staging/close
```

Excel không tự thành official và OCR không tự tạo record. Cùng identity khác giá trị luôn vào review.

## 8. Kiến trúc target

TDD chọn hướng modular monolith cho nhóm nhỏ. Ranh giới logic cần giữ rõ:

- Auth & Access Control
- Company Configuration
- Master Data
- Ingestion
- Mapping & Normalization
- Validation & Staging
- Closing & Correction
- KPI Engine & Aggregation
- Dashboard Serving
- Audit
- Notification

Write path không ghi trực tiếp vào dashboard model. Import và aggregation được kích hoạt qua
outbox, có idempotency key và retry hữu hạn. Dashboard overview đọc snapshot/cache; drill-down
đọc contributor link rồi mới về official/source/audit.

## 9. Hiện trạng repository

### Nền tảng đang có

- Backend là Spring Boot, Spring Security, JPA và PostgreSQL; frontend là React, TypeScript và
  Vite. Xem [apps/backend/pom.xml](../apps/backend/pom.xml) và
  [apps/frontend/package.json](../apps/frontend/package.json).
- JWT login/refresh/logout, role, explicit data scope và nhiều kiểm tra `@PreAuthorize` đã có.
  Xem [SecurityConfig.java](../apps/backend/src/main/java/com/factory/management/common/config/SecurityConfig.java)
  và [AuthorizationScope.java](../apps/backend/src/main/java/com/factory/management/common/security/AuthorizationScope.java).
- CRUD/soft-delete cho nhiều danh mục đã có, nhưng model master data chưa đạt effective dating
  đầy đủ của PRD.
- Form staging, các detail downtime/chất lượng và approval thành production report đã có.
  Xem [ProductionReportStagingServiceImpl.java](../apps/backend/src/main/java/com/factory/management/modules/production/service/ProductionReportStagingServiceImpl.java).
- Import Excel có template, preview và hiển thị lỗi, nhưng hiện dùng mapping cột cố định và chỉ
  lưu khi toàn workbook không có lỗi.
  Xem [StagingReportExcelService.java](../apps/backend/src/main/java/com/factory/management/modules/production/service/StagingReportExcelService.java).
- Audit append-only và API tìm kiếm đã có nền móng, nhưng chưa phủ đủ các hành động PRD.
- Dashboard hiện tính trực tiếp từ production reports; chưa có KPI Dictionary, snapshot,
  contributor link hoặc freshness/completeness model.
  Xem [ProductionReportServiceImpl.java](../apps/backend/src/main/java/com/factory/management/modules/production/service/ProductionReportServiceImpl.java).

### Khoảng cách trọng yếu

- Chưa có `companyId` xuyên suốt, company settings và adapter mapping versioned.
- Chưa có validation finding theo severity, Warning override và conflict group.
- Chưa có source batch/record đúng contract TDD cho import vận hành async và row-level dedup.
- Chưa có closure state machine theo scope, hierarchical lock và outbox.
- Chưa có reopen/correction workspace/official versioning/reclose.
- Chưa có KPI definition version, snapshot, ancestor recalculation và contributor link.
- Role hiện tại chưa map đủ `DATA_ADMIN` và `AUDITOR`; một số role/domain vượt phạm vi PRD.
  Nhiều master-data GET mới chỉ yêu cầu đăng nhập, còn staging update chưa kiểm tra lại scope của
  team đích khi người dùng đổi quan hệ tổ chức.
- Chưa có bộ test đủ chứng minh acceptance; backend hiện chỉ có context-load test và frontend
  chưa có test script.
- Cấu hình phát triển còn fallback JWT/admin password trong
  [application.yaml](../apps/backend/src/main/resources/application.yaml), và schema đang dựa
  vào `ddl-auto: update`; cả hai phải được đóng trước production.

## 10. Cách đọc trạng thái hiện tại

Feature matrix và release traceability chỉ dùng bộ trạng thái chung sau:

| Trạng thái | Ý nghĩa |
|---|---|
| `Implemented` | Có code và bằng chứng kiểm tra phù hợp acceptance |
| `Partial` | Có một phần luồng nhưng chưa đạt toàn bộ acceptance |
| `Missing` | Chưa có implementation sử dụng được |
| `Blocked` | Chưa thể chốt vì quyết định PRD/TDD còn mở |
| `Out of scope` | Không thuộc release đang xét |
| `Unverified` | Có code nhưng chưa có bằng chứng kiểm tra đủ tin cậy |

Hành vi hiện tại trái target, capability điều kiện hoặc nội dung được hoãn được giải thích trong cột
ghi chú của ma trận; không tạo thêm trạng thái tùy ý.

Không đổi trạng thái thành “đạt” nếu chưa có test hoặc bằng chứng nghiệm thu phù hợp.

## 11. Điều kiện được xem là MVP hoàn chỉnh

- Mọi `Must` có implementation và evidence trong feature matrix.
- Các blocker nghiệp vụ liên quan đã được quyết định hoặc có giả định tạm được phê duyệt.
- Một KPI Dictionary được duyệt và đối chiếu bằng ví dụ tính tay.
- Import mẫu thật pass contract test và upload lại không tạo trùng.
- Complete close, partial close và correction pass E2E.
- Recalculation ở scope con làm stale và tính lại mọi ancestor.
- API không cho user vượt data scope.
- Audit phủ đủ hành động bắt buộc.
- Performance profile có số cụ thể và đạt p95 theo PRD.
- Backup/restore được diễn tập; RPO/RTO được chốt trước production.

## 12. Tài liệu liên quan

- [Backlog vertical slices](backlog.md)
- [Cây domain và stable IDs](../content/skeleton.yaml)
- [Feature matrix](feature-matrix.csv)
- `docs/architecture.md`
- `docs/api-contracts.md`
- `docs/decisions.md`
- `docs/exec-plans.md`
