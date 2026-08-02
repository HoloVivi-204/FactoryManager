# Screen Map FactoryManager

## 1. Cách đọc

Trạng thái:

- `Current`: route/component có trong frontend.
- `Partial`: có màn hình nhưng chưa đủ hành vi PRD hoặc còn placeholder.
- `Broken`: route/component có trong frontend nhưng integration hiện tại không hoàn thành hành trình chính.
- `Target`: cần xây theo PRD/TDD, chưa được coi là endpoint/màn hình hiện có.
- `Target — Open`: hành vi target có trong PRD nhưng route hoặc channel còn phụ thuộc quyết định mở.
- `Out of MVP`: có thể có trong code nhưng ngoài phạm vi PRD hiệu suất nhà máy.

Route hiện tại dùng mẫu `/workspace/:roleSlug/:pageKey`. Khi chưa đăng nhập, ứng dụng render màn hình login;
không có route `/login` riêng. Client kiểm tra role để điều hướng, còn quyền dữ liệu phải được kiểm tra lại
ở backend.

Target không giữ nguyên tám role slug Current như contract sản phẩm. Ba role vận hành là
`executive-manager`, `operations-manager`, `operations-staff`; Ca trưởng/Tổ trưởng là employee position
trong Quản lý nhân sự. Migration route/role cần compatibility plan, không đổi slug im lặng.

## 2. Hành trình MVP đích

```text
Đăng nhập
  → chọn scope/kỳ
  → nhập tay hoặc upload file
  → phân loại Current/Historical Backfill/Late Arrival/Correction
  → đối soát Excel–web, cảnh báo gần trùng và tùy chọn OCR thứ yếu
  → theo dõi source batch và lỗi theo dòng
  → review staging, warning và conflict
  → kiểm tra coverage rồi close
  → tính KPI snapshot
  → dashboard/so sánh/drill-down
  → source record và audit

Nếu phát hiện sai sau close:
  dashboard giữ official gần nhất
  → tạo reopen request
  → duyệt
  → sửa correction workspace
  → reclose thành version mới
  → snapshot stale/recalculate
  → dashboard gắn Adjusted
```

### 2.1. Navigation target

| Module key | Label chuẩn | Nội dung | Enablement |
|---|---|---|---|
| `executive-overview` | Tổng quan điều hành | KPI, coverage, cảnh báo, việc cần xử lý | MVP |
| `production-management` | Quản lý sản xuất | Kế hoạch–thực tế, người + máy, báo cáo, chốt | MVP |
| `operations-management` | Quản lý vận hành | Trạng thái máy, downtime, sự cố | MVP |
| `people-management` | Quản lý nhân sự | Hồ sơ, phân công, chức danh Ca trưởng/Tổ trưởng | MVP phạm vi vận hành |
| `materials-management` | Quản lý vật tư | Danh mục, sử dụng, sự cố vật tư sản xuất | MVP phạm vi vận hành |
| `warehouse-management` | Quản lý kho | Nhập–xuất–tồn, kiểm kê | Capability sau MVP |
| `maintenance-scheduling` | Bảo trì & lập lịch | Yêu cầu, lịch, phiếu, lịch sử | Capability sau MVP |
| `data-reconciliation` | Dữ liệu & đối soát | Import, lịch sử, near-duplicate, OCR/conflict | Excel–web MVP; OCR conditional |

Không có module key riêng cho Ca trưởng/Tổ trưởng. Menu chỉ render module đã bật và được phép; gọi API trực tiếp vẫn phải qua server authorization.

## 3. Màn hình hiện tại trong phạm vi lõi

| Route/page key | Vai trò hiện tại | Component | Trạng thái | Ghi chú |
|---|---|---|---|---|
| mọi route khi chưa auth | Tất cả | `LoginPage` | Current | Username/password, error kết nối |
| `/workspace/admin/admin-*` | ADMIN | `AdminMasterPage` | Partial | CRUD master data; chưa có company settings/effective-history đầy đủ |
| `/workspace/director/overview` | DIRECTOR | `ExecutiveDashboardPage` | Partial | Dashboard điều hành hiện tại; role này không nằm trong sáu role PRD mặc định |
| `/workspace/{role}/overview` | Các role vận hành | `RoleOverviewPage` | Partial | Tổng quan theo role; lỗi/missing summary có thể bị hiển thị thành `0`, chưa phải KPI snapshot dashboard |
| `/workspace/team-leader/entry` | TEAM_LEADER | `TeamLeaderReportPage` | Partial | Tạo/sửa staging và chi tiết; chưa có validation severity/conflict |
| `/workspace/{manager}/approval` | FACTORY_MANAGER, PRODUCTION_MANAGER | `ApprovalPage` | Partial | Approve từng record, chưa phải closure theo ngày/scope |
| `/workspace/{role}/reports` | Role có menu | `ReportsPage` | Broken | Client gọi `/production-reports/search/my-scope`, nhưng backend không có route này; không demo/nghiệm thu |
| `/workspace/{role}/machines` | Role có menu | `OperationalPage` | Partial | Dữ liệu máy lấy từ phạm vi report hiện có |
| `/workspace/{role}/downtime` | Role có menu | `OperationalPage` | Partial | Chi tiết downtime theo staging/report |
| `/workspace/{role}/quality` | Role có menu | `OperationalPage` | Partial | Chi tiết chất lượng |
| `/workspace/{role}/people` | Role có menu | `OperationalPage` | Partial | Nhân sự vận hành; không dùng để ranking cá nhân |
| `/workspace/{role}/materials` | Role có menu | `OperationalPage` | Partial | Sự cố vật tư vận hành, không phải inventory |

Các label/role trong bảng Current là bằng chứng code hiện tại, không phải tên target đã được PRD v2.5 duyệt.

Mọi màn hình trên phải hỗ trợ loading, empty, error và forbidden state. Màn hình có số KPI không được dùng
`0` để thay loading hoặc missing data.

Known defect hiện tại: `RoleOverviewPage` không render `summary.error` sau khi loading kết thúc, còn formatter
đổi giá trị thiếu thành `0`/`0.00%`. Cho tới khi sửa, chỉ diễn giải số zero sau khi đã xác nhận API trả thành
công và field tương ứng thực sự có giá trị zero.

## 4. Màn hình MVP cần bổ sung hoặc tái thiết kế

Tên route dưới đây là contract định hướng; chưa phải route đã triển khai.

| Route target | Vai trò sản phẩm | Requirement | Trạng thái | Dữ liệu/API cần có |
|---|---|---|---|---|
| `/workspace/data-admin/company-settings` | Data Admin | FR-CFG-01..05 | Target | Company settings, calendar, shift version, terminology |
| `/workspace/data-admin/import-templates` | Data Admin | FR-CFG-02 | Target | Template/version/field mappings/pre-check |
| `/workspace/data-admin/validation-rules` | Data Admin | FR-CFG-03, FR-VAL-02 | Target | Severity, threshold, effective scope/date |
| `/workspace/admin/access-control` | System Admin | FR-MDM-04, FR-SEC-02 | Target | User, role, permission và data-scope assignment |
| `/workspace/data-admin/master-data` | Data Admin | FR-MDM-01..05 | Target | Effective-dated hierarchy, product/N/A, deactivate/history |
| `/workspace/production/entry` | Operations Staff, Operations Manager | FR-ING-01 | Partial | Canonical staging record + provenance |
| `/workspace/data/imports` | Operations/Executive Manager, Data Admin | FR-ING-02..06 | Target | Upload, source batch, time class, progress, summary, retry |
| `/workspace/data/imports/:batchId/errors` | Người upload theo scope | FR-ING-02 | Target | Row errors và file lỗi tải xuống |
| `/workspace/data/reconciliation` | Operations Manager, Data Admin, Auditor read-only | FR-ING-06, FR-VAL-05..06 | Target | Excel–web/OCR status, businessDataCode, score/confidence, resolution |
| `/workspace/data/reconciliation/:runId/items/:itemId` | Người có quyền review | FR-VAL-04..06 | Target | Field diff, candidate explanation, OCR secondary evidence, audit reason |
| `/workspace/production/staging` | Operations Staff/Managers/Auditor read-only | FR-VAL-01..06 | Target | Findings, source, current/proposed, filters |
| `/workspace/production/conflicts/:id` | Người có quyền review | FR-VAL-03..06 | Target | Field-level comparison, resolution, audit reason |
| `/workspace/production/closures` | Operations/Executive Manager | FR-CLO-01..02, FR-NOTI-02 | Target | Coverage, unresolved reconciliation, warning override, partial status |
| `/workspace/production/closures/:id` | Theo scope | FR-CLO-01..04 | Target | Closure items, official version, audit và recalc |
| `/workspace/production/reopen-requests` | Requester/Executive Manager | FR-CLO-03..04 | Target | Request, decision, correction workspace, reclose |
| `/workspace/dashboard/kpis` | Theo data scope | FR-DASH-01..05 | Target | KPI snapshots, comparison, coverage, freshness |
| `/workspace/dashboard/analytics` | Theo data scope | FR-DASH-08..09 | Target | Chart catalog, shared filters/status, capability-aware extension charts |
| `/workspace/dashboard/drill-down/:snapshotId` | Theo data scope | FR-DASH-04 | Target | Contributor → official → source/audit summary |
| `/workspace/notifications` | Theo data scope | FR-NOTI-02, FR-NOTI-03A | Target — Open `TBD-17` | Chỉ là route đề xuất nếu chọn in-app; closure/import screen vẫn phải hiện kết quả bắt buộc |
| `/workspace/audit` | Auditor, System Admin theo quyền | FR-AUD-01..03 | Target | Append-only log, filters, export guardrail |

## 5. Màn hình ngoài phạm vi lõi hoặc capability chưa bật

Các page key sau tồn tại trong navigation hoặc component hiện tại nhưng không thuộc MVP PRD:

- `attendance`, `leave`, `overtime`, payroll và employee KPI; lịch/phân công vận hành cơ bản vẫn thuộc Quản lý nhân sự.
- `finance`; warehouse/inventory chỉ hiển thị khi capability Quản lý kho được bật.
- Maintenance dashboard/request/schedule/work order/history chỉ hiển thị khi capability Bảo trì & lập lịch được bật.
- AI chatbot tự do.

Chúng được đánh dấu `Out of MVP`. Không thêm chúng vào Must traceability và không ưu tiên trước các gap
closure, correction, KPI snapshot hoặc audit.

## 6. State contract theo nhóm màn hình

### 6.1. Import

- `Received`, `Queued/Processing`, `Partially Succeeded`, `Succeeded`, `Failed`, `Cancelled`.
- Progress không được suy ra từ số staging nếu worker chưa hoàn tất.
- Summary gồm total/succeeded/failed/conflict.
- Row error có sheet/row/field/code/message/value an toàn.
- Upload lại cho biết reused/merged/conflict; không gọi tất cả là “thành công”.

### 6.2. Staging và conflict

- Badge `Draft`, `Valid`, `Needs Review`, `Rejected`, `Ready to Close`.
- Finding phân `Error`, `Warning`, `Information`.
- Warning override hiển thị actor, reason và time.
- Conflict view so sánh source, người nhập, thời gian, current value và proposed value.
- Trước close không xuất hiện trên official dashboard.

### 6.3. Closure/correction

- Coverage card: expected, valid, no-production confirmed, missing, error, warning.
- CTA close disable khi còn Error hoặc thiếu bắt buộc.
- Partial phải liệt kê child scope chưa đóng.
- Reopen đang xử lý không thay official version hiện hành.
- Reclose thành công hiển thị version mới và trạng thái `Adjusted`.

### 6.3A. Historical/similarity/reconciliation

- Badge thời gian: `Current`, `Historical Backfill`, `Late Arrival`, `Correction`.
- Status: `Matched`, `Value Mismatch`, `Missing in Excel`, `Missing on Web`, `OCR Mismatch`, `OCR Low Confidence`, `Resolved`.
- Near-duplicate warning hiển thị score từ 90% đến dưới 100%, field contribution và policy/model version; không có auto-merge.
- OCR luôn mang nhãn `Nguồn thứ yếu`, confidence từng field và evidence có quyền.
- Item bất thường chưa resolve hiển thị lý do chặn chốt.

### 6.4. Dashboard/drill-down

- Header gồm period, scope, completeness, coverage và last calculated.
- KPI card có missing/freshness state.
- Comparison chỉ bật khi hai kỳ đủ điều kiện.
- Breadcrumb: factory → line → work group → machine → shift/day → record → source.
- Audit link chỉ hiện khi có FR-AUD-03 permission.
- Analytics có tối thiểu tám view cốt lõi; mọi chart dùng cùng filter/scope/completeness/freshness.
- `Empty`, `Partial`, `Stale`, `Updating`, `Unavailable capability` không render thành series toàn số 0.
- Mỗi chart có drill-down hoặc bảng/text fallback; không có ranking cá nhân.

## 7. Quyền sản phẩm đích

| Role PRD | Màn hình chính |
|---|---|
| Executive Manager | Dashboard công ty/nhà máy, closure, reopen approval, investigation |
| Operations Manager | Staging/reconciliation review, conflict/warning, closure theo scope, dashboard |
| Operations Staff | Nhập liệu, staging của scope, reopen request |
| Data Admin | Company config, master data, mapping, validation/KPI config |
| System Admin | Account, role, permission, data scope, security policy |
| Auditor | Dashboard/audit/traceability read-only theo scope |

Ca trưởng/Tổ trưởng không nằm trong bảng role: đây là position của Operations Staff trong Quản lý nhân sự.

Backend luôn là nơi quyết định cuối cùng. Ẩn menu hoặc redirect ở frontend không phải security control.

## 8. Mobile

Mỗi target screen cần định nghĩa trước khi code:

- Navigation thay thế sidebar đang bị ẩn dưới 650 px.
- Thứ tự card và filter quan trọng.
- Table chuyển card hoặc xác định cột cố định.
- Modal conflict/closure có layout một cột và action không bị che.
- Chart có text/table fallback.

## 9. E2E mapping tối thiểu

| Journey | Screen sequence |
|---|---|
| Nhập tay và close | Entry → Staging → Closures → Dashboard → Drill-down |
| Import 95/5 | Imports → Batch errors → Staging → Upload lại → Dedup summary |
| Dữ liệu lịch sử gần trùng | Imports → Time class → 96% warning → Candidate review → Resolution/Audit |
| Đối soát nguồn | Excel import + Web records → Reconciliation → Mismatch/Missing → Resolution → Staging/Closure |
| OCR thứ yếu | Paper upload → OCR confidence → Reconciliation evidence → Human confirmation; không tạo official trực tiếp |
| Warning override | Staging → Finding detail → Override reason → Closure → Audit |
| Conflict | Imports/Entry → Conflict review → Resolution → Staging → Audit |
| Sửa sau chốt | Dashboard → Reopen request → Approval → Correction → Reclose → Adjusted dashboard |
| Scope security | Đăng nhập role giới hạn → thử route/API ngoài scope → Forbidden không lộ dữ liệu |
| Dashboard nhiều biểu đồ | Shared filters → 8 core charts → Partial/Stale/Empty states → Drill-down/table fallback |
