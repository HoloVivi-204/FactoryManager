# TECHNICAL DESIGN DOCUMENT (TDD)
## Hệ thống theo dõi hiệu suất nhà máy

| Thông tin | Nội dung |
|---|---|
| Phiên bản | 1.5 |
| Ngày cập nhật | 01/08/2026 |
| Trạng thái | Bản nháp kỹ thuật - phụ thuộc PRD v2.5 |
| PRD liên quan | [PRD_He_thong_hieu_suat_nha_may.md](./PRD_He_thong_hieu_suat_nha_may.md) |
| Mục tiêu | Mô tả kiến trúc kỹ thuật, mô hình dữ liệu, API, pipeline, adapter tích hợp và chiến lược mở rộng |
| Đội ngũ & timeline | Dự án dài hạn, phát triển bởi 3 người, tiếp tục sau kỳ thực tập theo nhịp độ tăng dần từng ngày (xem PRD mục 1 và mục 15). Các quyết định kỹ thuật trong TDD nên ưu tiên phương án đơn giản, dễ bảo trì bởi nhóm nhỏ hơn là phương án tối ưu cho quy mô lớn ngay từ đầu — xem 3.1 (modular monolith). |

### Lịch sử phiên bản

| Phiên bản | Thay đổi chính |
|---|---|
| 1.2 | Bản đầy đủ đầu tiên: kiến trúc, module, luồng dữ liệu, state machine, mô hình dữ liệu, API, job queue, KPI engine, cache/index/partitioning, bảo mật, testing, deployment, ADR. |
| 1.3 | Sửa 11.3 để tính lại KPI lan lên đúng các scope tổ tiên (tránh dashboard cấp trên hiển thị "trông như mới" nhưng thực chất stale); thêm 12.4 chính sách vòng đời/lưu trữ dài hạn (archival, cold tier) cho dữ liệu thô và file gốc; thêm ADR-12; ghi chú API document riêng sẽ tách ra sau; bổ sung bối cảnh đội 3 người/dự án dài hạn. |
| 1.4 | Sửa tham chiếu phiên bản PRD bị lệch ở mục 22 (Technical Acceptance Gates) — ghi cứng "PRD v2.2" trong khi PRD đã lên v2.4; đổi sang tham chiếu không hardcode số phiên bản để tránh lệch lại mỗi lần PRD được cập nhật. |
| 1.5 | Đồng bộ PRD v2.5: navigation theo module, ba cấp quyền vận hành, phân loại dữ liệu lịch sử, pipeline cảnh báo tương đồng 90–100%, đối soát Excel–web–OCR và contract nhiều biểu đồ. |

---

## 1. Phạm Vi TDD

TDD này mô tả cách triển khai các yêu cầu trong PRD. Những nội dung sau thuộc TDD, không nên đặt trong PRD:

- Module kỹ thuật và trách nhiệm từng module.
- API, database schema, indexing, partitioning, cache, job queue.
- Cách xử lý import, validation, aggregation, audit, notification.
- Chiến lược mapping/adapter để triển khai cho nhiều công ty sau MVP.
- Công nghệ tham khảo, vận hành, bảo mật, test và deployment.

Nguyên tắc: PRD trả lời "người dùng cần gì và quy tắc nghiệp vụ là gì"; TDD trả lời "hệ thống làm điều đó bằng cách nào".

---

## 2. Mục Tiêu Kỹ Thuật

- MVP triển khai single-tenant cho một công ty; data model có `companyId` để chuẩn bị mở rộng.
- Giữ lõi hệ thống ổn định khi triển khai cho công ty khác sau này.
- Đưa khác biệt của từng công ty vào cấu hình, mapping, KPI definition và adapter thay vì sửa code lõi.
- Tách đường ghi dữ liệu khỏi đường đọc dashboard.
- Dashboard tổng quan đọc dữ liệu đã tổng hợp, không quét dữ liệu thô cho mỗi request.
- Mọi dữ liệu official có nguồn gốc và audit trail.
- Hỗ trợ sửa sau chốt bằng versioning, không ghi đè mất lịch sử.
- Có cơ chế idempotency để import lại file hoặc chạy lại job không làm nhân đôi dữ liệu.
- Dễ test KPI bằng ví dụ tính tay đã được phê duyệt.

---

## 3. Kiến Trúc Tổng Quan

### 3.1. Mô hình triển khai đề xuất

```
Web App
  |
  v
API Gateway / Backend API
  |
  +-- Auth & Access Control
  +-- Company Configuration
  +-- Master Data
  +-- Ingestion
  +-- Reconciliation & Similarity
  +-- Validation & Staging
  +-- Closing & Correction
  +-- Dashboard Serving
  +-- Audit
  +-- Notification
  |
  v
Database + Cache + Job Queue + Object Storage
  |
  v
Background Workers
  +-- Import Worker
  +-- OCR Worker (optional)
  +-- KPI Aggregation Worker
  +-- Notification Worker
  +-- Export Worker (optional)
```

MVP có thể triển khai dưới dạng modular monolith để giảm chi phí vận hành. Ranh giới module vẫn phải rõ để sau này có thể tách worker hoặc service riêng khi tải tăng.

### 3.2. Công nghệ tham khảo

Đây là đề xuất kỹ thuật, không phải yêu cầu nghiệp vụ:

| Thành phần | Đề xuất |
|---|---|
| Frontend | React hoặc framework tương đương |
| Backend | Node.js/NestJS hoặc Java Spring Boot |
| Package manager nếu dùng Node.js | pnpm |
| Database | PostgreSQL ưu tiên vì hỗ trợ tốt partitioning, JSONB, index và transaction |
| ORM/query builder | Prisma, TypeORM, Kysely hoặc tương đương; không viết raw SQL trong business logic trừ migration/index chuyên biệt |
| Queue | BullMQ/Redis, RabbitMQ hoặc dịch vụ queue tương đương |
| Cache | Redis hoặc cache tương đương |
| Object storage | S3-compatible storage hoặc filesystem nội bộ cho file upload tùy môi trường |
| Auth | JWT hoặc session-based auth; refresh/session policy cấu hình theo công ty |
| Monitoring | Structured logging, metrics, tracing cơ bản, dashboard job failures |

Nếu công ty có stack chuẩn riêng, TDD phải được điều chỉnh bằng ADR trước khi triển khai.

---

## 4. Chiến Lược Mở Rộng Sang Nhiều Công Ty

Mục tiêu thực tế không phải là "cắm vào mọi công ty không cần chỉnh gì". Mục tiêu là: với các công ty có bài toán theo dõi vận hành tương tự, phần lõi không đổi; khác biệt được xử lý bằng cấu hình và adapter.

TDD phân biệt ba mức mở rộng:

| Mức | Phạm vi | Trạng thái |
|---|---|---|
| Một công ty, một nhà máy | MVP mặc định | Trong phạm vi |
| Một công ty, nhiều nhà máy | Cùng cấu hình company, nhiều factory/scope | Chuẩn bị trong mô hình dữ liệu |
| Nhiều công ty dùng chung một instance | Multi-tenant vận hành, cách ly dữ liệu, billing/onboarding tenant | Ngoài MVP, cần ADR riêng |

Trong MVP, `companyId` xuất hiện trong schema để tránh phải migrate lớn về sau, nhưng không có màn hình quản trị tenant và không cam kết vận hành nhiều công ty trên cùng instance.

### 4.1. Ba lớp hệ thống

| Lớp | Nội dung | Có sửa theo công ty không? |
|---|---|---|
| Core Platform | Auth, master data, staging, closing, audit, KPI engine, dashboard, notification | Hạn chế sửa |
| Company Configuration | Cơ cấu nhà máy, ca, ngày sản xuất, KPI, ngưỡng, thuật ngữ, quyền, retention | Có, qua UI/admin config |
| Data Adapters | Mapping file, API connector, ERP/MES sync, OCR template | Có, theo từng nguồn dữ liệu |

### 4.2. Các điểm cần cấu hình cho mỗi công ty

- Company, factory, line, group hierarchy.
- Production day rule, timezone, shift calendar.
- Machine list, machine effective assignment, machine standard speed/capacity.
- Product list nếu KPI phụ thuộc sản phẩm.
- Downtime reason taxonomy.
- User roles and data scopes.
- Input file templates and column mappings.
- Validation thresholds and severity.
- KPI definitions, aggregation rules and color thresholds.
- Audit retention and export policy.
- Notification deadlines and escalation rules.

### 4.3. Adapter contract

Mỗi adapter nguồn dữ liệu phải cung cấp:

| Thành phần | Mô tả |
|---|---|
| Adapter ID | Mã duy nhất theo công ty và nguồn dữ liệu |
| Source type | `web_manual`, `manager_excel`, `paper_ocr`, API, database sync, backfill hoặc correction |
| Mapping definition | Map dữ liệu nguồn sang canonical model |
| Validation pre-check | Kiểm tra định dạng, cột bắt buộc, kiểu dữ liệu |
| Idempotency key | Cách nhận diện cùng một batch hoặc record đã được xử lý trước đó |
| Business-time mapping | Cách lấy production date/source occurred time để server derive `dataTimeClass` |
| Reconciliation role | Primary input hoặc secondary evidence; `paper_ocr` luôn là secondary trong baseline này |
| Error reporting | Cách trả lỗi theo dòng/trường |
| Version | Phiên bản adapter để truy vết khi mapping thay đổi |

Ví dụ nguyên nhân cần adapter: công ty A gọi cột sản lượng là `actual_output`; công ty B gọi là `SL thực tế`; công ty C lấy qua API `quantityActual`. Core platform chỉ xử lý canonical field `actualOutput`.

### 4.4. Quy trình onboarding công ty mới

1. Khảo sát cơ cấu nhà máy, ca làm việc, KPI, nguồn dữ liệu.
2. Tạo company configuration.
3. Import master data ban đầu.
4. Cấu hình mapping file/API.
5. Cấu hình validation thresholds.
6. Cấu hình KPI Dictionary.
7. Chạy dữ liệu mẫu và đối chiếu với tính tay.
8. Pilot với một dây chuyền hoặc một tổ.
9. Khóa cấu hình đã được phê duyệt và mở rộng phạm vi.

---

## 5. Module Và Trách Nhiệm

| Module | Trách nhiệm | Không được làm |
|---|---|---|
| Auth & Access Control | Đăng nhập, session/token, role, scope, kiểm tra quyền server-side | Không chỉ dựa vào UI để bảo vệ dữ liệu |
| Company Configuration | Cấu hình công ty, ca, ngày sản xuất, thuật ngữ, KPI, validation rules | Không chứa dữ liệu vận hành phát sinh |
| Master Data | Quản lý factory, line, group, machine, standards, downtime reasons, effective dates | Không xóa cứng dữ liệu đã có lịch sử |
| Ingestion | Nhận form/file/OCR/API, tạo source batch, lưu file gốc, đưa job nền | Không ghi thẳng vào official data |
| Mapping & Normalization | Chuyển dữ liệu nguồn sang canonical model | Không tự quyết định dữ liệu thắng khi xung đột nghiệp vụ |
| Reconciliation & Similarity | Phân loại thời gian, tạo business data code, đối soát web/Excel/OCR, chấm điểm ứng viên gần trùng | Không tự merge, xóa, ghi đè hoặc chọn nguồn thắng từ điểm AI |
| Validation & Staging | Kiểm tra Error/Warning/Info, tạo staging records, conflict groups | Không tính KPI chính thức |
| Closing & Correction | Chốt, chốt một phần, mở lại, chốt lại, version official data | Không bỏ qua audit log |
| KPI Engine | Tính KPI từ official data theo KPI Dictionary | Không hardcode công thức chưa được phê duyệt |
| Aggregation | Tạo KPI snapshots theo grain và scope | Không phục vụ request dashboard trực tiếp từ dữ liệu thô |
| Dashboard Serving | Trả dữ liệu tổng quan, filter, comparison, drill-down | Không bỏ qua phân quyền dữ liệu |
| Notification | Nhắc thiếu dữ liệu, chốt trễ, job fail, escalation | Không gửi dữ liệu nhạy cảm không cần thiết |
| Audit | Ghi và truy xuất audit log | Không cho sửa/xóa log qua luồng nghiệp vụ thường |
| Export | Tạo PDF/ảnh/report async nếu bật | Không xuất dữ liệu ngoài quyền |
| Voice/NLU | Optional, map voice command thành filter dashboard | Không truy cập dữ liệu trực tiếp |

Navigation target là một capability catalog ổn định với các key `executive-overview`, `production-management`, `operations-management`, `people-management`, `materials-management`, `warehouse-management`, `maintenance-scheduling` và `data-reconciliation`. Label tiếng Việt lấy từ cấu hình đã duyệt trong PRD 4.4; backend vẫn kiểm tra action + scope cho API, không dùng việc ẩn/hiện tab làm authorization.

---

## 6. Luồng Dữ Liệu Kỹ Thuật

### 6.1. Nhập tay

```
Web Form
  -> API validates permission
  -> Normalize to canonical input
  -> Run validation rules
  -> Create staging record
  -> Write audit log
```

### 6.2. Import file

```
Upload file
  -> Store original file as temporary object
  -> Begin transaction
  -> Create source batch with file key
  -> Write outbox event: import_requested
  -> Commit transaction
  -> Mark file object active and linked to source batch as lifecycle metadata
  -> Return "received"

Outbox worker
  -> Trigger import processing

Import Worker
  -> Load source batch and file key
  -> Retry with backoff if file object is temporarily unreadable
  -> Parse file with adapter mapping
  -> Classify Current/Historical Backfill/Late Arrival/Correction
  -> Build businessDataCode or event matching metadata
  -> Validate each row
  -> Run exact dedup, then near-duplicate candidate scoring
  -> Create staging records for valid rows
  -> Create row-level errors for invalid rows
  -> Detect conflicts
  -> Notify uploader
```

#### 6.2.1. Phân loại lịch sử và cảnh báo gần trùng

```text
canonical source record
  -> derive dataTimeClass from productionDate + receivedAt + closure/correction state
  -> exact identity/dedup check
  -> candidate retrieval inside same company + record type + bounded business window
  -> versioned similarity scorer
  -> score 0.90..<1.00: create review warning with feature explanation
  -> same identity/different value: create conflict regardless of score
  -> persist candidate, score, rule/model version and review decision
```

Không gửi toàn bộ file hoặc PII đến model ngoài hệ thống nếu chưa có quyết định bảo mật riêng. Candidate retrieval phải giới hạn trước bằng field canonical để không so sánh mọi record với mọi record. Điểm số là advisory metadata; decision service chỉ cho phép `link_existing`, `keep_separate`, `create_conflict` hoặc `correct_candidate` khi actor có quyền và nhập lý do phù hợp.

#### 6.2.2. Đối soát Excel–web–OCR

```text
Excel source batch + web/manual source records
  -> group by businessDataCode/logical identity
  -> compare canonical fields
  -> create reconciliation run/items

optional paper image
  -> OCR worker -> field values + confidence + bounding evidence
  -> attach as secondary candidate to reconciliation item

reviewer
  -> inspect Matched/Mismatch/Missing/OCR Low Confidence
  -> resolve with reason
  -> validation/staging continues
  -> audit + outbox notification in the same transaction
```

Excel từ máy quản lý và dữ liệu web là nguồn đầu vào chính nhưng vẫn chưa official trước khi chốt. OCR không gọi thẳng Closing hoặc Official repository. Nếu OCR chưa bật, reconciliation run vẫn hoàn tất cho cặp Excel–web.

### 6.3. Chốt dữ liệu

```
User requests close scope
  -> Check permission
  -> Check required coverage
  -> Block if Error exists
  -> Require reasons for Warning overrides
  -> Create official records/version
  -> Mark closure status
  -> Write audit log
  -> Write outbox event: kpi_aggregation_requested
  -> Commit transaction

Outbox worker
  -> Trigger KPI aggregation
```

### 6.4. Sửa sau chốt

```
Create reopen request
  -> Approve request
  -> Create correction workspace referencing the current official version
  -> Keep current official version active for dashboard
  -> Apply changes only inside correction workspace
  -> Create new official version after reclose
  -> Mark previous version as superseded
  -> Mark report period as adjusted
  -> Write audit log
  -> Write outbox event: kpi_aggregation_requested
  -> Commit transaction

Outbox worker
  -> Trigger KPI aggregation for affected scope
```

### 6.5. Dashboard và drill-down

```
Dashboard request
  -> Check permission and scope
  -> Read KPI snapshots/cache for overview
  -> Return lastUpdated and dataFreshness status

Drill-down request
  -> Check permission and scope
  -> Read linked aggregate contributors
  -> Fetch official records/source metadata/audit summary
```

---

## 7. State Machines

### 7.1. Source batch

| State | Trigger |
|---|---|
| received | File/API/OCR input accepted |
| queued | Job enqueued |
| processing | Worker started |
| partially_succeeded | Some rows succeeded, some failed |
| succeeded | All processable rows succeeded |
| failed | Batch failed before row-level processing or unrecoverable error |
| cancelled | User/admin cancelled before processing |

### 7.2. Staging record

| State | Trigger |
|---|---|
| draft | User not submitted yet |
| valid | Passed validation with no blocking issue |
| needs_review | Has warning or conflict |
| rejected | Has blocking error |
| ready_to_close | Reviewed and eligible |
| closed | Converted into official record |
| superseded | Replaced before close due to conflict resolution |

### 7.3. Production day closure

| State | Meaning |
|---|---|
| open | Still accepting normal input |
| pending_review | Missing data, warnings or conflicts exist |
| partially_closed | Some scopes closed, others still open |
| closed_complete | Scope fully closed with required coverage |
| closed_with_exceptions | Scope closed with approved missing/late/offline exceptions |
| reopened | Closed scope opened for correction |
| adjusted | Closed again after correction |

### 7.4. KPI snapshot

| State | Meaning |
|---|---|
| pending | Aggregation job waiting |
| calculating | Worker calculating |
| current_complete | Snapshot is current and based on complete closed data |
| current_partial | Snapshot is current but based on partial/exception closure |
| stale | Official data changed, snapshot needs recalculation |
| failed | Aggregation failed |

---

## 8. Mô Hình Dữ Liệu Đề Xuất

Tên bảng dùng snake_case, số nhiều. Chi tiết kiểu dữ liệu sẽ được chốt trong schema/migration.

### 8.1. Canonical data model sơ bộ

Các adapter nguồn dữ liệu phải map về các field canonical. Đây là schema nghiệp vụ tối thiểu, chưa phải migration cuối cùng.

| Entity | Field chính | Ghi chú |
|---|---|---|
| Production output | companyId, productionDate, shiftId, machineId, workGroupId, productId, businessDataCode, dataTimeClass, plannedQuantity, actualQuantity, goodQuantity, defectQuantity, sourceRecordId | Một record chứa cả bộ chỉ số sản lượng/chất lượng của cùng máy/sản phẩm/ca. |
| Machine state interval | companyId, machineId, productionDate, shiftId, startTime, endTime, stateType, downtimeReasonId, sourceRecordId | Thời gian dùng để matching ứng viên trùng, không dùng làm identity ổn định sau khi sửa. |
| Incident | companyId, machineId, productionDate, shiftId, incidentStartTime, incidentEndTime, incidentType, severity, description, sourceRecordId | Sự cố kéo dài qua ca cần rule split hoặc giữ một incident có nhiều affected shifts. |
| Quality record | companyId, productionDate, shiftId, machineId, productId, defectType, defectQuantity, sourceRecordId | Phục vụ KPI chất lượng nếu được bật. |
| Production plan | companyId, productionDate, shiftId, machineId, productId, plannedQuantity, planVersion, effectiveFrom | Logical identity không chứa version; chỉ một version được current/effective tại một thời điểm. |

### 8.2. Tenant/company và cấu hình

| Bảng | Mục đích |
|---|---|
| companies | Công ty/tenant |
| company_settings | Timezone, ngày sản xuất, thuật ngữ, policy |
| production_calendars | Lịch sản xuất và ngày nghỉ |
| shifts | Ca làm việc, giờ bắt đầu/kết thúc, quy tắc ca đêm |
| validation_rules | Rule Error/Warning/Info theo company/scope |
| input_templates | Template file/API/OCR |
| input_template_fields | Mapping field nguồn sang canonical field |
| module_catalog | Key/label/order/capability của các tab chuẩn trong PRD 4.4 |
| module_access_policies | Action và role tối thiểu để thấy module; không thay data-scope authorization |
| similarity_policies | Threshold, feature weights/rule version và effective date; baseline cảnh báo `0.90` |

### 8.3. Master data có hiệu lực thời gian

| Bảng | Mục đích |
|---|---|
| factories | Nhà máy |
| production_lines | Dây chuyền/khu vực |
| work_groups | Tổ/nhóm |
| machines | Máy |
| machine_assignments | Máy thuộc dây chuyền/tổ theo thời gian |
| machine_standards | Định mức/tốc độ/công suất theo thời gian |
| products | Sản phẩm nếu cần |
| downtime_reasons | Nguyên nhân dừng máy |
| downtime_reason_versions | Phân loại nguyên nhân theo thời gian |
| employee_positions | Chức danh nhân sự như Ca trưởng/Tổ trưởng, không phải security role |
| employee_position_assignments | Phân công chức danh theo `validFrom`/`validTo` và phạm vi tổ/ca |

Quy tắc constraint:

- Các khoảng hiệu lực của cùng một machine assignment không được overlap trong cùng relation type.
- Các khoảng hiệu lực của cùng một machine standard không được overlap nếu cùng machine/product/standard type.
- Không hard delete master data đã được tham chiếu bởi source, staging, official hoặc audit records.

### 8.4. Người dùng và phân quyền

| Bảng | Mục đích |
|---|---|
| users | Tài khoản người dùng |
| roles | Vai trò |
| permissions | Quyền chi tiết |
| user_role_assignments | Gán role theo company |
| data_scope_assignments | Gán phạm vi factory/line/group |
| login_events | Đăng nhập, thất bại, khóa tài khoản |

Ba role vận hành target dùng stable code `EXECUTIVE_MANAGER`, `OPERATIONS_MANAGER`, `OPERATIONS_STAFF`. `DATA_ADMIN`, `SYSTEM_ADMIN` và `AUDITOR` là role/capability chức năng trực giao. Chức danh `SHIFT_LEADER`/`TEAM_LEADER` nằm ở `employee_position_assignments`; thay chức danh không tự thay `user_role_assignments`.

Mapping migration từ enum Current phải được chốt và test riêng: `DIRECTOR`/`FACTORY_MANAGER` là ứng viên cho `EXECUTIVE_MANAGER`; `DEPARTMENT_MANAGER`/`PRODUCTION_MANAGER` là ứng viên cho `OPERATIONS_MANAGER`; `TEAM_LEADER`/`EMPLOYEE` là ứng viên cho `OPERATIONS_STAFF`. Đây là mapping candidate theo scope hiện có, không phải phép đổi tên tự động; migration phải phát hiện user có nhiều role xung đột và không nâng quyền.

### 8.5. Dữ liệu nguồn, staging và official

| Bảng | Mục đích |
|---|---|
| source_batches | Một lần upload/import/OCR/API batch |
| source_records | Dòng hoặc record nguồn sau parse, gồm businessDataCode/dataTimeClass/sourceOccurredAt/receivedAt/model-template version |
| source_record_errors | Lỗi theo dòng/trường |
| staging_records | Dữ liệu tạm canonical |
| validation_findings | Error/Warning/Info gắn với staging record |
| conflict_groups | Nhóm xung đột dữ liệu |
| conflict_candidates | Các giá trị ứng viên trong một conflict group |
| record_match_candidates | Cặp record gần trùng, score, feature explanation, policy/model version và review status |
| reconciliation_runs | Một lần đối soát các source batch/web scope theo filter và policy version |
| reconciliation_items | Logical record, giá trị web/Excel/OCR, status, differences và resolution |
| ocr_field_extractions | Giá trị OCR theo field, confidence, bounding evidence và model/template version |
| official_records | Dữ liệu official hiện hành |
| official_record_versions | Phiên bản dữ liệu official trước/sau sửa |
| production_day_closures | Trạng thái chốt theo ngày và phạm vi |
| warning_overrides | Lý do bỏ qua Warning |

Trước khi viết migration cần đóng ADR về cấu trúc lưu official records:

| Phương án | Ưu điểm | Rủi ro |
|---|---|---|
| Một bảng riêng cho từng loại record | Constraint và index rõ, dễ tối ưu aggregation | Nhiều bảng, cần join/union khi drill-down tổng hợp |
| Bảng cha + bảng con theo loại record | Có identity chung và dữ liệu typed | Migration phức tạp hơn |
| Bảng generic + JSONB | Linh hoạt cho nhiều công ty | Constraint yếu hơn, dễ lỗi dữ liệu, aggregation khó tối ưu |

Khuyến nghị trước khi code schema: dùng bảng typed cho các record lõi của MVP hoặc bảng cha + bảng con; tránh generic JSONB cho dữ liệu official cần tính KPI nếu không có lý do mạnh.

### 8.6. KPI và dashboard

| Bảng | Mục đích |
|---|---|
| kpi_definitions | KPI Dictionary |
| kpi_definition_versions | Phiên bản công thức KPI |
| kpi_thresholds | Ngưỡng màu theo scope/type |
| kpi_snapshots | KPI đã tổng hợp theo grain/scope/time |
| kpi_snapshot_contributors | Link từ snapshot về official records hoặc nhóm record |
| dashboard_saved_filters | Bộ lọc đã lưu nếu cần |
| dashboard_chart_catalog | Chart code, required dimensions/metrics, capability, default order và drill-down contract |
| report_exports | Lịch sử xuất báo cáo |

### 8.7. Audit và notification

| Bảng | Mục đích |
|---|---|
| audit_logs | Log hành động bất biến |
| notifications | Thông báo trong hệ thống |
| notification_deliveries | Trạng thái gửi email/app/webhook nếu có |
| background_jobs | Metadata job để debug và retry |
| outbox_events | Sự kiện cần xử lý sau commit như import, aggregation, notification, export |

### 8.8. Identity, matching và deduplication

File hash chỉ nhận diện một file giống hệt, không đủ để chống trùng khi người dùng sửa file rồi upload lại. Mọi dòng dữ liệu vận hành phải tạo `dedupKey` hoặc matching metadata từ field canonical. Với event record, stable record ID hoặc external source ID là identity ưu tiên; natural matching chỉ dùng để tìm ứng viên trùng, không mặc định trở thành unique constraint tuyệt đối.

| Record type | Logical identity / matching rule đề xuất |
|---|---|
| production_output | companyId + productionDate + shiftId + machineId + productId |
| machine_state_interval | Stable record ID hoặc external source ID; natural matching dùng companyId + machineId + overlapping time range + stateType |
| incident | Stable record ID hoặc external source ID; natural matching dùng companyId + machineId + incidentStartTime + incidentType |
| quality_record | companyId + productionDate + shiftId + machineId + productId + defectType |
| production_plan | companyId + productionDate + shiftId + machineId + productId |

Dedup rules:

- Same logical identity/matching result, same value: link source to existing staging/official candidate, do not duplicate.
- Same logical identity/matching result, different value: create conflict group.
- Same logical identity/matching result, existing staging edited manually: create conflict group, do not overwrite.
- Same logical identity/matching result, official already closed: route through correction/late-data workflow.
- Event-like records such as machine_state_interval and incident need stable identity/versioning; changing endTime must update a version of the same record, not silently create a new logical record.
- Production plan version is not part of logical identity. If two plan versions overlap for the same machine/product/shift/date, system must flag conflict unless one is explicitly superseded.

Historical/similarity rules:

- `businessDataCode` cho production output được canonicalize từ company, production date, shift, machine, product và record type; không chứa upload time, batch ID, file name hoặc plan version.
- `dataTimeClass` được derive bằng policy versioned từ business date, received time, cutoff, closure và correction workspace. Không cho client tự gửi class rồi được tin trực tiếp.
- Exact identity/dedup luôn chạy trước similarity. Same identity/different value luôn thành conflict.
- Similarity score chỉ chạy trên candidate set đã giới hạn theo company, record type và business window. Baseline warning là `0.90 <= score < 1.00`; policy/version được lưu cùng candidate.
- Scorer trả contribution/rationale theo field để UI giải thích cảnh báo. Không dùng model output như SQL, permission, source precedence hoặc merge command.
- Mỗi resolve có idempotency key, actor, reason và audit. Candidate/model version cũ không bị xóa khi policy thay đổi.

---

## 9. API Design

> **Ghi chú phạm vi:** phần này là thiết kế API tham khảo ban đầu, đủ để bắt đầu code các module lõi. Một tài liệu API document riêng (OpenAPI/Swagger đầy đủ, request/response schema chi tiết cho mọi endpoint, mã lỗi chuẩn hoá) sẽ được viết sau, khi các endpoint dưới đây đã ổn định qua thực tế phát triển. Khi tài liệu API riêng ra đời, mục này nên được rút gọn lại thành liên kết trỏ sang đó thay vì duy trì hai nơi liệt kê endpoint dễ lệch nhau.

API dùng REST, version prefix `/api/v1`, resource plural, path kebab-case. Request/response dùng camelCase.

`companyId` không được tin trực tiếp từ client để quyết định tenant hoặc quyền. Trong MVP single-tenant, server resolve `companyId` từ authenticated context hoặc cấu hình deployment. Nếu một endpoint tạm nhận `companyId`, server bắt buộc kiểm tra nó khớp company của session trước khi xử lý.

### 9.1. Response format

```json
{
  "success": true,
  "data": {},
  "message": "Optional message"
}
```

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": []
  }
}
```

### 9.2. Request/response schema tối thiểu cho API chính

`POST /api/v1/staging-records`

```json
{
  "productionDate": "2026-07-10",
  "shiftId": "shift_night",
  "machineId": "machine_a",
  "workGroupId": "group_1",
  "productId": "product_x",
  "recordType": "production_output",
  "sourceOccurredAt": "2026-07-10T14:00:00+07:00",
  "values": {
    "plannedQuantity": 1000,
    "actualQuantity": 950,
    "goodQuantity": 930,
    "defectQuantity": 20
  },
  "sourceType": "manual"
}
```

`POST /api/v1/closures`

```json
{
  "productionDate": "2026-07-10",
  "scopeType": "production_line",
  "scopeId": "line_a",
  "mode": "complete",
  "warningOverrideReasons": []
}
```

`GET /api/v1/dashboard/kpis`

```http
GET /api/v1/dashboard/kpis?scopeType=factory&scopeId=factory_1&periodType=month&periodStart=2026-06-01&periodEnd=2026-06-30&shiftIds=shift_day&productIds=product_x
```

Nếu filter dashboard trở nên quá phức tạp cho query parameters, dùng endpoint tạo query riêng như `POST /api/v1/dashboard/kpi-queries`, trả về query id hoặc kết quả theo cùng response format.

`dataTimeClass`, `businessDataCode`, `companyId`, role và effective scope là server-derived fields. Client có thể gửi source timestamp/external ID làm dữ liệu đầu vào, nhưng không được tự gán record là lịch sử, current hoặc có quyền truy cập.

`POST /api/v1/reconciliation-runs`

```json
{
  "excelSourceBatchId": "batch_excel_20260801",
  "scopeType": "production_line",
  "scopeId": "line_a",
  "periodStart": "2026-07-01",
  "periodEnd": "2026-07-31",
  "includeOcrEvidence": true,
  "idempotencyKey": "reconcile-line-a-2026-07-v1"
}
```

`GET /api/v1/dashboard/charts`

```http
GET /api/v1/dashboard/charts?chartCodes=production-trend,plan-vs-actual,machine-state-mix,downtime-pareto,defect-pareto,shift-heatmap,data-coverage,reconciliation-status&scopeType=production_line&scopeId=line_a&periodStart=2026-07-01&periodEnd=2026-07-31
```

Mỗi chart item trả `chartCode`, `status`, `unit`, `series`, `completeness`, `freshness`, `lastCalculatedAt` và `drillDown`. `status` phân biệt `ready`, `empty`, `partial`, `stale`, `updating`, `forbidden` và `unavailable_capability`; không thay trạng thái này bằng series toàn số 0.

### 9.3. Endpoint nhóm cấu hình và danh mục

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/company/settings` | Xem cấu hình công ty của deployment/session hiện tại |
| PATCH | `/api/v1/company/settings` | Cập nhật cấu hình công ty của deployment/session hiện tại |
| GET | `/api/v1/factories` | Danh sách nhà máy trong quyền |
| POST | `/api/v1/machines` | Tạo máy |
| PATCH | `/api/v1/machines/:id` | Sửa máy |
| POST | `/api/v1/machines/:id/deactivate` | Ngừng sử dụng máy |
| POST | `/api/v1/master-data/imports` | Import danh mục |

### 9.4. Endpoint nhập liệu và import

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/v1/staging-records` | Nhập tay |
| GET | `/api/v1/staging-records` | Danh sách staging theo filter |
| POST | `/api/v1/source-batches` | Upload file/API batch |
| GET | `/api/v1/source-batches/:id` | Trạng thái batch |
| GET | `/api/v1/source-batches/:id/errors` | Lỗi dòng/trường |
| GET | `/api/v1/source-records/:id/match-candidates` | Ứng viên gần trùng, score, giải thích và policy/model version |
| POST | `/api/v1/conflict-groups/:id/resolve` | Xử lý xung đột |
| POST | `/api/v1/reconciliation-runs` | Tạo lần đối soát Excel–web và tùy chọn OCR |
| GET | `/api/v1/reconciliation-runs/:id` | Tiến độ/tổng hợp Matched/Mismatch/Missing/OCR confidence |
| GET | `/api/v1/reconciliation-runs/:id/items` | Danh sách item và chênh lệch từng field |
| POST | `/api/v1/reconciliation-items/:id/resolve` | Xác nhận/link/giữ riêng/tạo conflict với lý do và idempotency key |

### 9.5. Endpoint chốt và sửa sau chốt

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/v1/closures` | Chốt phạm vi/ngày |
| GET | `/api/v1/closures` | Xem trạng thái chốt |
| POST | `/api/v1/closures/:id/reopen-requests` | Tạo yêu cầu mở lại |
| POST | `/api/v1/reopen-requests/:id/approve` | Duyệt mở lại |
| POST | `/api/v1/closures/:id/reclose` | Chốt lại sau sửa |

### 9.6. Endpoint dashboard và drill-down

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/dashboard/kpis` | KPI tổng quan |
| GET | `/api/v1/dashboard/charts` | Nhiều chart theo catalog/whitelist, cùng filter và data-status contract |
| GET | `/api/v1/dashboard/trends` | Xu hướng |
| GET | `/api/v1/dashboard/rankings` | Xếp hạng máy/tổ; cấm xếp hạng cá nhân |
| GET | `/api/v1/dashboard/downtime-reasons` | Phân tích nguyên nhân dừng |
| GET | `/api/v1/dashboard/drill-down` | Drill-down từ KPI |
| GET | `/api/v1/audit-logs` | Tra audit theo quyền |

---

## 10. Job Queue Và Xử Lý Nền

### 10.1. Job types

| Job | Trigger | Idempotency key |
|---|---|---|
| import_file | `import_requested` outbox event sau khi upload file | sourceBatchId; row-level dedup dùng dedupKey/matching rule + templateVersion |
| reconcile_import_upload | Scheduled hoặc sau lỗi active-mark | sourceBatchId hoặc fileKey |
| cleanup_orphan_upload | Scheduled cleanup | fileKey + retentionCutoff |
| process_ocr | Upload image nếu bật OCR | companyId + imageHash + ocrTemplateVersion |
| score_match_candidates | Source record normalized hoặc policy re-evaluation | sourceRecordId + candidateSetVersion + similarityPolicyVersion |
| reconcile_sources | User tạo reconciliation run hoặc source batch hoàn tất | reconciliationRunId + inputSourceVersions + policyVersion |
| aggregate_kpi | Chốt hoặc chốt lại | companyId + scope + productionDate + officialVersion |
| send_notification | Missing data/job done/escalation | notificationId |
| export_report | User export request | reportExportId |

### 10.2. Retry policy

- Retry lỗi tạm thời như timeout, database connection, queue connection.
- Không retry vô hạn. Mỗi job có max retry và backoff.
- Lỗi parse file hoặc lỗi dữ liệu là lỗi nghiệp vụ, không retry tự động nếu file không đổi.
- Job thất bại sau retry vào dead letter queue hoặc trạng thái failed để admin xử lý.

### 10.3. Idempotency

Mọi job ghi dữ liệu phải có idempotency key. Ví dụ:

- Upload lại cùng file không tạo duplicate nếu file và template không đổi.
- Upload lại file đã sửa không tạo duplicate vì từng dòng được dedup bằng dedupKey hoặc matching rule.
- Chạy lại aggregate cho cùng officialVersion ghi đè snapshot cùng version, không cộng dồn hai lần.
- Resolve conflict hai lần với cùng requestId không tạo hai quyết định khác nhau.
- Chấm điểm lại cùng source/candidate/policy version không tạo candidate trùng; đổi policy tạo evaluation version mới, không ghi đè evidence cũ.
- Retry reconciliation cùng idempotency key không tạo run/item hoặc resolution thứ hai.

### 10.4. Transaction và locking cho chốt dữ liệu

Các thao tác chốt, mở lại và chốt lại phải chạy trong transaction ở phạm vi nhỏ nhất có thể.

Quy tắc:

- Chỉ một thao tác chốt/mở lại được chạy tại cùng thời điểm cho cùng company + productionDate + scope.
- Lock phải hiểu phân cấp scope. Chốt factory xung đột với mọi closure line/group bên dưới; chốt line xung đột với factory cha và group con; chốt group xung đột với factory/line cha.
- Phương án đơn giản cho MVP: luôn chốt ở grain thấp nhất được cấu hình, sau đó hệ thống tổng hợp trạng thái cấp trên. Nếu vẫn cho phép chốt cấp cha trực tiếp, phải lấy lock cho toàn bộ cây scope liên quan.
- Transaction chốt phải kiểm tra lại Error/Warning/missing data ngay trước khi tạo official version, không chỉ tin kết quả kiểm tra trước đó trên UI.
- Nếu có staging record mới xuất hiện trong lúc người dùng đang xem màn hình chốt, request chốt phải fail với conflict và yêu cầu refresh.
- Reclose sau sửa phải tạo officialVersion mới và đánh dấu snapshot liên quan là stale trong cùng transaction.
- Không chạy aggregation nặng trong transaction chốt.

### 10.5. Outbox bắt buộc cho sự kiện sau commit

Để tránh trường hợp database commit thành công nhưng publish queue thất bại, các thao tác tạo sự kiện xử lý nền phải ghi outbox event trong cùng transaction với dữ liệu nghiệp vụ liên quan.

Luồng chốt/chốt lại:

1. Transaction ghi official version, closure status, audit log và outbox event `kpi_aggregation_requested`.
2. Worker đọc outbox event chưa xử lý.
3. Worker chạy aggregation với idempotency key từ event id/officialVersion.
4. Worker đánh dấu outbox event processed hoặc failed có retry.

Luồng import file:

1. File gốc được lưu thành công vào storage ở trạng thái temporary.
2. Transaction tạo source batch, lưu file key và outbox event `import_requested`.
3. Sau khi commit thành công, hệ thống đánh dấu file object là active và liên kết với source batch như metadata vòng đời.
4. Worker đọc outbox event chưa xử lý.
5. Worker kích hoạt import với idempotency key từ event id/sourceBatchId.
6. Worker đọc file bằng file key trên source batch. Source batch đã commit kèm file key là điều kiện đủ để bắt đầu import; trạng thái active của file không phải điều kiện readiness.
7. Nếu file object tạm thời chưa đọc được do storage delay/lỗi tạm thời, worker retry với backoff và không đánh dấu outbox event processed.
8. Worker đánh dấu outbox event processed hoặc failed có retry.

Nếu file lưu thành công nhưng transaction database thất bại, không được tạo source batch rỗng. File temporary không có source batch hợp lệ phải được cleanup định kỳ sau retention window cấu hình. Cleanup không được xóa file đang được tham chiếu bởi source batch chưa xử lý xong, dù file đó chưa được đánh dấu active. Nếu bước đánh dấu active thất bại sau commit, reconciliation job phải retry việc liên kết/đánh dấu active hoặc đưa batch vào trạng thái cần admin xử lý; không được tự tạo batch thứ hai cho cùng file.

Queue có thể vẫn được dùng để scale worker, nhưng outbox là nguồn sự thật cho sự kiện cần xử lý sau commit.

---

## 11. KPI Engine Và Aggregation

### 11.1. KPI definition

Mỗi KPI definition gồm:

- `code`
- `name`
- `version`
- `effectiveFrom`
- `effectiveTo`
- `formulaType`
- `formulaConfig`
- `requiredFields`
- `aggregationRule`
- `weightingRule`
- `missingDataRule`
- `roundingRule`
- `thresholdConfig`
- `approvedBy`
- `approvedAt`

Không hardcode công thức KPI lõi trong controller/API. Công thức phải nằm trong KPI engine hoặc configuration versioned.

Giới hạn MVP:

- KPI Dictionary là tài liệu nghiệp vụ đã duyệt.
- Hệ thống lưu version KPI được duyệt để trace kết quả.
- Chỉ hỗ trợ các `formulaType` đã code sẵn hoặc cấu hình có kiểm soát.
- Không có UI nhập công thức tự do.
- Không dùng `eval()` hoặc thực thi công thức dạng code do người dùng nhập.

### 11.2. Aggregation grain

KPI snapshots lưu theo:

- Company.
- Factory, line, group, machine nếu áp dụng.
- Time grain: day, week, month, year, custom period materialized nếu cần.
- Shift nếu KPI cần xem theo ca.
- Product nếu KPI phụ thuộc sản phẩm.
- KPI definition version.
- Official data version.
- Data completeness status: complete, partial, exception, stale.
- Coverage metrics: expected child scopes, closed child scopes, missing child scopes.

### 11.3. Recalculation

Khi chốt hoặc chốt lại:

1. Xác định phạm vi official records bị ảnh hưởng (scope trực tiếp vừa chốt/chốt lại, ví dụ một `work_group`).
2. **Xác định toàn bộ scope tổ tiên (ancestor) chứa scope đó trong cùng kỳ báo cáo** — ví dụ `work_group` vừa sửa thuộc `production_line` nào, `production_line` đó thuộc `factory` nào. Việc xác định ancestor phải dùng đúng `machine_assignments`/cơ cấu tổ chức **có hiệu lực tại thời điểm phát sinh dữ liệu** (theo effective dating ở PRD 8.5), không dùng cơ cấu hiện tại nếu đã thay đổi.
3. Đánh dấu snapshot của **cả scope vừa sửa lẫn mọi scope tổ tiên ở bước 2** là `stale` — không chỉ scope trực tiếp. Nếu bỏ qua bước này, KPI cấp cha (line/factory) sẽ tiếp tục hiển thị số liệu cũ dù dữ liệu cấp con đã được sửa, mà không có dấu hiệu cảnh báo nào cho người xem.
4. Ghi hoặc xử lý outbox event để kích hoạt aggregation cho scope vừa sửa và từng scope tổ tiên bị ảnh hưởng, theo đúng grain nhỏ nhất có thể của từng scope.
5. Tính lại snapshots theo thứ tự từ scope thấp nhất lên scope cao nhất (bottom-up), vì snapshot cấp cha phụ thuộc kết quả đã tính lại của scope con.
6. Cập nhật `lastCalculatedAt`, `sourceVersion`, `dataFreshness`, `completenessStatus` và `coverageRate` cho từng snapshot vừa tính lại (mọi cấp).
7. Ghi audit log cho recalculation summary, liệt kê đầy đủ danh sách scope đã bị đánh dấu stale và đã tính lại (không chỉ scope gốc).

> **Lưu ý triển khai:** đây là nguồn lỗi tinh vi phổ biến trong hệ thống rollup — nếu chỉ tính lại đúng scope vừa sửa mà quên lan lên scope cha, dashboard cấp trên sẽ "trông như đã cập nhật" (không có nhãn stale) nhưng thực chất đang hiển thị số liệu cũ. Cần có test case riêng cho việc này ở mục 19.2.

### 11.4. Drill-down contributor links

Để dashboard vừa nhanh vừa truy nguyên được:

- KPI overview đọc từ `kpi_snapshots`.
- Drill-down đọc `kpi_snapshot_contributors` để tìm official records liên quan.
- Official records liên kết về source records và audit logs.

Cách này tránh quét toàn bộ dữ liệu thô khi xem dashboard tổng quan, nhưng vẫn hỗ trợ điều tra nguyên nhân.

### 11.5. Dashboard chart catalog

Chart code cốt lõi:

| Chart code | Dữ liệu | Cách hiển thị mặc định | Drill-down |
|---|---|---|---|
| `production-trend` | Sản lượng official theo thời gian | Line/area | Ngày → ca → record |
| `plan-vs-actual` | Kế hoạch và thực tế | Grouped bar | Kỳ → máy/sản phẩm |
| `approved-kpi-components` | Thành phần KPI đã duyệt | Multi-series line/bar | Snapshot → contributors |
| `machine-state-mix` | Running/downtime/no-plan | Stacked bar | Trạng thái → interval |
| `downtime-pareto` | Phút dừng theo lý do + tích lũy | Pareto | Lý do → máy/ca |
| `defect-pareto` | Lỗi theo loại + tích lũy | Pareto | Loại lỗi → source record |
| `shift-heatmap` | Sản lượng/KPI theo ngày và ca | Heatmap | Ô → ca/máy |
| `data-coverage` | Complete/partial/missing/stale | Stacked bar | Trạng thái → scope thiếu |
| `reconciliation-status` | Matched/mismatch/missing/OCR confidence | Stacked bar/donut | Segment → reconciliation items |

Các code `material-variance`, `inventory-flow`, `maintenance-backlog` và `maintenance-plan-completion` là conditional. API trả `unavailable_capability` nếu caller yêu cầu chart chưa bật; UI không render dữ liệu giả. Query nhận danh sách chart code whitelist, không nhận công thức hoặc SQL tự do. Chart cache key luôn chứa company, effective scope, filter hash, KPI/official version và permission-relevant dimensions.

---

## 12. Cache, Indexing Và Partitioning

### 12.1. Cache

Cache chỉ dùng cho dữ liệu đọc phổ biến:

- Dashboard hôm nay.
- Dashboard tuần này.
- Master data ít thay đổi.
- Permission scope của user trong thời gian ngắn.

Cache key đề xuất:

```
factory-performance:v1:{company_id}:dashboard:{scope_type}:{scope_id}:{grain}:{period}:{filter_hash}
```

Cache phải bị invalidate hoặc versioned khi:

- Chốt dữ liệu.
- Chốt lại sau sửa.
- KPI definition thay đổi.
- Quyền hoặc phạm vi người dùng thay đổi.

### 12.2. Indexing

Index bắt buộc hoặc gần bắt buộc:

- `official_records(company_id, production_date, machine_id)`
- `official_records(company_id, production_date, work_group_id)`
- `staging_records(company_id, production_date, status)`
- `kpi_snapshots(company_id, grain, period_start, scope_type, scope_id, kpi_code)`
- `audit_logs(company_id, entity_type, entity_id, created_at)`
- `source_batches(company_id, status, created_at)`

### 12.3. Partitioning

Partition theo thời gian áp dụng cho bảng tăng mạnh:

- `official_records`
- `official_record_versions`
- `audit_logs`
- `source_records`
- `kpi_snapshots` nếu dữ liệu lớn

Đề xuất partition theo tháng cho dữ liệu vận hành nếu volume cao. Quyết định cuối cùng phụ thuộc dữ liệu thực tế và PostgreSQL/MySQL được chọn.

### 12.4. Data lifecycle và archival (lưu trữ dài hạn)

Partitioning và cache ở 12.1-12.3 giải quyết bài toán **tốc độ truy vấn** khi dữ liệu tăng qua nhiều năm. Mục này giải quyết bài toán còn lại: **chi phí lưu trữ** khi dự án chạy nhiều năm liên tục (dự án dài hạn, không kết thúc sau kỳ thực tập — xem PRD mục 1).

| Loại dữ liệu | Chính sách đề xuất | Ghi chú |
|---|---|---|
| `official_records`, `official_record_versions` đã cũ (partition quá hạn `hotRetentionMonths`) | Chuyển partition sang storage rẻ hơn (cold tier) hoặc nén; vẫn phải đọc được khi drill-down vào dữ liệu cũ, chỉ chấp nhận độ trễ truy vấn cao hơn | Không xoá — dữ liệu official không được xoá cứng (PRD 8.5) |
| `kpi_snapshots` | Giữ "nóng" vô thời hạn, không chuyển cold tier | Đây là bảng nhỏ, được đọc thường xuyên nhất (dashboard/drill-down xu hướng nhiều năm); chi phí giữ nóng không đáng kể so với lợi ích tốc độ |
| `source_records`, `source_batches`, file gốc trong object storage | Sau `rawRetentionMonths`, chuyển sang cold storage hoặc xoá theo chính sách công ty, **miễn không phải là file gốc của dữ liệu official chưa hết hạn audit** | Cần đối chiếu với FR-AUD-04 (thời hạn lưu audit log) và TBD tương ứng ở PRD — không tự ý xoá sớm hơn thời hạn audit |
| `audit_logs` | Theo policy riêng, thường dài hơn dữ liệu vận hành thô (yêu cầu tuân thủ/đối chiếu) | Xem FR-AUD-04 |

Nguyên tắc: **`hotRetentionMonths` và `rawRetentionMonths` là tham số cấu hình theo công ty** (đặt trong `company_settings`, xem TDD 8.2), không hardcode, vì mỗi công ty có thể có yêu cầu lưu trữ khác nhau (liên quan chiến lược mở rộng nhiều công ty ở TDD mục 4).

> **Blocker nghiệp vụ:** giá trị cụ thể của `hotRetentionMonths`/`rawRetentionMonths` chưa được chốt — xem TBD-21 (PRD mục 14). Không cần chặn phát triển giai đoạn đầu (dữ liệu chưa đủ lớn để cần archival), nhưng nên chốt trước khi hệ thống chạy đủ lâu để bảng dữ liệu thô vượt ngưỡng cần quan tâm — với dự án dài hạn nhiều người, nên rà lại câu hỏi này định kỳ (ví dụ mỗi 6 tháng) thay vì chỉ hỏi một lần.

---

## 13. Security Và Privacy

### 13.1. Authentication

- Mọi request nghiệp vụ cần authentication.
- Mật khẩu phải hash bằng thuật toán an toàn.
- Sai mật khẩu quá số lần cấu hình thì khóa tạm.
- Session/token expiry cấu hình theo policy.
- Secrets lấy từ biến môi trường, không hardcode.

### 13.2. Authorization

Kiểm tra quyền theo:

- Role.
- Company.
- Factory/line/group scope.
- Action.
- Data sensitivity.

Không tin dữ liệu scope từ client. Server phải tự resolve scope từ user.

Authorization target tách ba khái niệm:

- `operationalRole`: `EXECUTIVE_MANAGER`, `OPERATIONS_MANAGER`, `OPERATIONS_STAFF`;
- `functionalRole`/permission: `DATA_ADMIN`, `SYSTEM_ADMIN`, `AUDITOR` và action cụ thể;
- `employeePosition`: chức danh như Ca trưởng/Tổ trưởng, chỉ mô tả phân công nhân sự.

Không derive permission từ `employeePosition`, label navigation hoặc role slug do client gửi. Endpoint trả module access chỉ là presentation contract; mọi endpoint dữ liệu vẫn chạy authorization server-side.

MVP single-tenant:

- `companyId` vẫn được lưu và kiểm tra nhất quán.
- Không có yêu cầu vận hành nhiều công ty độc lập trên cùng instance trong MVP.
- Nếu chuyển sang multi-tenant thật, phải có ADR riêng về database isolation, backup/restore theo tenant, logging, admin access và quy trình onboarding/offboarding tenant.

### 13.3. Privacy

- MVP không hiển thị ranking cá nhân.
- Export báo cáo phải kiểm tra quyền tương tự API đọc.
- Audit log có thể chứa dữ liệu nhạy cảm, chỉ cấp cho vai trò phù hợp.
- Không log password, token, secret hoặc file content không cần thiết.

---

## 14. Error Handling Và Logging

### 14.1. Error classes

Phân biệt:

- Validation error: dữ liệu đầu vào sai.
- Authorization error: không đủ quyền.
- Conflict error: dữ liệu xung đột cần review.
- Not found error: entity không tồn tại hoặc ngoài quyền.
- Operational error: database, queue, storage, external service.
- Programmer error: bug cần fix, không trả chi tiết cho người dùng.

### 14.2. Logging

Log phải có structured fields:

- requestId
- userId nếu có
- companyId
- module
- action
- entityType
- entityId
- durationMs
- errorCode nếu có
- similarityPolicyVersion/ocrModelVersion nếu log liên quan đối soát; không log toàn bộ file/ảnh nếu không cần thiết

Không log dữ liệu nhạy cảm không cần thiết.

---

## 15. Audit Design

Audit log ghi append-only. Không có update/delete qua luồng nghiệp vụ thường.

Các hành động phải log:

- Login failed, account locked.
- Create/update/deactivate master data.
- Change standard speed/capacity.
- Change role/scope.
- Manual data entry.
- File import and import result.
- Historical/current classification và thay đổi classification do policy version.
- Near-duplicate candidate review và reconciliation resolution.
- OCR confirmation nếu bật OCR.
- Validation warning override.
- Conflict resolution.
- Close, reopen, approve reopen, reclose.
- KPI definition approval/change.
- Report export.

Audit log tối thiểu gồm:

- companyId
- actorUserId
- action
- entityType
- entityId
- beforeValue
- afterValue
- reason
- requestId
- ip/userAgent nếu phù hợp
- createdAt

---

## 16. Notification Design

Notification có thể bắt đầu bằng in-app notification trong MVP.

Các trigger:

- Gần hết ca nhưng thiếu dữ liệu bắt buộc.
- Đến hạn chốt nhưng còn Error/Warning/missing data.
- File import thành công/lỗi một phần/thất bại.
- Aggregation failed.
- Dữ liệu trễ được nhập vào kỳ đã chốt.
- Quá hạn chốt cần escalate.

Notification config theo company:

- Deadline theo ca/ngày.
- Recipient role.
- Escalation delay.
- Channel: in-app, email, webhook nếu có.

---

## 17. Export Design

Export sau MVP hoặc tùy chọn.

Nguyên tắc:

- Export chạy nền nếu file nặng.
- File export gắn với user, filter, thời điểm, data version.
- Không export dữ liệu ngoài quyền.
- Nếu dữ liệu kỳ đã bị điều chỉnh sau khi export, khi xem lại lịch sử export phải hiển thị trạng thái outdated/adjusted.

---

## 18. Voice/NLU Design Tùy Chọn

Voice không thuộc MVP core.

Nếu bật:

- Voice chỉ chuyển thành dashboard filter hoặc command đã whitelist.
- Voice không truy cập database trực tiếp.
- Voice request đi qua Dashboard Serving và Access Control.
- Không hỗ trợ hỏi đáp tự do trong phạm vi này.
- Lệnh không hiểu phải trả feedback trong 3 giây.

Ví dụ command hợp lệ:

- "Xem hiệu suất hôm nay"
- "Xem dây chuyền A tuần này"
- "So sánh tổ A và tổ B tháng này"

---

## 19. Testing Strategy

### 19.1. Unit tests

- KPI formulas.
- Missing data rules.
- Closing eligibility.
- Source conflict detection.
- Effective-date lookup.
- Permission scope resolution.
- Business data code canonicalization và bốn lớp `dataTimeClass`.
- Similarity boundary: `0.8999`, `0.90`, `0.9999`, `1.00`; cùng identity khác giá trị luôn conflict.
- Navigation capability resolution không cấp quyền từ chức danh Ca trưởng/Tổ trưởng.

### 19.2. Integration tests

- Import file có lỗi một phần.
- Upload lại file đã sửa không tạo duplicate theo dedupKey hoặc matching rule.
- Retry similarity/reconciliation job không tạo candidate, item hoặc resolution trùng.
- Excel–web reconciliation trả đúng Matched/Mismatch/Missing và chặn chốt item chưa resolve.
- OCR low-confidence chỉ là evidence thứ yếu và không tạo official record, khi capability OCR bật.
- Chốt khi có Warning và lý do override.
- Chốt bị chặn bởi Error.
- Đóng kỳ ngoại lệ khi còn dữ liệu thiếu được phê duyệt, chỉ bắt buộc nếu FR-CLO-05 được đưa vào release.
- KPI cấp trên hiển thị partial khi chỉ một phần scope con đã chốt.
- Sửa sau chốt và recalculation.
- **Sửa sau chốt ở scope con (work_group) phải làm snapshot của mọi scope tổ tiên (line, factory) chứa scope đó trong cùng kỳ chuyển sang `stale` và được tính lại — không chỉ snapshot của scope vừa sửa.**
- Machine transfer effective date không làm đổi báo cáo lịch sử.
- Drill-down từ KPI về official/source/audit.
- Dashboard chart catalog trả cùng filter/scope/completeness cho tối thiểu 8 view cốt lõi và không biến unavailable/empty thành 0.

### 19.3. Contract tests cho adapter

Mỗi adapter phải có test:

- Parse sample input.
- Map required fields.
- Reject missing required fields.
- Produce stable idempotency key.
- Report row-level errors.
- Preserve source occurred/received time, template/model version và dataTimeClass inputs.

### 19.4. Performance tests

Dataset tối thiểu theo PRD:

- 2 năm dữ liệu.
- 100 máy.
- 3 ca/ngày.
- Nhiều tổ/dây chuyền.
- Bản ghi trạng thái/máy/ca: TBD theo dữ liệu thật hoặc giả định được duyệt.
- Dòng tối đa mỗi file import: TBD.
- Số import đồng thời: TBD.
- Người dùng đồng thời: TBD.
- Tần suất refresh dashboard: TBD.
- Dashboard p95 < 3 giây cho overview.
- Drill-down p95 < 5 giây với filter hợp lý.

Không được sign-off performance nếu các tham số TBD trên chưa có giá trị hoặc chưa được mentor/Product approver chấp nhận làm giả định kiểm thử.

### 19.5. E2E acceptance tests

- Scenario nhập tay và chốt ngày bình thường.
- Upload file 100 dòng, 95 hợp lệ, 5 lỗi.
- Dữ liệu thiếu cuối ngày.
- Chốt khi có Warning.
- Phát hiện sai sau chốt.
- Dữ liệu đến muộn sau chốt.
- Xung đột giữa form và file import.
- Dữ liệu lịch sử giống 96% record hiện có tạo warning có giải thích, không tự merge.
- Đối soát Excel–web có match, mismatch và missing; mọi item bất thường có resolution/audit trước close.
- Ba cấp role vận hành và navigation theo capability; đổi chức danh Ca trưởng/Tổ trưởng không đổi quyền.
- Tám biểu đồ cốt lõi dùng official data, cùng filter và có empty/partial/stale/drill-down state.
- Xung đột form/file/OCR, chỉ bắt buộc khi capability OCR được bật.

---

## 20. Deployment Và Vận Hành

### 20.1. Environment variables

Ví dụ nhóm biến môi trường:

- `DATABASE_URL`
- `REDIS_URL`
- `JWT_SECRET` hoặc session secret tương đương
- `OBJECT_STORAGE_BUCKET`
- `OBJECT_STORAGE_ACCESS_KEY`
- `OBJECT_STORAGE_SECRET_KEY`
- `APP_BASE_URL`
- `LOG_LEVEL`
- `ALLOWED_ORIGINS`

Không commit `.env`. Không hardcode secret.

### 20.2. Migration

- Mọi thay đổi schema qua migration.
- Migration version-controlled.
- Không sửa trực tiếp schema production.
- Migration liên quan dữ liệu lịch sử phải có rollback/forward plan.

### 20.3. Backup và restore

- Backup database định kỳ.
- Backup object storage nếu lưu file gốc.
- Kiểm thử restore theo lịch.
- Audit log và official data có retention theo policy.
- RPO và RTO phải được chốt trước production. Ví dụ format quyết định: RPO <= 15 phút, RTO <= 4 giờ.

### 20.4. Monitoring

Metrics cần có:

- API response time p50/p95/p99.
- Dashboard query duration.
- Import job duration and failure rate.
- Aggregation job duration and failure rate.
- Queue depth.
- Cache hit rate.
- Closure lateness.
- Missing data count.

Alert cần có:

- Aggregation failed.
- Import failure spike.
- Dashboard p95 vượt ngưỡng.
- Queue backlog cao.
- Database connection issue.

---

## 21. Open Technical Decisions

| Mã | Quyết định cần chốt | Chặn công việc | Ghi chú |
|---|---|---|---|
| ADR-01 | Stack backend chính: NestJS hay Spring Boot? | Khởi tạo codebase | Cần chốt trước khi scaffold backend. |
| ADR-02 | Database chính: PostgreSQL hay MySQL? | Migration/schema | Cần chốt trước schema và partition/index strategy. |
| ADR-03 | Queue: Redis/BullMQ, RabbitMQ hay dịch vụ cloud? | Import async/worker | Không chặn nhập tay/chốt cơ bản nếu outbox table đã có. |
| ADR-04 | ORM/query builder nào? | Repository/migration | Cần chốt trước khi viết data access. |
| ADR-05 | Cache strategy và invalidation chi tiết | Không chặn giai đoạn nền tảng | Chốt trước tối ưu dashboard/performance. |
| ADR-06 | Object storage dùng gì? | File import/OCR/export | Không chặn nhập tay MVP. |
| ADR-07 | Auth dùng JWT hay session? | Login và access control | Cần chốt trước auth/permission. |
| ADR-08 | Khi mở rộng sau MVP, multi-tenant dùng một database hay tách database theo company? | Không chặn MVP | MVP single-tenant; chốt sau khi có nhu cầu multi-tenant thật. |
| ADR-09 | Official records dùng bảng typed, bảng cha/con hay generic JSONB? | Schema dữ liệu vận hành | Phải chốt trước migration official/staging records. |
| ADR-10 | Chốt dữ liệu chỉ ở grain thấp nhất hay cho phép chốt cấp cha trực tiếp với hierarchical lock? | Module chốt | Phải chốt trước khi code closure workflow. |
| ADR-11 | Outbox processor triển khai trong cùng app worker hay service riêng? | Worker sau chốt | Cơ chế outbox là bắt buộc; chỉ còn chốt cách triển khai processor. |
| ADR-12 | `hotRetentionMonths`/`rawRetentionMonths` (mục 12.4) mặc định là bao nhiêu, và cơ chế chuyển cold tier triển khai bằng gì (partition detach thủ công, table tiering của cloud provider, hay job tự viết)? | Không chặn giai đoạn đầu | Nên chốt trước khi `source_records`/`official_records` đủ lớn để ảnh hưởng chi phí lưu trữ; xem lại định kỳ vì dự án chạy dài hạn. |
| ADR-13 | Similarity scorer dùng rule-weighted, model ML cục bộ hay dịch vụ ngoài; cách version, giải thích và đánh giá precision/recall? | Cảnh báo AI gần trùng | Không chặn exact dedup hoặc Excel–web comparison deterministic; phải chốt trước khi bật cảnh báo AI trên dữ liệu thật. |

MVP mặc định single-tenant cho một công ty. Không nên chốt ADR multi-tenant trước khi có nhu cầu vận hành nhiều công ty thật sự và yêu cầu bảo mật/cách ly cụ thể.

---

## 22. Technical Acceptance Gates

Trước khi sign-off kỹ thuật MVP:

- PRD (phiên bản mới nhất đã được mentor duyệt — xem bảng Lịch Sử Phiên Bản đầu file PRD) đã được duyệt hoặc các blocker liên quan đã có quyết định tạm thời.
- KPI Dictionary tối thiểu có một KPI được phê duyệt bằng ví dụ tính tay.
- Canonical schema cho các record MVP đã được chốt.
- Logical identity, matching rules, unique constraints và dedup rules cho từng loại record MVP đã được chốt.
- `businessDataCode`, bốn lớp `dataTimeClass` và policy classification có test input/output cụ thể.
- Mapping ba role vận hành từ enum Current có migration/negative permission test; chức danh Ca trưởng/Tổ trưởng không cấp quyền.
- Navigation label/module key khớp PRD 4.4 và module chưa bật không xuất hiện như capability sẵn sàng.
- Cấu trúc lưu official records đã được chốt bằng ADR trước migration.
- Permission matrix được map thành quyền hệ thống cụ thể.
- Server resolve company context từ auth/deployment config; không tin `companyId` từ client.
- Transaction/hierarchical locking cho chốt, mở lại và chốt lại đã có test.
- Outbox event cho aggregation sau chốt đã có test mất queue/publish failure.
- Outbox event `import_requested` đã có test khi queue/worker tạm thời không hoạt động; source batch phải được xử lý sau retry và không tạo staging record trùng.
- Import worker đã có test trường hợp outbox chạy trước khi file được đánh dấu active; worker vẫn đọc theo file key trên source batch hoặc retry nếu storage tạm thời chưa đọc được.
- File upload temporary không có source batch hợp lệ được cleanup định kỳ; file đã commit source batch nhưng chưa active được reconciliation xử lý.
- Import file mẫu thật pass contract test.
- Excel–web reconciliation pass test match/mismatch/missing, unresolved item chặn close và retry idempotent.
- Cảnh báo gần trùng lưu score/feature/policy version, không auto merge; ADR-13 và fixture precision/recall được duyệt trước dữ liệu thật.
- Nếu OCR bật, sample form/confidence threshold (TBD-22), secondary-source guardrail và audit pass test.
- Chốt, chốt một phần, sửa sau chốt và recalculation pass E2E test.
- Drill-down từ KPI về dữ liệu nguồn hoạt động.
- Tám chart cốt lõi dùng official snapshot/aggregate, giữ scope/filter/completeness/freshness và có drill-down hoặc bảng fallback.
- Permission test chứng minh user không truy cập ngoài scope bằng API.
- Audit log có đủ hành động bắt buộc.
- Performance test với dataset giả lập đạt ngưỡng PRD hoặc có quyết định điều chỉnh ngưỡng.
- RPO/RTO được chốt nếu chuẩn bị triển khai production.
- Backup/restore tối thiểu đã được kiểm thử trong môi trường staging.
