# API Contracts

> Tài liệu phân biệt rõ API đang được controller hiện thực hóa với contract mục
> tiêu trong TDD. Một route có trong bảng Target nhưng mang trạng thái `Gap`
> không phải là endpoint có thể gọi ở phiên bản hiện tại.

## 1. Nguồn và ký hiệu

- **Current**: có controller public trong source code hiện tại.
- **Partial**: có API gần tương đương nhưng khác path, payload, state hoặc quy
  tắc nghiệp vụ.
- **Dormant**: có entity/service nền nhưng không có controller public.
- **Gap**: chưa tìm thấy endpoint tương đương trong source code.
- **Target**: contract mong muốn từ
  [PRD v2.5](PRD_He_thong_hieu_suat_nha_may.md) và
  [TDD v1.5](TDD_He_thong_hieu_suat_nha_may.md).

Source code là nguồn sự thật cho Current. TDD là thiết kế ban đầu và tự yêu cầu
một OpenAPI document riêng trước khi xem API target là contract hoàn chỉnh.

## 2. Contract Current

### 2.1. Base URL và transport

```text
http://localhost:8080/factory-management/api/v1
```

- API prefix: `/api/v1`.
- Context path mặc định: `/factory-management`.
- JSON dùng `camelCase`.
- Upload Excel dùng `multipart/form-data`.
- CORS hiện chỉ allow `http://localhost:5173` và
  `http://127.0.0.1:5173`.
- Method được frontend client hỗ trợ hiện là `GET`, `POST`, `PUT`, `DELETE`;
  chưa có helper `PATCH`.

### 2.2. Authentication và request identity

Các route sau hiện public:

```text
POST /auth/login
POST /auth/introspect
POST /auth/refresh
POST /auth/logout
```

`POST /auth/register` yêu cầu `ADMIN`; các route còn lại mặc định yêu cầu:

```http
Authorization: Bearer <JWT>
```

JWT hiện dùng HS512 và chứa `sub`, `jti`, `scope`, `userId`, `employeeId` và
`tokenVersion`. Logout/refresh ghi `jti` vào `invalid_token`; đổi password,
role hoặc trạng thái tài khoản tăng `tokenVersion`.

Client có thể gửi:

```http
X-Request-Id: <tối đa 80 ký tự>
```

Nếu thiếu hoặc không hợp lệ, server sinh UUID. Response luôn trả lại
`X-Request-Id`; error envelope có thể chứa `requestId`.

### 2.3. Response envelope hiện tại

Success:

```json
{
  "code": 1000,
  "message": "Thông báo tùy chọn",
  "result": {}
}
```

Error:

```json
{
  "code": 1001,
  "message": "Mô tả lỗi",
  "timestamp": "2026-07-30T10:15:30",
  "path": "/factory-management/api/v1/...",
  "requestId": "b4c20c80-...",
  "fieldErrors": {
    "fieldName": "Mô tả lỗi trường"
  }
}
```

Frontend hiện coi response thành công khi HTTP thành công và `code == 1000`.
`ApiResponse<T>` không có field `success` hoặc `data`.

Các mapping lỗi hiện thấy:

- authentication failure: `401`;
- authorization failure: `403`;
- validation/application error: theo `ErrorCode`;
- unique/optimistic locking conflict: `409`;
- lỗi không phân loại: error code chung, không lộ stack trace trong body.

Không phải mọi current list endpoint đều có pagination. Một số route dùng
`PageResponse`; nhiều route `/all` trả toàn bộ `List`.

### 2.4. Route families hiện có

| Nhóm | Base path Current | Ghi chú |
|---|---|---|
| Auth | `/auth` | register, login, introspect, refresh, logout, me, change-password |
| User/RBAC | `/users`, `/users/{userId}/data-scopes` | role, enabled/locked, reset password và explicit scope |
| Audit | `/audit-events` | GET có filter/pagination cho ADMIN; chưa có export và path khác target |
| Organization | `/factories`, `/departments`, `/production-lines`, `/teams`, `/employees` | CRUD hiện dùng `/all`, `PUT`, soft delete qua `DELETE` |
| Production master | `/shifts`, `/machine-types`, `/machines`, `/downtime-reasons`, `/quality-error-types`, `/products`, `/materials` | Chưa có effective-dated contract thống nhất |
| Planning | `/production-plans`, `/production-orders` | CRUD và action approve/close/status |
| Production staging | `/production-report-staging` | `DRAFT → SUBMITTED`; từ `SUBMITTED` rẽ sang `CHANGE_REQUESTED → DRAFT` hoặc `APPROVED → LOCKED` |
| Staging details | `/machine-downtime-staging`, `/quality-report-staging`, `/material-issue-staging`, `/employee-actual-staging` | Gắn với production report staging cha |
| Excel staging import | `/staging-report-excel` | template, preview, import đồng bộ |
| Official production | `/production-reports` | Chỉ đọc; official được tạo khi approve staging; không có `/search/my-scope` |
| Dashboard | `/production-reports/dashboard`, `/production-reports/dashboard/my-scope`, `/executive-dashboard` | Tổng hợp trực tiếp; chưa phải KPI snapshot API |
| Optional extension | `/ai-chat` | Tool-based, read-only, Ollama local |
| Ngoài lõi PRD | `/hr`, `/employee-portal`, `/financial-*`, `/warehouses`, `/inventory`, `/maintenance` | Current extensions, không phải target core contract |

Danh sách controller mới là bằng chứng endpoint public. Các class
`DailyCloseBatch`, `ReportImportBatch`, `GeneratedReportFile` và service liên
quan không tạo ra contract public khi chưa có controller.

Known consumer mismatch: `ReportsPage` gọi
`GET /production-reports/search/my-scope`, nhưng `ProductionReportController`
chỉ có `GET /production-reports/search` cho `ADMIN`, `DIRECTOR`, `FINANCE`.
Scoped endpoint hiện có là `/production-reports/dashboard/my-scope`, không phải
report-list search. Vì vậy route của frontend không được xem là Current.

## 3. Contract Target

### 3.1. Quy ước chung

- Logical base path: `/api/v1`. Context path của deployment không phải một phần
  resource contract.
- Resource dùng danh từ số nhiều và path `kebab-case`.
- JSON field và query parameter dùng `camelCase`.
- Mọi nghiệp vụ cần authentication.
- Server resolve `companyId` từ authenticated/deployment context. Nếu payload
  tạm nhận `companyId`, server phải xác minh nó khớp context; không dùng nó như
  bằng chứng tenant hoặc quyền.
- Server resolve role/action/data scope và áp dụng cho cả read, write, export,
  dashboard, audit và AI/tool.
- Ba operational role target là `EXECUTIVE_MANAGER`, `OPERATIONS_MANAGER`,
  `OPERATIONS_STAFF`; employee position như Ca trưởng/Tổ trưởng và navigation label không phải bằng chứng quyền.
- List lớn phải có pagination/filter/order contract rõ trong OpenAPI.
- Write có thể được giao lại bởi client/worker phải có idempotency mechanism.
  Header, request field và persistence model cụ thể còn phải chốt; không tự
  mặc định chỉ dựa vào file hash.
- Request thay đổi state phải kiểm tra lại state/version trong transaction.
  Cơ chế `If-Match`, version field hay idempotency header cần được chốt bằng
  API/ADR, chưa phải Current.

### 3.2. Envelope target và điểm chưa tương thích

TDD mô tả target success:

```json
{
  "success": true,
  "data": {},
  "message": "Thông báo tùy chọn"
}
```

Target error:

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

Contract này **chưa được Current triển khai**. Current dùng
`{code, message, result, timestamp, path, requestId, fieldErrors}`. Trước khi
thay envelope phải:

1. đóng quyết định tương thích trong [DECISIONS.md](DECISIONS.md);
2. cập nhật frontend client và mọi consumer trong cùng một migration plan;
3. định nghĩa error codes, pagination và request correlation trong OpenAPI;
4. có contract test cho cả success/error.

Không nên duy trì vô thời hạn hai envelope trên cùng API version.

### 3.3. Canonical target payloads

#### Tạo staging record

```http
POST /api/v1/staging-records
Content-Type: application/json
Authorization: Bearer <token>
```

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
  "sourceType": "web_manual"
}
```

Target phải tạo provenance cho manual input, normalize canonical values, chạy
validation/dedup/conflict và chỉ tạo official data qua closure. Logical
identity cuối cùng vẫn phụ thuộc PRD TBD-07.

Response target trả `businessDataCode` và `dataTimeClass` do server derive. Client không được gửi
`dataTimeClass`, similarity score, role hoặc company scope rồi được server tin trực tiếp.

Current gần nhất là `POST /production-report-staging`, nhưng payload dùng
numeric IDs cho factory/department/line/team/leader/machine, không có
`companyId`, `productId`, `recordType`, `sourceType` hoặc generic `values`.
Current endpoint tạo `DRAFT` theo current workflow; nó không đồng nghĩa với
canonical endpoint target.

#### Chốt phạm vi

```http
POST /api/v1/closures
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "productionDate": "2026-07-10",
  "scopeType": "production_line",
  "scopeId": "line_a",
  "mode": "complete",
  "warningOverrideReasons": []
}
```

Điều kiện target:

- scope phải nằm trong quyền;
- coverage và danh sách dữ liệu bắt buộc phụ thuộc TBD-04;
- Error chặn chốt;
- Warning chỉ được override bởi người có quyền và có lý do;
- closure/official version/audit/outbox cùng transaction;
- concurrent close/reopen/reclose cùng cây scope phải conflict an toàn.

Giá trị `mode` cho partial/exception và cấu trúc warning reasons chưa được TDD
định nghĩa đủ để code; phải hoàn thiện trong OpenAPI sau khi domain blocker
được quyết định.

#### Dashboard KPI

```http
GET /api/v1/dashboard/kpis
    ?scopeType=factory
    &scopeId=factory_1
    &periodType=month
    &periodStart=2026-06-01
    &periodEnd=2026-06-30
    &shiftIds=shift_day
    &productIds=product_x
Authorization: Bearer <token>
```

Mỗi result target phải mang đủ metadata để người dùng không nhầm dữ liệu:

- KPI definition/version;
- scope và filter đã áp dụng;
- official source version/watermark;
- `complete`, `partial`, `exception`, `stale` hoặc `updating`;
- coverage và missing scopes khi không complete;
- `lastCalculatedAt`/`lastUpdated` và data freshness;
- contributor/drill-down reference có kiểm tra quyền.

Nếu filter vượt khả năng query string, TDD cho phép thiết kế
`POST /dashboard/kpi-queries`; route này chỉ trở thành contract sau khi có
OpenAPI và implementation.

#### Source batch/import

Target resource lifecycle:

```text
POST /source-batches
GET  /source-batches/{id}
GET  /source-batches/{id}/errors
```

Contract upload cụ thể — multipart trực tiếp, pre-signed object upload hay API
source metadata — vẫn phải được chốt. Response nhận file phải thể hiện
“received/queued”, không được nói import hoàn tất nếu worker chưa xử lý.

Mỗi batch target phải truy được ít nhất:

- source type và original file metadata/key;
- adapter/template version;
- state và row counts;
- idempotency/dedup metadata;
- row/field errors;
- uploader, timestamps và request ID;
- retry/failure status không lộ nội bộ nhạy cảm.

#### Reconciliation và ứng viên gần trùng

```text
POST /reconciliation-runs
GET  /reconciliation-runs/{id}
GET  /reconciliation-runs/{id}/items
GET  /source-records/{id}/match-candidates
POST /reconciliation-items/{id}/resolve
```

`POST /reconciliation-runs` nhận `excelSourceBatchId`, scope/kỳ, tùy chọn
`includeOcrEvidence` và `idempotencyKey`. Item trả tối thiểu:

- `businessDataCode`, `dataTimeClass` và các source record IDs;
- giá trị canonical theo nguồn `web_manual`, `manager_excel`, `paper_ocr`;
- `matched`, `value_mismatch`, `missing_in_excel`, `missing_on_web`,
  `ocr_mismatch`, `ocr_low_confidence` hoặc `resolved`;
- similarity score, feature explanation, policy/model/template version khi có;
- resolution, actor, reason, time và authorized audit reference.

`paper_ocr` luôn là secondary evidence trong baseline. Score từ `0.90` đến dưới
`1.00` chỉ tạo warning. Cùng identity khác giá trị luôn conflict; API không có
action “auto merge by score”. Retry resolve cùng idempotency key không tạo quyết định thứ hai.

#### Dashboard chart catalog

```http
GET /api/v1/dashboard/charts?chartCodes=production-trend,plan-vs-actual,machine-state-mix,downtime-pareto,defect-pareto,shift-heatmap,data-coverage,reconciliation-status&scopeType=production_line&scopeId=line_a&periodStart=2026-07-01&periodEnd=2026-07-31
```

Mỗi chart trả `chartCode`, `status`, `unit`, `series`, `completeness`, `freshness`,
`lastCalculatedAt` và `drillDown`. `status` gồm `ready`, `empty`, `partial`,
`stale`, `updating`, `forbidden`, `unavailable_capability`; không trả series zero giả cho các trạng thái này.

#### Navigation/capability presentation

Target có thể trả module key/label/order được phép qua resource session/me được chốt trong OpenAPI.
Đây chỉ là presentation contract: sửa response hoặc gọi thẳng URL không được vượt authorization
server-side. Module key ổn định theo PRD 4.4; label không dùng làm identifier.

### 3.4. Mapping Target → Current

| Target TDD | Current gần nhất | Trạng thái | Khoảng cách chính |
|---|---|---|---|
| `GET /company/settings` | Không có | Gap | Chưa có Company/company context/settings |
| `PATCH /company/settings` | Không có | Gap | Current client/server chưa hỗ trợ PATCH |
| `GET /factories` | `GET /factories/all` | Partial | Path/list/pagination và company scope khác |
| `POST /machines` | `POST /machines` | Partial | Current không effective-dated/company-scoped |
| `PATCH /machines/{id}` | `PUT /machines/{id}` | Partial | Method và version/effective-date semantics khác |
| `POST /machines/{id}/deactivate` | `DELETE /machines/{id}` | Partial | Current soft-delete bằng DELETE; target action path khác |
| `POST /master-data/imports` | Không có generic route | Gap | Excel hiện chỉ import production staging |
| `POST /staging-records` | `POST /production-report-staging` và detail staging routes | Partial | Domain-specific schema, không canonical/source-aware |
| `GET /staging-records` | `/production-report-staging/*` | Partial | State/filter/pagination khác; chưa có unified finding/conflict |
| `POST /source-batches` | `POST /staging-report-excel/preview` và `/import` | Partial | Current synchronous và atomic khi validate; không persistent source batch/outbox hoặc partial-row success |
| `GET /source-batches/{id}` | Không có controller | Gap | Có một số batch entity dormant nhưng không public API |
| `GET /source-batches/{id}/errors` | Preview trả lỗi ngay trong response | Partial | Không có stable batch/error resource để polling/download |
| `GET /source-records/{id}/match-candidates` | Không có | Gap | Chưa có score/explanation/policy version hoặc human review |
| `POST /conflict-groups/{id}/resolve` | Không có | Gap | Current thường reject unique conflict; không field comparison |
| `POST /reconciliation-runs` và item routes | Không có | Gap | Excel Current không đối soát stateful với web/OCR |
| `POST /closures` | Không có controller | Gap | Approve từng staging không phải close date/scope |
| `GET /closures` | Không có controller | Gap | `DailyCloseBatch` entity dormant không tạo public API |
| `POST /closures/{id}/reopen-requests` | Không có | Gap | Chưa có correction workflow |
| `POST /reopen-requests/{id}/approve` | Không có | Gap | PRD đã chốt một bước duyệt bởi Quản lý điều hành, code chưa có |
| `POST /closures/{id}/reclose` | Không có | Gap | Chưa có official version/supersede/recalculation |
| `GET /dashboard/kpis` | `/production-reports/dashboard`, `/dashboard/my-scope`, `/executive-dashboard` | Partial | Current aggregate live, hardcoded KPI, thiếu completeness/version/freshness |
| `GET /dashboard/charts` | Một số field/chart trong overview/executive dashboard | Partial | Không có catalog tám view, uniform status, capability gating hoặc drill-down contract |
| `GET /dashboard/trends` | `GET /executive-dashboard` có trường trends | Partial | Route/schema/scope và KPI snapshot semantics khác |
| `GET /dashboard/rankings` | Không có ranking máy/tổ chuẩn PRD | Gap | Không được dùng personal KPI/ranking làm thay thế |
| `GET /dashboard/downtime-reasons` | CRUD `/downtime-reasons`; detail trong reports | Gap | CRUD taxonomy không phải aggregate analysis |
| `GET /dashboard/drill-down` | `/production-reports/{id}/details` | Partial | Chưa đi contributor → official version → source → audit summary |
| `GET /audit-logs` | `GET /audit-events` | Partial | Path/schema/action coverage/authorization target chưa đồng nhất |
| Export audit log (path chốt trong OpenAPI) | Không có | Gap | FR-AUD-03 yêu cầu export được kiểm quyền và chính hành động export phải được audit |

### 3.5. Current routes bổ sung không có trong bảng TDD API ban đầu

Các route này có thật và cần được bảo toàn hoặc version/migrate có chủ đích:

```text
/auth/*
/users/*
/users/{userId}/data-scopes/*
/departments/*
/production-lines/*
/teams/*
/shifts/*
/products/*
/production-plans/*
/production-orders/*
/machine-downtime-staging/*
/quality-report-staging/*
/material-issue-staging/*
/employee-actual-staging/*
/ai-chat/messages
```

TDD không liệt kê một route không có nghĩa route Current tự động bị xóa. Mọi
thay đổi breaking phải có consumer inventory, migration path và ExecPlan.

## 4. State contract: Current và Target không đồng nhất

| Aggregate | Current | Target |
|---|---|---|
| Production staging | Flow public dùng `DRAFT`, `SUBMITTED`, `CHANGE_REQUESTED`, `APPROVED`, `LOCKED`; enum còn có `FILE_GENERATED`, `IMPORT_FAILED`, `IMPORTED` cho code nền | `draft`, `valid`, `needs_review`, `rejected`, `ready_to_close`, `closed`, `superseded` |
| Import | Preview/import đồng bộ; dormant batch có `UPLOADED`, `VALIDATED`, `VALIDATION_FAILED`, `IMPORTING`, `IMPORTED`, `FAILED` | `received`, `queued`, `processing`, `partially_succeeded`, `succeeded`, `failed`, `cancelled` |
| Closure | Không có public aggregate; dormant batch có `CLOSED`, `FILE_GENERATED`, `IMPORT_FAILED`, `IMPORTED`, `LOCKED` | `open`, `pending_review`, `partially_closed`, `closed_complete`, `closed_with_exceptions`, `reopened`, `adjusted` |
| KPI snapshot | Không có | `pending`, `calculating`, `current_complete`, `current_partial`, `stale`, `failed` |

Nhánh transition Current chính xác là:

```text
DRAFT → SUBMITTED
SUBMITTED → CHANGE_REQUESTED → DRAFT
SUBMITTED → APPROVED → LOCKED
```

`APPROVED` tạo official report ngay; `CHANGE_REQUESTED` không thể chuyển thẳng
sang `LOCKED`.

Không map enum bằng tên gần giống. Cần state-transition design và migration
explicit; đặc biệt `APPROVED` Current đang tạo official ngay, trong khi Target
chỉ tạo official qua closure.

## 5. Authorization contract và gap cần ưu tiên

Target bắt buộc kiểm tra server-side theo role, action, company và data scope
cho mọi read/write/export.

Current đã có JWT, `SecurityFilterChain`, `@PreAuthorize` và
`AuthorizationScope`, nhưng cần lưu ý:

- nhiều master-data GET chỉ yêu cầu authenticated và trả `/all`;
- company boundary chưa tồn tại;
- update staging kiểm tra quyền trên bản ghi hiện tại trước khi service cho
  phép đổi hierarchy/team mới; service phải kiểm tra cả target scope để tránh
  chuyển record sang phạm vi không được cấp;
- audit hiện chỉ có read API cho ADMIN; chưa có export được kiểm quyền và audit
  chính thao tác export theo FR-AUD-03;
- các role Current chưa ánh xạ chính thức một-một với actor/RACI của PRD;
- endpoint trả `404` hay `403` khi resource ngoài scope cần được chuẩn hóa để
  tránh lộ tồn tại của dữ liệu.

## 6. Contract acceptance gate

Một target endpoint chỉ được đánh dấu Current khi có đủ:

1. controller/route và DTO thực tế;
2. server-side company/role/scope authorization;
3. validation và state-transition guard;
4. transaction, idempotency/concurrency rule nếu có write;
5. success/error/status/pagination schema trong OpenAPI;
6. audit/provenance theo requirement;
7. integration/contract test cho happy path, invalid input, 401, 403, 404/409
   và retry/idempotency nếu áp dụng;
8. frontend hoặc consumer được cập nhật;
9. không còn dựa vào domain blocker chưa có quyết định ghi trong DECISIONS/PRD.

Kế hoạch thay đổi contract phải theo [PLANS.md](PLANS.md); quyết định breaking
change và các blocker phải được ghi trong [DECISIONS.md](DECISIONS.md).
