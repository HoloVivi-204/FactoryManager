# Backlog hệ thống theo dõi hiệu suất nhà máy

## 1. Cách dùng backlog

Backlog này phân rã PRD v2.5 và TDD v1.5 thành các lát cắt dọc. Mỗi `FP-SLICE-*`
phải tạo được một hành vi người dùng chạy xuyên suốt dữ liệu, API, UI, quyền, audit và test.
Không tạo ticket riêng kiểu “làm toàn bộ database” hoặc “làm toàn bộ frontend”.

Quy ước:

- `Decision gate`: cần nguồn quyết định nghiệp vụ hoặc kỹ thuật chốt đầu vào trước khi triển khai.
- `Ready for implementation`: có đủ quyết định để nhóm triển khai thực hiện và kiểm chứng độc lập.
- `Blocked`: còn ít nhất một mục trong `Blocked by` chưa hoàn tất.
- `Blocked by`: stable ID của gate hoặc slice phải hoàn tất trước.
- `Must`: thuộc MVP acceptance scope.
- `Should`: chỉ làm sau khi toàn bộ Must đã đạt hoặc Product approver đổi thứ tự.

Chỉ đưa nhân sự/vật tư ở phạm vi vận hành vào Must; không đưa chấm công, lương, KPI cá nhân, tài chính,
kho đầy đủ, bảo trì đầy đủ hoặc chatbot vào lõi backlog này. OCR giấy, voice và export dashboard đi qua
capability riêng; Excel–web reconciliation vẫn là Must khi OCR chưa bật.

`factory-performance-feature-matrix.csv` là requirement matrix, mỗi dòng đúng một FR; file này
không nhúng scenario hoặc fixture. Scenario nằm ở `content-skeleton.yaml.acceptance_scenarios`
và nối tới acceptance của backlog qua `delivery_ref_ids`.

## 2. Definition of Done chung

Một slice chỉ hoàn tất khi:

- hành vi end-to-end chạy được trên UI và API trong đúng data scope;
- schema thay đổi qua migration versioned, không dựa vào `ddl-auto` để tạo contract production;
- API trả lỗi ổn định cho validation, authorization, conflict và not-found;
- hành động thay đổi nghiệp vụ có audit trong cùng transaction;
- unit/integration test phủ happy path, permission denial và lỗi nghiệp vụ chính;
- frontend build, backend test và contract liên quan đều pass;
- các ID PRD trong slice được cập nhật bằng evidence ở feature matrix.

## 3. Decision gates

### FP-GATE-001 — Chốt phạm vi nhà máy, ngày sản xuất và coverage

- **Readiness:** Decision gate
- **Priority:** Blocker
- **Blocked by:** None
- **PRD:** `TBD-02`, `TBD-04`, `TBD-05`, `TBD-06`
- **Nguồn quyết định:** Mentor và Quản lý vận hành

#### What to decide

Chốt số nhà máy trong release, quy tắc ca đêm thuộc ngày nào, tuần bắt đầu ngày nào, ca có được
overlap không, giới hạn nhập lùi và tập máy/ca/tổ bắt buộc để một scope được xem là complete.

#### Acceptance criteria

- [ ] Mỗi câu hỏi có nguồn quyết định, nội dung quyết định, ngày hiệu lực và ví dụ được duyệt.
- [ ] Có ít nhất một ví dụ ca ngày, ca qua đêm, ngày nghỉ và scope thiếu dữ liệu.
- [ ] Thuật toán `productionDate` và `expected coverage` có expected input/output cụ thể.
- [ ] Quyết định được phản ánh vào `DECISIONS.md`, PRD hoặc phụ lục được PRD tham chiếu.

### FP-GATE-002 — Chốt canonical records, identity và import contract

- **Readiness:** Decision gate
- **Priority:** Blocker
- **Blocked by:** `FP-GATE-001`
- **PRD:** `TBD-07`, `TBD-08`, `TBD-09`, `TBD-11`, `TBD-19`, `TBD-20`
- **TDD:** `ADR-09`; `ADR-13` chỉ chặn cảnh báo AI trên dữ liệu thật
- **Nguồn quyết định:** Quản trị dữ liệu và Quản lý vận hành

#### What to decide

Chốt record types của MVP, typed schema, logical identity/matching rule, overlap/gap của
machine-state, event qua nhiều ca, file mẫu thật, mapping cột, `businessDataCode`, bốn lớp
`dataTimeClass`, severity rule và cách merge khi upload lại. Exact matching/deterministic
reconciliation không phải chờ ADR-13; AI similarity phải chờ ADR-13.

#### Acceptance criteria

- [ ] Mỗi record type có canonical fields, required fields và stable identity hoặc matching rule.
- [ ] File mẫu thật có template version và expected result theo từng dòng.
- [ ] Có ví dụ same-value dedup, different-value conflict và record đã sửa tay.
- [ ] Có ví dụ Current, Historical Backfill, Late Arrival, Correction và ứng viên giống 96%.
- [ ] File Excel và dữ liệu web có expected Matched/Mismatch/Missing theo logical identity.
- [ ] Có quyết định rõ dòng lỗi được lưu ở source error hay staging rejected.
- [ ] `ADR-09` chọn typed tables hoặc parent/child; không dùng generic JSONB nếu chưa có lý do duyệt.

### FP-GATE-003 — Duyệt KPI Dictionary đầu tiên

- **Readiness:** Decision gate
- **Priority:** Blocker
- **Blocked by:** `FP-GATE-001`, `FP-GATE-002`
- **PRD:** `TBD-01`, `FR-KPI-01`, `FR-KPI-03`
- **Conditional decision:** `TBD-18` chỉ áp dụng khi yêu cầu tính lại lịch sử; mặc định không
  hồi tố không chặn gate.
- **Nguồn quyết định:** Mentor và Quản lý điều hành

#### What to decide

Chọn một KPI đầu tiên, công thức, required fields, weighting, aggregation theo kỳ, missing-data
rule, rounding, thresholds và effective date. Xác nhận mặc định không hồi tố; chỉ chốt thêm
`TBD-18` nếu release yêu cầu tính lại lịch sử.

#### Acceptance criteria

- [ ] KPI có mã ổn định, version, người duyệt và ngày hiệu lực.
- [ ] Có ít nhất ba ví dụ tính tay: complete, missing và partial.
- [ ] Nêu rõ aggregation cấp máy/tổ/dây chuyền/nhà máy và ngày/tháng.
- [ ] Xác nhận không có KPI hay ranking cá nhân trong MVP.
- [ ] Quyết định hồi tố mặc định là không hồi tố nếu chưa có phê duyệt riêng.

### FP-GATE-004 — Chốt closure/outbox và release profile

- **Readiness:** Decision gate
- **Priority:** Blocker trước production
- **Blocked by:** `FP-GATE-001`, `FP-GATE-002`
- **TDD:** `ADR-10`, `ADR-11`; Technical Acceptance Gates
- **Nguồn quyết định:** Nhóm kỹ thuật, Mentor và IT công ty

#### What to decide

Chọn grain chốt trực tiếp, hierarchical locking, cách chạy outbox processor, profile tải,
browser support, RPO và RTO.

#### Acceptance criteria

- [ ] Có lock matrix cho factory, line và group.
- [ ] Có sequence diagram transaction/outbox cho import và close/reclose.
- [ ] Profile tải có số record, file rows, concurrent import/users và refresh frequency cụ thể.
- [ ] Có browser matrix, RPO và RTO được Product/IT duyệt.

## 4. Must vertical slices

### FP-SLICE-001 — Đăng nhập và giới hạn dữ liệu theo role/scope target

- **Readiness:** Ready for implementation
- **Priority:** Must
- **Blocked by:** None
- **PRD:** `FR-CFG-04`, `FR-NAV-01`, `FR-NAV-02`, `FR-NAV-03`, `FR-MDM-04`,
  `FR-KPI-02`, `FR-DASH-01`, `FR-SEC-01`, `FR-SEC-02`, `FR-SEC-03`, `FR-SEC-05`, `FR-AUD-02`

#### What to build

Cho một Nhân viên vận hành đăng nhập và chỉ xem/ghi dữ liệu được giao; Quản lý vận hành xem phạm
vi phụ trách; Quản lý điều hành xem phạm vi công ty/nhà máy. Quản trị hệ thống gán role/action/scope;
auditor có quyền đọc audit phù hợp. Server resolve company/scope từ authentication, không tin tham số client.

#### Acceptance criteria

- [ ] Tám enum Current được map có chủ đích sang ba role vận hành; `DATA_ADMIN`, `SYSTEM_ADMIN` và `AUDITOR` có action cụ thể.
- [ ] `TEAM_LEADER`/Ca trưởng/Tổ trưởng chuyển thành employee position có effective date, không tự cấp role.
- [ ] Navigation dùng đúng module key/label PRD 4.4; tab disabled hoặc ngoài quyền không hiện như capability sẵn sàng.
- [ ] Mọi dữ liệu target có company context do server resolve.
- [ ] Sửa URL, body hoặc query không cho user vượt scope.
- [ ] UI không hiển thị KPI/ranking cá nhân trong workspace Factory Performance.
- [ ] Thay role/scope vô hiệu hóa token/quyền cache cũ và tạo audit.
- [ ] Integration test chứng minh allow/deny ở factory, line, group và record.

### FP-SLICE-002 — Cấu hình ngày sản xuất và lịch ca có hiệu lực

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-001`, `FP-SLICE-001`
- **PRD:** `FR-CFG-01`, `FR-CFG-05`

#### What to build

Quản trị dữ liệu cấu hình timezone, production-day rule, ca ngày/đêm, ngày nghỉ và thuật ngữ;
form nhập tự suy ra hoặc kiểm tra đúng production date.

#### Acceptance criteria

- [ ] Company settings có version/effective date và `companyId`.
- [ ] Ca qua nửa đêm được gắn đúng production date theo quyết định đã duyệt.
- [ ] Không cho hai cấu hình ca hiệu lực trái rule overlap.
- [ ] UI xem được cấu hình hiện hành và lịch sử; quyền sửa chỉ dành cho role được cấp.
- [ ] Unit test phủ ca ngày, ca đêm, ngày nghỉ và thay đổi lịch theo thời gian.
- [ ] Thay đổi cấu hình tạo audit before/after.

### FP-SLICE-003 — Danh mục lõi có effective dating

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-001`, `FP-GATE-002`, `FP-SLICE-001`
- **PRD:** `FR-CFG-03`, `FR-MDM-01`, `FR-MDM-02`, `FR-MDM-05`, `FR-MDM-06`, `FR-AUD-02`

#### What to build

Quản trị nhà máy, dây chuyền, tổ, máy, sản phẩm, chức danh/phân công vận hành, định mức, taxonomy
trạng thái máy và nguyên nhân dừng với ngày hiệu lực; báo cáo quá khứ luôn resolve cơ cấu/chức danh tại ngày sản xuất.

#### Acceptance criteria

- [ ] Máy và định mức có effective intervals không overlap.
- [ ] Máy đã được tham chiếu chỉ được deactivate, không hard-delete.
- [ ] Chuyển máy sang dây chuyền mới không làm đổi báo cáo trước ngày chuyển.
- [ ] Đổi một Nhân viên vận hành thành Ca trưởng/Tổ trưởng không tạo security role mới và không làm đổi dữ liệu lịch sử.
- [ ] Taxonomy trạng thái máy và nguyên nhân dừng cấu hình theo company/effective date mà không sửa code.
- [ ] Product dimension chỉ chuyển `enabledForAnalysis=true` sau khi catalog và mapping vượt kiểm
  tra required code, uniqueness và unresolved Error; nếu không thì dùng `N/A`.
- [ ] Mọi query danh mục áp dụng company và data scope.
- [ ] Thay đổi master, định mức, taxonomy hoặc product-analysis state tạo audit before/after.
- [ ] Integration test chứng minh historical lookup và no-hard-delete.

### FP-SLICE-004 — Nhập tay một báo cáo ca vào staging có provenance

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-SLICE-001`, `FP-SLICE-002`, `FP-SLICE-003`
- **PRD:** `FR-ING-01`, `FR-ING-03`, `FR-VAL-01`

#### What to build

Nhân viên vận hành (có thể được phân công chức danh Ca trưởng/Tổ trưởng) nhập production output
cho một máy/sản phẩm/ca, lưu Draft rồi submit thành staging;
dữ liệu giữ actor, source type và source record link nhưng chưa xuất hiện trong official KPI.

#### Acceptance criteria

- [ ] Form bắt buộc production date, shift, group, machine, product và operating status.
- [ ] Quan hệ tổ/máy/ca được resolve theo effective date và data scope.
- [ ] Draft sửa được; submitted staging có nhãn “chưa chính thức”.
- [ ] Dashboard official không đọc staging record.
- [ ] Create/update/submit có audit và request ID.
- [ ] E2E test chạy từ UI nhập liệu đến staging detail.

### FP-SLICE-005 — Validation severity và màn hình blocker trước chốt

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-002`, `FP-SLICE-002`, `FP-SLICE-004`
- **PRD:** `FR-CFG-03`, `FR-VAL-02`, `FR-NOTI-02`
- **Open non-blocker:** `TBD-17` quyết định kênh; in-app chỉ là giả định tạm được đề xuất.

#### What to build

Chạy validation rules versioned, lưu finding theo Error/Warning/Information và hiển thị chính xác
máy/ca/tổ đang chặn chốt. Đến deadline chốt theo production calendar, hệ thống gửi reminder
cho quản lý đúng scope qua kênh đã chốt hoặc giả định tạm được ghi nhận. Người có quyền có thể nhập
lý do override Warning.

#### Acceptance criteria

- [ ] Error, Warning và Information là dữ liệu lưu được, không chỉ là message tạm.
- [ ] Threshold/severity rule cấu hình theo company, có version/effective date và không cần sửa code.
- [ ] Error chặn Ready to Close; Warning cần actor có quyền và lý do không rỗng.
- [ ] Closure preflight trả expected coverage, missing scopes và findings.
- [ ] UI phân biệt chưa nhập, đang import, không sản xuất xác nhận và chờ nhập bù.
- [ ] Khi tới deadline mà scope chưa ready/closed, scheduler gửi một reminder cho Quản lý vận hành
  đúng scope, kèm blocker list; retry không tạo thông báo trùng.
- [ ] Kênh reminder khớp quyết định `TBD-17`; nếu dùng tạm in-app, giả định có phạm vi và ngày hết hạn.
- [ ] Scope đã ready/closed trước deadline không nhận reminder thiếu dữ liệu.
- [ ] Warning override và thay đổi rule có audit.
- [ ] Test phủ Error, Warning override, Information không chặn và reminder đúng deadline/scope.

### FP-SLICE-006 — Chốt hoàn chỉnh thành official version bằng transaction/outbox

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-004`, `FP-SLICE-005`
- **PRD:** `FR-CLO-01`, `FR-AUD-01`

#### What to build

Quản lý vận hành chốt một scope đủ coverage. Transaction kiểm tra lại blocker, tạo official
version, closure state, audit và outbox aggregation; không chạy KPI nặng trong transaction.

#### Acceptance criteria

- [ ] Cùng company/date/scope chỉ có một close mutation chạy tại một thời điểm.
- [ ] Staging mới xuất hiện sau preflight làm close trả conflict và yêu cầu refresh.
- [ ] Official record lưu provenance và không sửa trực tiếp.
- [ ] Commit thành công nhưng queue tạm lỗi vẫn còn outbox event để retry.
- [ ] Close lặp với cùng idempotency key không tạo official version thứ hai.
- [ ] E2E test chứng minh staging không official trước chốt và official sau chốt.

### FP-SLICE-007 — Một KPI được duyệt, dashboard snapshot và drill-down

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-003`, `FP-SLICE-006`
- **PRD:** `FR-CFG-03`, `FR-KPI-01`, `FR-KPI-02`, `FR-KPI-03`, `FR-KPI-05`,
  `FR-DASH-01`, `FR-DASH-04`, `FR-DASH-05`, `FR-DASH-08`, `FR-SEC-03`

#### What to build

Sau close, worker tính KPI đã duyệt vào snapshot. Người quản lý xem overview theo scope,
highlight khu vực dưới ngưỡng và drill-down qua contributor đến official/source/audit summary.

#### Acceptance criteria

- [ ] KPI definition/version chứa formula type, aggregation, missing, rounding và approval.
- [ ] KPI enablement cấu hình theo company/version; KPI bị tắt không được schedule hoặc render.
- [ ] Công thức chạy trong KPI engine; không `eval` và không hardcode trong controller.
- [ ] Snapshot có scope, period, KPI version, official source version, freshness và completeness.
- [ ] Overview đọc snapshot, không tải toàn bộ production reports để tính trong request.
- [ ] Drill-down chỉ trả contributor trong quyền người dùng.
- [ ] Chart catalog có các code cốt lõi đã duyệt; unavailable/empty/partial/stale không bị đổi thành series zero.
- [ ] Kết quả khớp tuyệt đối ví dụ tính tay đã duyệt.

### FP-SLICE-008 — Import file lỗi một phần, dedup từng dòng và thông báo kết quả

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-002`, `FP-SLICE-003`, `FP-SLICE-005`
- **PRD:** `FR-CFG-02`, `FR-ING-02`, `FR-ING-03`, `FR-ING-05`, `FR-ING-06`,
  `FR-VAL-06`, `FR-NOTI-03A`

#### What to build

Người có quyền upload file theo template versioned. Outbox worker parse/mapping; dòng hợp lệ vào
staging, dòng lỗi ở source error, kết quả được thông báo. Mỗi record có businessDataCode/dataTimeClass;
upload lại file đã sửa merge từng logical record mà không nhân đôi dòng cũ. File Excel được đối soát
deterministic với dữ liệu web ngay cả khi OCR chưa bật.

#### Acceptance criteria

- [ ] File gốc, source batch, template version và uploader được lưu theo lifecycle TDD.
- [ ] File 100 dòng với 95 hợp lệ tạo 95 staging records và 5 row errors.
- [ ] Người dùng xem/tải lỗi theo dòng và trường.
- [ ] Upload lại đủ 100 dòng không tạo lại 95 record cũ.
- [ ] Record đã sửa tay hoặc khác giá trị chuyển conflict, không bị ghi đè.
- [ ] Current/Historical Backfill/Late Arrival/Correction được derive từ business time và closure state.
- [ ] Excel–web tạo đúng Matched/Mismatch/Missing; retry cùng idempotency key không tạo run/item trùng.
- [ ] Test queue/storage tạm lỗi chứng minh outbox retry không tạo duplicate.

### FP-SLICE-009 — Review và resolve xung đột nhiều nguồn

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-SLICE-005`, `FP-SLICE-008`
- **PRD:** `FR-VAL-03`, `FR-VAL-04`, `FR-VAL-05`, `FR-VAL-06`, `FR-AUD-01`

#### What to build

Khi form và file có cùng matching result nhưng khác giá trị, tạo conflict group. Với identity chưa
khớp hoàn toàn, scorer versioned tạo warning từ 90% đến dưới 100%. Người được cấp quyền xem từng
field/source/actor/time/score explanation và quyết định link, giữ riêng, tạo conflict hoặc sửa.

#### Acceptance criteria

- [ ] Same identity/same value chỉ link provenance, không tạo conflict hoặc duplicate.
- [ ] Different value tạo `needs_review` và không tự ghi đè.
- [ ] Score 0.8999 không tạo warning mặc định; 0.90 và 0.9999 tạo warning; score 1.00 không bypass identity/conflict.
- [ ] AI/model không có command tự merge/xóa/ghi đè; policy/model version và feature explanation được lưu.
- [ ] Item reconciliation bất thường phải resolve có lý do trước close; OCR nếu bật chỉ là evidence thứ yếu.
- [ ] UI so sánh current/candidate theo từng field.
- [ ] Resolver role/action được kiểm tra server-side.
- [ ] Resolve lặp cùng request ID là idempotent.
- [ ] Quyết định và lý do được audit; candidate cũ vẫn tra cứu được.

### FP-SLICE-010 — Chốt một phần với coverage cấp cha

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-SLICE-006`, `FP-SLICE-007`
- **PRD:** `FR-CLO-02`

#### What to build

Cho phép chốt một tổ/dây chuyền đủ điều kiện trong khi scope khác còn mở. Dashboard cấp cha hiển
thị Partial, coverage và phần thiếu; không trình bày như Complete.

#### Acceptance criteria

- [ ] Hierarchical lock ngăn close factory và child scope xung đột đồng thời.
- [ ] Snapshot cấp cha có expected/closed/missing child counts.
- [ ] UI nêu rõ Partial và danh sách scope chưa chốt.
- [ ] Không so sánh Partial với Complete nếu policy không cho phép.
- [ ] Khi child cuối cùng chốt, parent chuyển Complete và snapshot được tính lại.
- [ ] Integration test phủ hai line, một closed và một open.

### FP-SLICE-011 — Mở lại, correction workspace và chốt lại

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `TBD-12`, `FP-GATE-004`, `FP-SLICE-006`, `FP-SLICE-007`
- **PRD:** `FR-ING-03`, `FR-CLO-03`, `FR-CLO-04`, `FR-AUD-01`

#### What to build

Người có quyền tạo reopen request; quản lý nhà máy duyệt một cấp; sửa chỉ trong correction
workspace. Mỗi giá trị sửa giữ correction provenance tới official version trước, actor, reason và
source record; reclose tạo official version mới và tính lại mọi ancestor bị ảnh hưởng.

#### Acceptance criteria

- [ ] Không endpoint nào sửa trực tiếp official current.
- [ ] Reopen request có reason và affected scope; chỉ factory manager duyệt/từ chối.
- [ ] Dashboard giữ official gần nhất và hiện “đang điều chỉnh”.
- [ ] Mỗi corrected value giữ previous official version, source type `correction`, actor, reason và
  source record để drill-down provenance.
- [ ] Reclose tạo version mới, supersede version cũ và đánh dấu report adjusted.
- [ ] Snapshot scope trực tiếp, line và factory ancestor cùng chuyển stale trong transaction.
- [ ] Worker tính lại bottom-up; audit liệt kê mọi scope stale/recalculated.

### FP-SLICE-012 — Dashboard đủ bộ lọc và so sánh kỳ

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-SLICE-003`, `FP-SLICE-007`, `FP-SLICE-010`
- **PRD:** `FR-MDM-05`, `FR-KPI-03`, `FR-DASH-02`, `FR-DASH-03`, `FR-DASH-05`, `FR-DASH-08`

#### What to build

Người quản lý lọc snapshot theo factory/line/group/machine/shift/product/downtime reason và khoảng
ngày; xem chênh lệch với kỳ liền trước có cùng scope/filter khi hai kỳ đủ điều kiện.

#### Acceptance criteria

- [ ] Mọi filter được áp dụng ở server và nằm trong data scope.
- [ ] Product filter chỉ xuất hiện khi product dimension có `enabledForAnalysis=true`; nếu catalog
  hoặc mapping chưa đáng tin, API/UI không cung cấp filter này và dùng product `N/A`.
- [ ] Production date, không phải timestamp nhập, quyết định kỳ báo cáo.
- [ ] Comparison dùng cùng KPI version hoặc nêu rõ version khác nhau.
- [ ] Partial/stale/missing không bị trình bày như so sánh Complete.
- [ ] Top downtime reasons và danh sách máy/tổ dưới ngưỡng cùng drill-down được.
- [ ] Tối thiểu tám view cốt lõi dùng cùng filter/scope/completeness/freshness và có drill-down hoặc table fallback.
- [ ] Test phủ month boundary, ca đêm, product và permission tampering.

### FP-SLICE-013 — Audit đầy đủ và tra cứu theo vai trò kiểm toán

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-SLICE-001`, `FP-SLICE-006`, `FP-SLICE-008`, `FP-SLICE-009`,
  `FP-SLICE-011`
- **PRD:** `FR-AUD-01`, `FR-AUD-02`, `FR-AUD-03`

#### What to build

Chuẩn hóa audit append-only cho auth, master, mapping, staging, import, warning, conflict, close,
reopen/reclose, KPI definition và role/scope; auditor xem hoặc xuất audit theo quyền.

#### Acceptance criteria

- [ ] Audit có company, actor, action, entity, before/after, reason, request ID và time.
- [ ] Login failed/account lock, master/standard và role/scope đều được ghi.
- [ ] Không có update/delete audit qua business API.
- [ ] Auditor chỉ đọc trong scope; tổ trưởng không xem audit toàn nhà máy.
- [ ] Export audit áp dụng cùng scope/filter như màn hình, từ chối người không có quyền và tự tạo
  audit event cho lần export thành công.
- [ ] Secret, token và file content không xuất hiện trong log/audit.
- [ ] Test rollback chứng minh audit không commit khi business mutation thất bại.

### FP-SLICE-014 — Đóng technical acceptance gates cho MVP

- **Readiness:** Blocked
- **Priority:** Must
- **Blocked by:** `FP-GATE-004`, `FP-SLICE-001` đến `FP-SLICE-013`
- **PRD:** NFR và tiêu chí thành công
- **TDD:** Technical Acceptance Gates

#### What to build

Biến các gate kỹ thuật thành bằng chứng chạy lặp lại được: migration, security configuration,
performance, monitoring, backup/restore và E2E.

#### Acceptance criteria

- [ ] Không có secret/admin password dùng được làm fallback trong production profile.
- [ ] Migration chạy từ database rỗng và nâng cấp được từ baseline hỗ trợ.
- [ ] Dashboard overview đạt p95 dưới 3 giây và drill-down dưới 5 giây theo profile đã duyệt.
- [ ] Có metric/alert cho import, aggregation, queue, closure lateness và missing count.
- [ ] Backup/restore staging được diễn tập; RPO/RTO có evidence.
- [ ] Toàn bộ scenario PRD Must và permission negative tests chạy trong CI.

## 5. Should vertical slices

Các slice này chỉ bắt đầu sau `FP-SLICE-014`, trừ khi Product approver đổi scope bằng quyết định
được ghi nhận.

### FP-SLICE-101 — Import danh mục ban đầu

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-003`, `FP-SLICE-008`
- **PRD:** `FR-MDM-03`

#### Acceptance criteria

- [ ] Import master data dùng template versioned, row errors và effective-date validation.
- [ ] Dòng hợp lệ/lỗi tuân cùng chính sách partial import đã duyệt.
- [ ] Không tạo overlap hoặc hard-delete reference lịch sử.
- [ ] Có audit và contract test với file mẫu.

### FP-SLICE-102 — Đóng kỳ có ngoại lệ

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-010`
- **PRD:** `FR-CLO-05`

#### Acceptance criteria

- [ ] Chỉ role được cấp mới phê duyệt ngoại lệ với reason.
- [ ] Kỳ mang `closed_with_exceptions`, không `closed_complete`.
- [ ] Snapshot và UI nêu coverage, missing scopes và lý do.
- [ ] Comparison không che giấu khác biệt completeness.

### FP-SLICE-103 — Ngưỡng màu KPI theo scope/loại máy

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-007`
- **PRD:** `FR-KPI-04`

#### Acceptance criteria

- [ ] Threshold versioned theo company, KPI và scope/type.
- [ ] Dashboard dùng threshold có hiệu lực tại kỳ.
- [ ] Thay threshold có audit và không tự viết lại snapshot lịch sử.
- [ ] Có test fallback threshold và effective date.

### FP-SLICE-104 — Giữ bộ lọc và so sánh hai máy/tổ

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-012`
- **PRD:** `FR-DASH-06`, `FR-DASH-07`

#### Acceptance criteria

- [ ] Filter giữ nguyên khi chuyển biểu đồ/tab trong phiên phân tích.
- [ ] Chỉ so sánh hai scope khi user có quyền cả hai.
- [ ] Hai bên dùng cùng kỳ, KPI version và filter.
- [ ] URL/state có thể chia sẻ mà không làm lộ scope không được phép.

### FP-SLICE-105 — Nhắc chủ động và escalation quá hạn

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-005`, `FP-SLICE-010`
- **PRD:** `FR-NOTI-01`, `FR-NOTI-04`
- **Open non-blocker:** `TBD-17` quyết định in-app, email hoặc kênh tích hợp khác.

#### Acceptance criteria

- [ ] Job xác định đúng máy/ca thiếu theo production calendar.
- [ ] Nhắc gần hết ca và escalation sau delay cấu hình, không gửi trùng.
- [ ] Recipient được resolve theo role/scope.
- [ ] Kênh gửi khớp `TBD-17`; in-app không được coi là contract đã khóa khi quyết định còn Open.
- [ ] Không gửi dữ liệu nhạy cảm thừa qua bất kỳ kênh nào.

### FP-SLICE-106 — Chính sách lưu audit

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-013`, quyết định `TBD-13`
- **PRD:** `FR-AUD-04`

#### Acceptance criteria

- [ ] Retention được cấu hình theo company và có giá trị đã duyệt.
- [ ] Archival/purge không làm mất audit còn nằm trong kỳ bắt buộc.
- [ ] Source file của official data không bị xóa trước policy liên quan.
- [ ] Job retention có dry-run, audit và test khôi phục mẫu.

### FP-SLICE-107 — Biểu đồ theo capability vật tư, kho và bảo trì

- **Readiness:** Blocked
- **Priority:** Should
- **Blocked by:** `FP-SLICE-012` và capability tương ứng được bật
- **PRD:** `FR-DASH-09`, `FR-NAV-03`

#### Acceptance criteria

- [ ] `material-variance`, `inventory-flow`, `maintenance-backlog` và `maintenance-plan-completion` chỉ xuất hiện khi module, dữ liệu và quyền tương ứng sẵn sàng.
- [ ] API trả `unavailable_capability` thay vì dữ liệu giả khi chart chưa bật.
- [ ] Mỗi chart dùng cùng scope/filter/status contract và có drill-down hoặc bảng dữ liệu tương đương.
- [ ] Không dùng chart extension để bù tám view cốt lõi còn thiếu hoặc hiển thị KPI cá nhân.

## 6. Quy tắc chọn việc tiếp theo

1. Hoàn tất gate đang chặn slice có thứ tự thấp nhất.
2. Ưu tiên một slice chạy end-to-end trước khi mở nhiều slice cùng module.
3. Không xây KPI/dashboard chính thức trước `FP-GATE-003`.
4. Không xây import production trước `FP-GATE-002`.
5. Không gọi MVP hoàn tất nếu `FP-SLICE-014` chưa có evidence.
6. Mỗi lần merge, cập nhật trạng thái/evidence trong feature matrix; không suy diễn từ tên class.
