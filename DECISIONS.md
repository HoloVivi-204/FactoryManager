# Decisions, ADR và TBD

> Đây là sổ quyết định, không phải nơi tự giải đáp câu hỏi nghiệp vụ. Mục có
> trạng thái `Open` vẫn là open cho đến khi lựa chọn, ngày, bối cảnh, phạm vi
> ảnh hưởng và cách kiểm chứng được ghi nhận đầy đủ.

## 1. Cách đọc trạng thái

| Trạng thái | Ý nghĩa |
|---|---|
| `Accepted — Product` | Đã được PRD ghi là quyết định nghiệp vụ |
| `Accepted — Technical` | Đã có ADR kỹ thuật ghi rõ lựa chọn và hệ quả |
| `Implemented, ADR pending` | Code hiện dùng phương án này nhưng TDD/ADR chưa chính thức đóng quyết định |
| `Target constraint` | Bất biến target đã được PRD/TDD yêu cầu; chi tiết triển khai có thể còn mở |
| `Open — Blocker` | Không implement phần phụ thuộc nếu chưa có quyết định hoặc giả định tạm được ghi trong DECISIONS/PRD |
| `Open — Conditional` | Chỉ chặn khi capability liên quan được đưa vào release |
| `Open — Non-blocker` | Có thể tiếp tục phần độc lập; không được biến đề xuất thành quyết định ngầm |
| `Superseded` | Đã bị một quyết định mới thay thế, phải dẫn tới quyết định mới |

Thứ tự nguồn:

1. [PRD v2.5](PRD_He_thong_hieu_suat_nha_may.md) quyết định nghiệp vụ.
2. [TDD v1.5](TDD_He_thong_hieu_suat_nha_may.md) quyết định/đề xuất kỹ thuật.
3. Source code chứng minh Current, nhưng không tự đóng domain blocker.
4. [ARCHITECTURE.md](ARCHITECTURE.md) mô tả Current/Target và
   [API_Contracts.md](API_Contracts.md) mô tả contract/gap.

## 2. Quyết định sản phẩm đã chốt

| ID | Trạng thái | Quyết định | Hệ quả |
|---|---|---|---|
| PRD-TBD-03 | Accepted — Product | MVP chạy single-tenant cho một công ty; multi-tenant cùng instance ngoài MVP | Target vẫn lưu `companyId`; không xây tenant-admin/billing/isolation đa tenant trong MVP |
| PRD-TBD-10 | Accepted — Product | Sửa sau chốt dùng một bước phê duyệt; Quản lý điều hành phê duyệt hoặc từ chối reopen request | API/role matrix phải có create request và approve/reject tách biệt |
| PRD-TBD-15 | Accepted — Product | Không xếp hạng hiệu suất cá nhân và không dùng dữ liệu cho lương, kỷ luật hoặc đánh giá cá nhân trong MVP | Dashboard/KPI core chỉ ở máy/tổ/ca; HR KPI Current không được trộn vào target performance ranking |
| SCOPE-01 | Accepted — Product | OCR giấy là capability đối soát thứ yếu mức Should; voice, export và ERP/MES vẫn sau MVP/tùy chọn | Excel–web reconciliation là Must; OCR chỉ được bật sau TBD-22 và không được ghi official trực tiếp |
| SCOPE-02 | Accepted — Product | MVP có nhân sự/vật tư ở phạm vi vận hành; kho đầy đủ, bảo trì đầy đủ, tài chính, chấm công chi tiết và tính lương không thuộc lõi Must | Tab extension chỉ xuất hiện khi capability được bật; không dùng code Current để bù Must còn thiếu |
| PRD-DEC-01 | Accepted — Product | Navigation theo module với nhãn chuẩn PRD 4.4; không có tab Ca trưởng/Tổ trưởng | Ca trưởng/Tổ trưởng được quản lý như chức danh có hiệu lực trong Quản lý nhân sự |
| PRD-DEC-02 | Accepted — Product | Ba cấp quyền vận hành là Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành | Role quản trị dữ liệu/hệ thống/kiểm toán trực giao; migration enum Current không được nâng quyền ngầm |
| PRD-DEC-03 | Accepted — Product | Dữ liệu được phân lớp Current/Historical Backfill/Late Arrival/Correction; cảnh báo gần trùng từ 90% đến dưới 100% chỉ hỗ trợ review | Exact identity chạy trước; điểm 100% đi qua exact matching; AI không tự merge/xóa/ghi đè/chọn nguồn thắng |
| PRD-DEC-04 | Accepted — Product | Excel và dữ liệu web là nguồn đầu vào chính qua staging; OCR giấy là bằng chứng thứ yếu | Mismatch/missing/OCR low confidence cần resolution, lý do và audit trước close |
| PRD-DEC-05 | Accepted — Product | Dashboard có tối thiểu tám view cốt lõi và chart extension theo capability | Không chart xếp hạng cá nhân; mọi chart giữ scope/filter/status và drill-down/table fallback |

## 3. Bất biến kiến trúc target

Các mục này không giải quyết các TBD chi tiết, nhưng là guardrail đã có trong
PRD/TDD:

| ID | Trạng thái | Bất biến |
|---|---|---|
| INV-01 | Target constraint | Backend tự resolve company/role/action/data scope; không tin `companyId`, role hoặc scope client gửi |
| INV-02 | Target constraint | Ingestion luôn qua source/provenance, normalization, validation và staging; không ghi thẳng official |
| INV-03 | Target constraint | Error chặn chốt; Warning chỉ override bởi người có quyền và có lý do |
| INV-04 | Target constraint | Official không bị sửa/ghi đè trực tiếp; correction tạo version mới và giữ version cũ tra cứu được |
| INV-05 | Target constraint | Close/reclose ghi official version, closure, audit và outbox trong cùng transaction; aggregation không chạy nặng trong transaction đó |
| INV-06 | Target constraint | Mọi background write có idempotency; queue delivery/retry không được tạo duplicate |
| INV-07 | Target constraint | Dashboard overview đọc aggregate/snapshot; drill-down truy được contributor → official → source/audit trong phạm vi quyền |
| INV-08 | Target constraint | KPI chỉ dùng formula type được code/cấu hình có kiểm soát; không dùng `eval()` hoặc công thức code do user nhập |
| INV-09 | Target constraint | Báo cáo lịch sử dùng hierarchy/standard/shift có hiệu lực tại thời điểm dữ liệu |
| INV-10 | Target constraint | Secret lấy từ environment/secret manager; không hardcode hoặc commit `.env` |
| INV-11 | Target constraint | Similarity/OCR output chỉ tạo candidate/evidence có version và giải thích; không trực tiếp mutate staging/official |
| INV-12 | Target constraint | Excel từ máy quản lý không tự động là official; web/Excel đều qua validation/staging/close, OCR luôn secondary trong baseline |
| INV-13 | Target constraint | Navigation label/chức danh không cấp quyền; backend resolve operational role + functional action + effective data scope |
| INV-14 | Target constraint | Chart chỉ đọc official snapshot/aggregate; unavailable/empty/partial/stale không được trình bày thành zero |

## 4. Lựa chọn Current đang được code hiện thực hóa

Các mục dưới đây là fact của repository, chưa tự động là ADR đã được ghi nhận:

| ID | Trạng thái | Current implementation | Việc cần làm |
|---|---|---|---|
| CUR-01 | Implemented, ADR pending | Backend Java 17 + Spring Boot 4.1 | Dùng làm evidence để đóng hoặc thay ADR-01 có chủ đích |
| CUR-02 | Implemented, ADR pending | PostgreSQL | Formalize ADR-02 trước khi thiết kế partition/index target |
| CUR-03 | Implemented, ADR pending | Spring Data JPA/Hibernate | Formalize ADR-04; xác định nơi được dùng SQL migration/index chuyên biệt |
| CUR-04 | Implemented, ADR pending | JWT HS512 stateless, DB-backed introspection/revocation/tokenVersion | Formalize ADR-07 và policy key rotation/session security |
| CUR-05 | Implemented, ADR pending | React 19 + TypeScript + Vite | Ghi ADR frontend nếu target cần ổn định lâu dài |
| CUR-06 | Implemented, ADR pending | API envelope `{code,message,result,...}` | Resolve với target `{success,data,error}` trước khi public API ổn định |
| CUR-07 | Implemented, ADR pending | Excel preview/import chạy đồng bộ và ghi staging DRAFT | Không gọi đây là source-batch/outbox pipeline |
| CUR-08 | Implemented, ADR pending | Approve từng staging tạo typed `ProductionReport` và official details đồng bộ | Cần migration plan sang closure/versioning target |
| CUR-09 | Implemented, ADR pending | KPI OEE/availability/performance/quality được hardcode khi approve | Không mở rộng formula trước PRD-TBD-01/KPI Dictionary |
| CUR-10 | Implemented, ADR pending | Dashboard aggregate trực tiếp từ official reports | Không đạt target snapshot/freshness/completeness nếu dữ liệu tăng |
| CUR-11 | Implemented, ADR pending | Hibernate `ddl-auto=update` + `schema.sql` chạy khi startup | Phải thay bằng migration versioned trước production |
| CUR-12 | Implemented, ADR pending | Frontend đang có `package-lock.json` | Workflow mới phải dùng pnpm; việc bỏ npm lockfile cần một thay đổi riêng có kiểm tra build |

Current config còn default development cho JWT signer và bootstrap admin.
Đây không phải lựa chọn production được chấp nhận. Production phải fail-safe khi
secret bắt buộc bị thiếu; việc loại default không cần domain decision.

## 5. Open Technical Decisions — TDD ADR

| ADR | Trạng thái chính thức | Câu hỏi | Current evidence, không phải quyết định cuối |
|---|---|---|---|
| ADR-01 | Open | Backend chính NestJS hay Spring Boot? | Repository đang dùng Spring Boot; nên formalize hoặc nêu migration rationale |
| ADR-02 | Open | PostgreSQL hay MySQL? | Repository/config/schema SQL đang phụ thuộc PostgreSQL |
| ADR-03 | Open | BullMQ/Redis, RabbitMQ hay cloud queue? | Chưa có queue/worker runtime |
| ADR-04 | Open | ORM/query builder nào? | Current dùng Spring Data JPA/Hibernate |
| ADR-05 | Open | Cache strategy và invalidation? | Chưa có cache; filter-hash invalidation và permission-aware cache còn phải thiết kế |
| ADR-06 | Open | Object storage dùng gì? | Current Excel xử lý trong request; một dormant flow lưu blob DB, không phải target object storage |
| ADR-07 | Open | JWT hay session? | Current dùng JWT; vẫn cần formal policy về signing key, refresh/revoke và browser storage |
| ADR-08 | Open cho sau MVP | Multi-tenant dùng shared DB hay DB per company? | PRD chỉ chốt single-tenant MVP; không cần chốt isolation tương lai lúc này |
| ADR-09 | Open — Blocker | Official data dùng typed tables, parent/child hay generic JSONB? | Current có typed report tables nhưng chưa có generic target version model |
| ADR-10 | Open — Blocker | Chỉ close grain thấp nhất hay cho phép close cấp cha với hierarchical lock? | Current chưa có public closure aggregate |
| ADR-11 | Open | Outbox processor cùng app worker hay service riêng? | Chưa có outbox table/processor/worker |
| ADR-12 | Open — Non-blocker sớm | Hot/raw retention và cơ chế cold tier? | Chỉ chặn khi volume/production retention yêu cầu |
| ADR-13 | Open — Blocker cho AI similarity dữ liệu thật | Rule-weighted, model cục bộ hay dịch vụ ngoài; version/explanation/precision-recall thế nào? | Exact dedup và Excel–web comparison deterministic vẫn làm độc lập được |

### Thứ tự đóng ADR kỹ thuật hợp lý

Đây là dependency order, không thay thế nội dung quyết định:

1. Formalize ADR-01, ADR-02, ADR-04 và ADR-07 từ current evidence hoặc quyết
   định migrate.
2. Đóng ADR-09 trước schema/migration official version.
3. Đóng ADR-10 sau khi PRD-TBD-04 và hierarchy model rõ.
4. Đóng ADR-03, ADR-06, ADR-11 trước source-batch/outbox worker.
5. Đóng ADR-05 trước tối ưu dashboard.
6. Đóng ADR-13 trước khi bật AI similarity trên dữ liệu thật; exact dedup và đối soát deterministic không phải chờ.
7. ADR-08 và ADR-12 chỉ khi nhu cầu tương ứng xuất hiện.

## 6. Open Product Decisions — PRD TBD

### 6.1. Blocker bắt buộc

Không implement phần phụ thuộc các mục sau bằng giả định của developer:

| TBD | Trạng thái | Nguồn quyết định | Cần quyết định |
|---|---|---|---|
| TBD-01 | Open — Blocker | Mentor / Quản lý điều hành | Công thức KPI chính thức; OEE đầy đủ hay công thức khác |
| TBD-02 | Open — Blocker | Mentor | MVP một hay nhiều nhà máy trong cùng công ty |
| TBD-04 | Open — Blocker | Quản lý vận hành | Coverage/dữ liệu bắt buộc để được close |
| TBD-05 | Open — Blocker | Quản lý vận hành | Ca qua đêm thuộc ngày nào; tuần báo cáo bắt đầu ngày nào |
| TBD-06 | Open — Blocker | Quản lý vận hành | Ca overlap, ngày nghỉ/ca đặc biệt và cửa sổ nhập lùi |
| TBD-07 | Open — Blocker | Quản trị dữ liệu | Identity/matching cuối cho production, state, incident, quality và plan |
| TBD-08 | Open — Blocker | Quản trị dữ liệu | File thật và mapping từng cột |
| TBD-09 | Open — Blocker | Quản trị dữ liệu | Merge/retry/replace policy khi upload lại file sửa |
| TBD-11 | Open — Blocker | Quản lý vận hành | Validation Error/Warning/Information và thresholds ban đầu |
| TBD-12 | Open — Blocker | Quản lý điều hành | Late-data window sau close |
| TBD-19 | Open — Blocker | Quản lý vận hành | Machine-state overlap/gap và incident đồng thời |
| TBD-20 | Open — Blocker | Quản lý vận hành | Event kéo dài qua ca được split hay giữ một record |

### 6.2. Blocker có điều kiện

| TBD | Trạng thái | Nguồn quyết định | Khi nào chặn |
|---|---|---|---|
| TBD-16 | Open — Conditional | Mentor / Quản trị dữ liệu | Khi import/backfill dữ liệu lịch sử |
| TBD-18 | Open — Conditional | Quản lý điều hành / Mentor | Khi cho phép KPI formula version tính lại hồi tố |
| TBD-22 | Open — Conditional | Quản trị dữ liệu / Quản lý vận hành | Khi bật OCR giấy: mẫu phiếu, confidence threshold và fixture ẩn danh |

### 6.3. Non-blocker hoặc sau MVP

| TBD | Trạng thái | Nguồn quyết định | Guardrail hiện tại |
|---|---|---|---|
| TBD-13 | Open — Non-blocker | Mentor / IT công ty | Phải chốt audit/export retention trước production |
| TBD-14 | Open — Sau MVP | Mentor / Người dùng pilot | Không làm voice nếu chưa có pain point/pilot |
| TBD-17 | Open — Non-blocker | Mentor / IT công ty | Đề xuất in-app cho MVP chưa phải quyết định cuối |
| TBD-21 | Open — Non-blocker sớm | Mentor / IT công ty | Rà lại trước khi raw/official storage đủ lớn |

### 6.4. Đã quyết định

| TBD | Kết quả |
|---|---|
| TBD-03 | Single-tenant MVP |
| TBD-10 | Reopen được duyệt một bước bởi Quản lý điều hành |
| TBD-15 | Không individual ranking/pay/discipline trong MVP |
| PRD-DEC-01..05 | Navigation/module, ba cấp role, dữ liệu lịch sử/similarity, source reconciliation và chart catalog theo PRD v2.5 |

## 7. Các quyết định hòa giải Current ↔ Target cần tạo

Những mục sau chưa có mã ADR trong TDD nhưng cần record riêng trước khi đổi
code:

| Candidate | Loại | Câu hỏi cần chốt |
|---|---|---|
| ADR-C01 — API envelope | Technical | Giữ `{code,result}` trong v1 hay migration sang `{success,data,error}`; compatibility window ra sao |
| ADR-C02 — Organization mapping | Product + Technical | `Department` Current ánh xạ thế nào vào target `factory → line → workGroup`; có giữ Department là extension không |
| ADR-C03 — Role mapping | Product + Security | Tám enum role Current ánh xạ vào ba role vận hành + functional roles thế nào; Ca trưởng/Tổ trưởng chuyển thành employee position ra sao mà không nâng quyền |
| ADR-C04 — Company context migration | Technical | Backfill `companyId`, unique constraints và server resolver cho dữ liệu Current |
| ADR-C05 — Workflow migration | Product + Technical | Chuyển `approve staging → official` hiện tại sang `ready_to_close → closure → versioned official` mà không mất dữ liệu |
| ADR-C06 — API resource migration | Technical | Giữ domain-specific staging routes, thêm canonical façade hay tạo API version mới |
| ADR-C07 — Schema migration tool | Technical | Flyway hay Liquibase; baseline dữ liệu do Hibernate tạo thế nào; forward/rollback policy |
| ADR-C08 — Extra modules | Product | HR/finance/inventory/maintenance/AI được duy trì như extension hay tách release boundary |
| ADR-C09 — Frontend token storage | Security | Chấp nhận localStorage với CSP/hardening hay đổi sang cookie/session architecture |
| ADR-C10 — Production secrets/bootstrap | Security/Operations | Cơ chế fail-fast, secret rotation và bootstrap admin không có default production |

### Bất biến không cần chờ quyết định

- Update một resource phải kiểm tra quyền trên **target scope sau update**, không
  chỉ scope của bản ghi cũ. Current staging update cần được xem là security gap
  vì có thể đổi team/hierarchy sau khi pre-authorization kiểm bản ghi cũ.
- Không commit `.env`, secret, password hoặc token.
- Không gọi entity/service dormant là API Current khi không có controller.
- Không dùng module personal KPI Current để đáp ứng KPI hiệu suất nhà máy của
  PRD.

## 8. Mẫu ghi một quyết định mới

```markdown
## D-001 — <Tên quyết định>

- Trạng thái: Proposed | Accepted | Superseded
- Ngày:
- Requirement/TBD liên quan:
- Phạm vi áp dụng:
- Bối cảnh:
- Các phương án đã cân nhắc:
- Lựa chọn:
- Ảnh hưởng tới PRD/TDD/API/schema:
- Test hoặc kiểm tra cần cập nhật:
- Bằng chứng xác nhận:
- Thay thế quyết định: Không | D-/ADR-...
```

Dùng `D-xxx` cho quyết định chung. Chỉ dùng `ADR-xx` khi đó là quyết định kiến
trúc kỹ thuật được theo dõi trong TDD; giữ cùng các trường tối thiểu ở trên.
Không dùng “đề xuất” như lựa chọn đã được xác nhận.

Khi quyết định thay đổi contract hoặc kiến trúc, cập nhật đồng thời tài liệu và
test liên quan. Chỉ tạo ExecPlan theo [PLANS.md](PLANS.md) nếu thay đổi kéo dài
nhiều milestone và có migration, sequencing, benchmark hoặc rollback phức tạp;
quyết định mới không tự động bắt buộc ExecPlan.
