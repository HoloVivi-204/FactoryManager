# Final Status

Ngày audit mã nguồn: **2026-07-30**
Ngày đồng bộ yêu cầu và tài liệu: **2026-08-01**
Baseline code được audit: nhánh `main`, commit `69ffcdf`.

## Quyết định

| Mục tiêu | Status | Kết luận |
|---|---|---|
| Demo nội bộ luồng hiện có | Partial | Có thể demo có kiểm soát khi PostgreSQL, biến môi trường và dữ liệu mẫu đã chuẩn bị; loại `ReportsPage` khỏi happy path và xác minh response dashboard trước khi diễn giải KPI. |
| Nghiệm thu toàn bộ MVP theo PRD/TDD | Blocked | 0/47 Must đủ evidence để ghi `Implemented`; 22 `Partial`, 15 `Missing`, 9 `Blocked`, 1 `Unverified`. |
| Triển khai production | Blocked | Chưa có migration versioned, rollback/restore proof, health/monitoring, test gate xanh và secret-safe defaults. |

Chi tiết 47 Must: [RELEASE_MUST_TRACEABILITY.md](./RELEASE_MUST_TRACEABILITY.md).

## Kết quả build và test đã quan sát

| Ngày | Hạng mục | Command/evidence | Kết quả | Chi tiết |
|---|---|---|---|---|
| 2026-07-30 | pnpm trên Windows | `pnpm.cmd --version` | Pass | Phiên bản `11.9.0`. |
| 2026-07-30 | Frontend production build | `pnpm.cmd run build` | Pass | TypeScript và Vite build thành công; JS chính 852,80 kB, Vite cảnh báo chunk vượt 500 kB. |
| 2026-07-30 | Maven Wrapper | `.\mvnw.cmd -v` | Pass | Maven 3.9.16 chạy trên Java runtime 21; project vẫn compile với release Java 17. |
| 2026-07-30 | Backend package | `.\mvnw.cmd -DskipTests package` | Pass có điều kiện | JAR build thành công; command cố ý bỏ test nên không phải release gate đầy đủ. |
| 2026-07-29 | Frontend lint | `pnpm.cmd run lint` | Fail | Exit 1: **59 errors, 12 warnings**; phần lớn `no-explicit-any` và hook dependency. |
| 2026-07-29 | Frontend typecheck | `pnpm.cmd exec tsc -b --pretty false` | Pass | Exit 0. |
| 2026-07-30 | Backend test phase | `factory-management/target/surefire-reports/com.factory.management.FactoryManagementApplicationTests.txt` | Fail | 1 test, 0 failure, 1 error; PostgreSQL yêu cầu SCRAM authentication nhưng test không cung cấp password. |
| 2026-07-29 | Backend test coverage | `factory-management/src/test/java/com/factory/management/FactoryManagementApplicationTests.java:6` | Missing | Chỉ có `@SpringBootTest contextLoads`; không có unit/integration/contract/security/performance/E2E suite. |

Lỗi backend test hiện tại phản ánh test phụ thuộc database ngoài và không có
test profile/container hay credential test tự cô lập; nó không chứng minh toàn
bộ application logic hỏng, nhưng vẫn làm release gate đỏ.

## Những phần đang dùng được

- JWT stateless, đăng nhập/refresh/logout/change-password và method security.
- CRUD nhiều master-data, soft deactivate.
- Manual production staging và bốn nhóm chi tiết staging.
- Excel template/preview/import cho file hoàn toàn hợp lệ.
- Luồng `DRAFT → SUBMITTED → APPROVED`, approve tạo official report.
- Dashboard official mức tổng hợp cơ bản và report-details API.
- Audit event nền tảng, request correlation và error handler chung.

Các phần trên chỉ đủ cho demo development slice; thao tác “approve” hiện tại không tương đương closure workflow trong PRD.
Không tính `ReportsPage` là phần dùng được: client gọi `/production-reports/search/my-scope`, nhưng backend
không có route này.

## Release blockers chính

1. Không có `companyId`, company settings, production calendar và effective dating.
2. Import lỗi một phần không được lưu phần hợp lệ; thiếu source batch, conflict review và field provenance.
3. Thiếu closure/partial close/reopen/correction/reclose/version/recalculation.
4. Thiếu KPI Dictionary/version/snapshot, period aggregation chuẩn, comparison và full drill-down.
5. Data scope còn lỗ hổng: master GET chưa scope; staging update kiểm team cũ nhưng có thể đổi team mới.
6. Có JWT signer và bootstrap-admin fallback trong `application.yaml`; production bắt buộc lấy secret từ environment/secret manager và fail-fast khi thiếu.
7. Database dùng `schema.sql` `mode: always` cùng `ddl-auto: update`; không có Flyway/Liquibase hoặc rollback plan.
8. Frontend lint đỏ, backend test đỏ và coverage gần như trống; build bỏ test không thay thế test gate.
9. Không có outbox processor/worker, readiness/health endpoint, structured monitoring, backup/restore rehearsal.
10. Frontend/backend lệch contract scoped report search nên `ReportsPage` bị hỏng.
11. `RoleOverviewPage` có thể bỏ qua lỗi summary và formatter đổi missing thành zero, gây nguy cơ trình bày
    lỗi/thiếu dữ liệu như KPI `0`.
12. Role/menu Current vẫn dùng tám enum và workspace theo role; Target cần ba cấp
    `Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`, functional permissions trực giao,
    Ca trưởng/Tổ trưởng là chức danh và navigation theo module/capability.
13. Chưa có `businessDataCode`, bốn lớp thời gian dữ liệu, cảnh báo gần trùng từ 90% đến dưới 100% hoặc đối soát
    Excel–web; OCR nếu bật chỉ được là bằng chứng thứ yếu.
14. Dashboard chưa có catalog tối thiểu tám biểu đồ/view dùng chung filter, trạng thái
    empty/partial/stale, table fallback và drill-down.

## Ranh giới phạm vi MVP và extension

| Module | Status | Ghi chú |
|---|---|---|
| Nhân sự và vật tư phục vụ vận hành | MVP target có giới hạn | Chỉ gồm hồ sơ/phân công/chức danh vận hành, danh mục và lượng dùng/sự cố vật tư gắn với sản xuất; implementation Current chưa tự động được tính là đạt Must. |
| Chấm công, nghỉ phép, tăng ca, lương và KPI cá nhân | Ngoài MVP | Không được dùng dữ liệu hiệu suất để xếp hạng cá nhân, trả lương hoặc kỷ luật. |
| Kho đầy đủ và bảo trì/lập lịch đầy đủ | Extension sau MVP | Chỉ hiển thị tab khi capability được bật và người dùng có quyền; không dùng các module Current để bù các Must còn thiếu. |
| Finance | Ngoài MVP | Không được dùng để bù cho các Must production-performance còn thiếu. |
| OCR phiếu giấy | Should, tắt mặc định | Chỉ là bằng chứng đối soát thứ yếu; phải chốt TBD-22 trước khi bật và không được tự ghi official. |
| AI chatbot | Ngoài MVP | Capability thử nghiệm; không phải tiêu chí sign-off MVP. |

Đặc biệt, `EmployeeKpi` và trang “KPI cá nhân” còn xung đột với FR-KPI-02/FR-SEC-03, không chỉ là scope mở rộng.

## Điều kiện tối thiểu để đổi sang GO

- Tất cả 47 Must chuyển sang `Implemented` với test/evidence; mọi quyết định đang `Blocked` được ghi nhận
  rõ trong PRD/TDD/DECISIONS cùng ngày, lựa chọn và tác động.
- Sửa data-scope bypass và thêm test âm cho đọc/ghi/export ngoài phạm vi.
- Loại bỏ secret fallback, tắt bootstrap mặc định và dùng environment/secret manager.
- Có migration versioned, backup/restore rehearsal và rollback/forward plan.
- `pnpm.cmd run lint`, `pnpm.cmd run build` và `.\mvnw.cmd test` đều exit 0 trên clean checkout.
- Đồng bộ scoped report-list API với frontend, đồng thời có test bảo đảm loading/error/missing không hiển thị
  như dữ liệu zero.
- Hoàn tất migration role/navigation có negative permission test; đổi chức danh Ca trưởng/Tổ trưởng
  không tạo hoặc nâng quyền hệ thống.
- Có test cho phân loại dữ liệu lịch sử, exact-first matching, biên cảnh báo `0.90`/`1.00` và đối soát
  Excel–web; nếu bật OCR, mọi field phải có confidence và evidence được kiểm quyền.
- Catalog biểu đồ cốt lõi dùng official snapshot, cùng filter và có trạng thái empty/partial/stale,
  table fallback và drill-down về contributor/source.
- Có test cho partial import, dedup/retry, conflict, close/partial close, reopen/reclose, KPI recalculation và concurrency.
- Có health/readiness, structured logging/metrics/alerts và performance evidence theo TDD.

Cho đến khi các điều kiện trên hoàn tất, nhãn phát hành đúng là **development demo**, không phải **MVP complete** hoặc **production-ready**.
