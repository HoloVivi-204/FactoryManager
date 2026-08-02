# ExecPlan Standard và Template

> File này là chuẩn để viết một ExecPlan tự chứa cho một thay đổi cụ thể. Nó
> không phải backlog, sprint board, changelog hay bảng theo dõi trạng thái.
> Tiến độ hằng ngày thuộc issue/PR; không thêm danh sách “đang làm/đã xong” vào
> `docs/exec-plans.md`.

## 1. Khi nào cần ExecPlan

Chỉ tạo ExecPlan khi thay đổi đồng thời:

1. kéo dài qua nhiều milestone có thể kiểm chứng; và
2. có migration/backfill, sequencing triển khai, benchmark theo profile tải
   hoặc rollback phức tạp.

Các đặc điểm sau chỉ là **tín hiệu để đánh giá độ phức tạp**, không tự động bắt
buộc một ExecPlan:

- đi qua nhiều module frontend/backend/database;
- thêm hoặc đổi public API/response envelope;
- đổi state machine, business key, unique constraint hoặc official data;
- đụng authentication, authorization, company context hoặc data scope;
- thêm queue, cache, object storage, outbox, worker hoặc external adapter;
- thay KPI formula, aggregation, historical recalculation hoặc dashboard
  freshness;
- phụ thuộc một PRD TBD/TDD ADR.

Một thay đổi chỉ có một milestone, kể cả khi đụng API hoặc authorization, không
cần ExecPlan nếu không có migration/sequencing/benchmark/rollback phức tạp.
Requirement, test và tài liệu contract liên quan vẫn phải được cập nhật.

## 2. Nguyên tắc của một ExecPlan

### 2.1. Tự chứa

Người mới vào repository phải có thể thực hiện plan mà không dựa vào chat riêng.
Plan phải dẫn:

- requirement IDs từ
  [PRD v2.5](prd.md);
- target design từ
  [TDD v1.5](technical-design.md);
- Current/Target trong [architecture.md](architecture.md);
- public contract trong [api-contracts.md](api-contracts.md);
- ADR/TBD trong [decisions.md](decisions.md);
- file/class/test Current làm bằng chứng.

Không viết “sửa service tương ứng” hoặc “thêm test cần thiết”. Phải chỉ rõ
service nào, hành vi nào, test quan sát điều gì.

### 2.2. Outcome trước implementation

Plan bắt đầu bằng hành vi người dùng/hệ thống có thể quan sát, sau đó mới mô tả
code. Ví dụ:

```text
Khi hai request cùng close một scope, chỉ một request thành công; request còn
lại nhận conflict có requestId và không tạo official version/outbox trùng.
```

Không dùng “tạo 5 class, 3 table” làm mục tiêu chính.

### 2.3. Không tự quyết blocker

Nếu thay đổi phụ thuộc một mục `Open — Blocker` trong
[decisions.md](decisions.md), ExecPlan phải dừng tại decision gate. Có ba kết
quả hợp lệ:

1. quyết định được ghi đầy đủ trong DECISIONS/PRD/TDD;
2. một giả định tạm có phạm vi và ngày hết hạn được ghi như quyết định;
3. đổi phạm vi plan để không phụ thuộc blocker.

Developer không được điền công thức KPI, production-day rule, identity,
coverage hoặc source precedence bằng “best practice” kỹ thuật.

### 2.4. Lát cắt dọc và TDD

Milestone phải tạo một lát cắt chạy được từ boundary đến persistence/read model
và có test. Với feature/fix:

1. viết test thể hiện hành vi thất bại;
2. chạy và lưu failure signal mong đợi;
3. implement thay đổi nhỏ nhất;
4. chạy test thành công;
5. refactor, rồi chạy lại unit/integration/contract tests.

Không xây toàn bộ schema rồi toàn bộ service rồi mới có test ở cuối.

### 2.5. Milestone không phải state tracker

ExecPlan được sắp theo milestone vì dependency và khả năng nghiệm thu. Không
thêm cột trạng thái, phần trăm hoàn thành hoặc nhật ký làm việc. Issue/PR quản
lý tiến độ; ExecPlan quản lý ý định, thứ tự, bằng chứng và cách nghiệm thu.

## 3. Template ExecPlan

Sao chép phần dưới vào một file plan dành riêng cho thay đổi. Không điền trực
tiếp tiến độ dự án vào `docs/exec-plans.md`.

````markdown
# ExecPlan: <Kết quả có thể quan sát>

## Metadata

- Requirement IDs:
- ADR/TBD liên quan:
- Public API/data contract liên quan:
- Phạm vi release:
- Consumer bị ảnh hưởng:

## 1. Mục tiêu và giá trị người dùng

Mô tả ai gặp vấn đề gì, hành vi mới là gì và vì sao thay đổi này cần thiết.
Nêu một ví dụ đầu-cuối bằng dữ liệu cụ thể.

## 2. Tiêu chí thành công quan sát được

Viết theo Given/When/Then hoặc input → output. Bao gồm happy path, quyền,
invalid state, concurrency/idempotency và freshness nếu liên quan.

Ví dụ định dạng:

- Given <trạng thái cụ thể>, when <actor/action>, then <kết quả và side effects>.
- Với cùng idempotency key được gửi hai lần, <số record/event mong đợi>.
- User ngoài scope gọi cùng resource nhận <contract mong đợi> và không lộ dữ liệu.

Không dùng “code chạy đúng” hoặc “test pass” làm tiêu chí duy nhất.

## 3. Current evidence

Liệt kê file, class, method, route, table/index và test đang quyết định hành vi
hiện tại. Giải thích data flow hiện tại và failure có thể tái hiện.

Ví dụ:

- `apps/backend/src/main/.../SomeController.java`: route và authorization.
- `apps/backend/src/main/.../SomeService.java`: transaction/state transition.
- `apps/frontend/src/features/*/api/...` ho?c `apps/frontend/src/shared/api/...`: consumer contract.
- Lệnh tái hiện và output/error quan trọng.

## 4. Target behavior và traceability

Map từng requirement sang thay đổi và bằng chứng nghiệm thu:

| Requirement | Target behavior | API/data/module | Test/evidence |
|---|---|---|---|
| FR-... | ... | ... | ... |

Nêu rõ điểm nào là Target chưa tồn tại. Không mô tả target route/table như
Current.

## 5. Decision dependencies

| Decision/TBD | Trạng thái được xác minh | Bản ghi trong DECISIONS/PRD | Tác động nếu chưa chốt |
|---|---|---|---|
| TBD-/ADR-... | ... | ... | Plan dừng hoặc thu hẹp ở đâu |

Nếu không có blocker, ghi lý do và dẫn docs/decisions.md. Không tạo quyết định mới
trong phần implementation.

## 6. Scope và non-goals

### Trong phạm vi

Nêu các user flow, module, contract và migration thực sự thay đổi.

### Ngoài phạm vi

Nêu rõ capability dễ bị hiểu nhầm là đi kèm nhưng không thuộc plan. Giải thích
vì sao loại ra và dependency tương lai.

## 7. Kiến trúc và dependency impact

Mô tả Current flow → Target flow. Nếu có ít nhất ba component, dùng sơ đồ nhỏ.

Nêu:

- module sở hữu business rule;
- dependency direction;
- transaction boundary;
- trust boundary;
- synchronous/asynchronous boundary;
- consistency model và source of truth;
- consumer/upstream/downstream bị ảnh hưởng.

## 8. API contract

Cho từng route đổi/thêm:

- method/path;
- auth/role/action/scope/company resolution;
- request/query/header schema;
- success/error/status/pagination;
- idempotency và optimistic/concurrency contract;
- backward compatibility/versioning;
- frontend hoặc external consumer.

Nếu contract chưa đủ để viết OpenAPI, plan chưa sẵn sàng implement.

## 9. Data model và migration

Nêu:

- table/column/type/nullability/default;
- PK/FK/unique/check/index;
- logical identity và dedup rule;
- effective dates/version fields;
- backfill và cách xử lý dữ liệu không hợp lệ;
- deployment order tương thích app cũ/app mới;
- forward fix và rollback;
- volume/time/lock estimate;
- retention/provenance impact.

Không dùng `ddl-auto=update` làm migration production.

## 10. Security và privacy

Phân tích:

- dữ liệu/ID nào đến từ untrusted client;
- server resolve company/scope như thế nào;
- target-scope check khi resource đổi hierarchy;
- secret/PII/audit redaction;
- file/external service boundary;
- abuse/rate-limit/replay/idempotency;
- tests 401/403 và cross-scope.

## 11. Milestones và implementation narrative

Mô tả thứ tự thay đổi theo nguyên nhân → kết quả. Nêu đường dẫn file dự kiến,
không chỉ tên lớp chung.

### Milestone 1 — <Lát cắt có thể chạy/kiểm chứng>

- Outcome:
- Test viết trước:
- Files/modules dự kiến:
- Thay đổi contract/data:
- Cách thực hiện:
- Lệnh verification:
- Failure signal mong đợi trước fix:
- Evidence nghiệm thu sau fix:

### Milestone 2 — <Lát cắt kế tiếp>

Lặp lại cấu trúc trên. Milestone là thứ tự thực hiện, không mang trạng thái.

## 12. Risks

| Rủi ro | Nguyên nhân | Tác động quan sát được | Giảm thiểu | Tín hiệu rollback |
|---|---|---|---|---|
| ... | ... | ... | ... | ... |

Nêu riêng rủi ro dữ liệu, compatibility, security, vận hành và khả năng không
đạt benchmark. Không dùng danh sách rủi ro thay cho test hoặc rollback cụ thể.

## 13. Test strategy

| Lớp test | Hành vi cần chứng minh | Fixture/dependency | Lệnh chạy |
|---|---|---|---|
| Unit | Domain rules và edge cases | Clock/ID/dependency được kiểm soát | ... |
| Repository/migration | Constraint, query, effective date | PostgreSQL test instance | ... |
| Integration | HTTP + security + transaction + outbox | App + DB/queue/storage cần thiết | ... |
| Contract | Request/response/error compatibility | OpenAPI/consumer fixture | ... |
| E2E | User flow đầu-cuối | Backend + frontend | ... |
| Performance | p95/throughput theo profile đã duyệt | Dataset/traffic model | ... |

Bao gồm test:

- happy path;
- validation boundary;
- state transition sai;
- duplicate/retry;
- concurrent requests;
- role và scope ngoài quyền;
- rollback khi DB/queue/storage lỗi;
- historical/effective-date behavior;
- stale/partial/freshness nếu có aggregation.

## 14. Observability và vận hành

Nêu log fields, metrics, traces, dashboard và alert chứng minh feature đang
hoạt động. Với worker, phải có queue depth, duration, retries, dead-letter và
idempotency conflict. Với API, có requestId, latency và error code.

Nêu runbook ngắn:

- cách phát hiện lỗi;
- cách xác định record/event bị ảnh hưởng;
- retry/reconcile an toàn;
- khi nào cần operator can thiệp.

## 15. Rollout, compatibility và rollback

Mô tả thứ tự:

1. migration/config/dependency;
2. backend/worker;
3. frontend/consumer;
4. feature flag hoặc activation;
5. quan sát sau deploy.

Rollback phải nói rõ dữ liệu đã ghi bởi phiên bản mới xử lý thế nào. Không ghi
chung chung “revert commit” khi có migration/event/external side effect.

## 16. Verification commands

Ghi command chạy từ directory nào, prerequisite và expected result. Ví dụ:

```powershell
# Backend
.\mvnw.cmd test

# Frontend — dependency đã được cài theo pnpm policy
pnpm lint
pnpm build
```

Current chưa có `pnpm-lock.yaml`; nếu plan chuẩn hóa lockfile thì phải mô tả
việc migration đó, không dùng `--frozen-lockfile` trước khi lockfile tồn tại.
Nếu command cần PostgreSQL/Redis/Ollama/object storage, nêu cách cung cấp test
dependency và environment variable không chứa secret.

## 17. Acceptance evidence

Nêu artifact cần lưu để đối chiếu:

- test report/contract diff;
- migration verification;
- API example hoặc screenshot có dữ liệu test;
- performance result nếu có NFR;
- security/authorization evidence;
- observability screenshot/query;
- link ADR/PRD/TDD/API docs đã cập nhật.

Không ghi log tiến độ ở đây. Khi hoàn tất, ghi implementation thực tế và mọi
sai khác quan trọng so với plan trong bằng chứng nghiệm thu.

## 18. Resume

Nếu việc thực hiện bị gián đoạn, phần này phải đủ để tiếp tục mà không cần chat
riêng:

- milestone gần nhất đã được kiểm chứng và bằng chứng tương ứng;
- milestone kế tiếp cùng lệnh verification đầu tiên cần chạy;
- decision/blocker còn mở và link tới nguồn;
- prerequisite môi trường, migration hoặc dữ liệu mẫu cần khôi phục;
- Current evidence cần đọc lại nếu code đã thay đổi từ lần kiểm chứng gần nhất.

Resume chỉ giữ ngữ cảnh tiếp tục plan; không biến thành nhật ký tiến độ hoặc
state tracker.
````

## 4. Tiêu chí tự kiểm một ExecPlan

ExecPlan chưa sẵn sàng để thực hiện nếu gặp một trong các trường hợp:

1. Target được mô tả như thể đã tồn tại trong Current.
2. Một PRD blocker bị thay bằng giả định kỹ thuật không được ghi như quyết
   định.
3. Không xác định actor, scope, company context hoặc unauthorized behavior.
4. API đổi nhưng không có consumer/compatibility/error contract.
5. Schema đổi nhưng thiếu identity, constraint, backfill hoặc rollback.
6. Async flow thiếu idempotency, retry, outbox/transaction boundary.
7. KPI/aggregate flow thiếu source version, partial/stale/freshness semantics.
8. Milestone chỉ chia theo layer và không tạo lát cắt kiểm chứng được.
9. Test chỉ có happy path hoặc phụ thuộc implementation details.
10. Rollback chỉ nói “revert” dù đã ghi data/event.
11. Command không nêu working directory/prerequisite/expected result.
12. Plan biến thành nhật ký tiến độ hoặc backlog thay vì mô tả cách đạt outcome.
13. Thiếu Risks hoặc không có tín hiệu rollback quan sát được.
14. Thiếu Resume đủ để tiếp tục sau khi công việc bị gián đoạn.

## 5. Quan hệ với tài liệu khác

- Kiến trúc và trust/data flows: [architecture.md](architecture.md).
- Contract Current/Target và route gaps:
  [api-contracts.md](api-contracts.md).
- Quyết định, ADR và domain blockers: [decisions.md](decisions.md).
- Product requirements:
  [prd.md](prd.md).
- Technical target:
  [technical-design.md](technical-design.md).

Khi implementation làm thay đổi một nguồn sự thật, cập nhật nguồn đó trong
cùng thay đổi; không để ExecPlan trở thành bản sao thay thế PRD/TDD/API docs.
