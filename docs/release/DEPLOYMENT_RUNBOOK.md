# Deployment Runbook FactoryManager

## 1. Trạng thái

Repository hiện **chưa sẵn sàng production**. Runbook này định nghĩa gate và trình tự target; nó không cấp
quyền triển khai production khi các blocker vẫn còn.

Blocker chính:

- Không có migration versioned; startup dùng `schema.sql` cùng `ddl-auto=update`.
- Secret và bootstrap admin còn fallback trong source.
- Test suite gần như trống và không tự cô lập.
- Có data-scope gap khi update staging sang team khác.
- Closure/correction/versioning/KPI snapshot chưa tồn tại đầy đủ.
- Chưa có backup/restore drill, RPO/RTO, health/readiness và monitoring release-grade.

## 2. Topology target tối thiểu

```text
Browser
  → HTTPS reverse proxy/load balancer
  → Spring Boot API
       → PostgreSQL
       → outbox worker/queue khi được triển khai
       → object storage cho source file khi được triển khai

Static frontend
  → versioned immutable assets
```

Dashboard overview phải đọc snapshot/cache target; không scale production bằng cách tiếp tục `findAll()` và
aggregate toàn bộ record trong request.

## 3. Môi trường

| Environment | Mục đích | Dữ liệu |
|---|---|---|
| Local | Phát triển | Synthetic/local |
| Test/CI | Unit, integration, contract | Disposable |
| Staging | Migration, smoke, UAT, performance | Synthetic hoặc đã ẩn danh |
| Production | Vận hành | Dữ liệu thật theo retention/security policy |

Không dùng chung database, signing key hoặc account giữa các môi trường.

## 4. Secret và configuration

Tối thiểu:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SIGNER_KEY`
- `JWT_VALID_DURATION`
- `JWT_REFRESH_DURATION`
- `ADMIN_BOOTSTRAP_ENABLED=false`
- `VITE_API_URL` tại frontend build/deploy

Nếu AI capability được bật riêng:

- `AI_ENABLED`
- `OLLAMA_CHAT_URL`
- `OLLAMA_MODEL`
- Các timeout/rate-limit tương ứng

Production cần secret manager hoặc cơ chế inject environment của nền tảng. Không lưu secret trong image,
source, frontend bundle, command history hoặc tài liệu.

Ứng dụng phải fail startup khi thiếu production secret bắt buộc. Bootstrap admin không được tự bật.

## 5. Release gates

### 5.1. Product

- PRD/TDD version dùng cho release được xác định.
- Mọi Must có status và evidence trong `RELEASE_MUST_TRACEABILITY.md`.
- Không còn Blocked requirement thuộc release.
- KPI Dictionary và worked examples đã được ghi nhận nếu dashboard official nằm trong release.

### 5.2. Security

- Data-scope test phủ read/write/export và relation target khi update.
- Không có fallback signer/admin credential trong production profile.
- Upload kiểm MIME/size và chính sách malware phù hợp.
- Failed login/rate limit/account lock đáp ứng policy.
- Dependency/security scan không còn finding blocker.

### 5.3. Database

- Flyway/Liquibase hoặc công cụ migration versioned đã thay startup mutation hiện tại.
- Migration chạy thành công từ bản production gần nhất trên snapshot staging.
- Backfill idempotent và có progress/checksum.
- Backup trước migration đã xác minh đọc được.
- Restore drill đạt RPO/RTO được ghi nhận.

### 5.4. Test và hiệu năng

- Backend unit/integration/contract/security tests pass trên CI disposable DB.
- Frontend lint/build/component/E2E pass.
- Scenario import 95/5, dedup, conflict, close, partial, correction và scope denial pass.
- Dashboard p95 < 3 giây và drill-down p95 < 5 giây trên profile tải đã chốt.
- KPI sau close/reclose cập nhật dưới SLA hoặc UI hiện `Đang cập nhật`.

### 5.5. Operations

- Health, readiness và liveness endpoint.
- Structured log có request ID nhưng không chứa secret/PII không cần thiết.
- Metrics API latency, job failure/retry, outbox lag, snapshot freshness và missing data.
- Alert có threshold và cách xử lý.
- Rollback app/schema đã diễn tập.

Không triển khai production nếu một gate trên chưa đạt.

## 6. Build artifact

Backend target:

```powershell
cd factory-management
.\mvnw.cmd test
.\mvnw.cmd package
```

Frontend:

```powershell
cd factory-management-frontend
pnpm.cmd install --frozen-lockfile
pnpm.cmd run lint
pnpm.cmd run build
```

Repository hiện còn `package-lock.json` và chưa có pnpm lockfile được commit; vì vậy
`--frozen-lockfile` là target gate chưa chạy được cho tới khi package-manager migration được thực hiện có
chủ đích. Không tạo lockfile production trong lúc deploy.

Artifact phải gắn commit SHA, build time, dependency lock checksum và migration version.

## 7. Trình tự triển khai target

1. Chọn version đã qua release gates.
2. Tạo và xác minh backup.
3. Chạy migration pre-deploy tương thích ngược.
4. Deploy backend canary/rolling với readiness.
5. Smoke test API/auth/scope.
6. Deploy frontend assets versioned.
7. Bật worker/outbox consumer sau khi schema và API đã sẵn sàng.
8. Theo dõi error rate, latency, DB, queue/outbox và snapshot freshness.
9. Chạy smoke/UAT có ID cụ thể.
10. Chỉ hoàn tất khi observation window không có blocker.

Migration phá vỡ tương thích phải dùng expand → migrate/backfill → switch → contract qua nhiều release,
không deploy app và destructive schema change cùng một bước.

## 8. Smoke test

Tối thiểu:

- Health/readiness.
- Login, refresh và logout.
- Account scope A không đọc/sửa scope B.
- Tạo Draft synthetic, submit và review theo capability của release.
- Import file synthetic nhỏ; kiểm tra source batch/outbox nếu đã triển khai.
- Close synthetic scope và chờ snapshot nếu closure đã triển khai.
- Dashboard đúng period/scope/completeness.
- Drill-down không vượt scope.
- Audit có actor/action/reason/request ID phù hợp.

Mọi dữ liệu smoke production dùng namespace riêng và có quy trình dọn an toàn; không xóa official/audit bằng
SQL ad-hoc.

## 9. Monitoring sau deploy

Theo dõi:

- HTTP p50/p95/p99, 4xx/5xx.
- Authentication failure và forbidden spike.
- DB connections, slow query, lock/deadlock.
- Import duration, partial/failure count, retry/DLQ.
- Outbox oldest age và attempts.
- KPI snapshot stale/failed/lag.
- Coverage/missing-data bất thường.
- Frontend error và asset load failure.

Dashboard hoặc alert chưa tồn tại thì production gate chưa đạt; không thay monitoring bằng kiểm tra log thủ
công không liên tục.

## 10. Rollback

### Application

1. Dừng rollout.
2. Tắt worker tạo write mới nếu cần.
3. Chuyển traffic về artifact trước.
4. Xác minh version trước vẫn tương thích schema đã expand.
5. Chạy smoke read/auth/scope.

### Database

- Ưu tiên forward fix cho migration additive.
- Chỉ chạy down migration đã được diễn tập và chứng minh không mất dữ liệu.
- Nếu corruption/mất dữ liệu, cô lập write và thực hiện restore theo runbook đã kiểm chứng.
- Không dùng `git checkout`, `ddl-auto` hoặc SQL thủ công như cơ chế rollback production.

### Frontend

- Chuyển manifest/static origin về asset version trước.
- Purge/invalidate cache theo version, không xóa rộng toàn bộ storage nếu không cần.

## 11. Bằng chứng triển khai

Mỗi lần deploy lưu ngoài source secret:

- Version/commit/artifact checksum.
- Migration version và kết quả.
- Backup/restore reference.
- Smoke test IDs và request IDs đã che dữ liệu.
- Metric snapshot trong observation window.
- Sự cố, rollback hoặc follow-up.

`FINAL_STATUS.md` chỉ tổng hợp kết quả có thể kiểm chứng; không chứa credential hoặc log thô.
