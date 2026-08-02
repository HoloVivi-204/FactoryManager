# Release Must Traceability

Ngày đối chiếu tài liệu: **2026-08-01**
Baseline mã nguồn: nhánh `main`, commit `69ffcdf`
Nguồn yêu cầu: `PRD_He_thong_hieu_suat_nha_may.md` v2.5, 47 dòng có mức ưu tiên `Must`.

## Quy ước trạng thái

- `Implemented`: có luồng chạy và bằng chứng mã nguồn đáp ứng trực tiếp tiêu chí.
- `Partial`: có một phần luồng nhưng còn thiếu tiêu chí acceptance hoặc có lỗ hổng.
- `Missing`: chưa có năng lực bắt buộc hoặc implementation hiện tại đi ngược guardrail.
- `Blocked`: chưa thể nghiệm thu do thiếu quyết định/deliverable bắt buộc.
- `Out of scope`: không thuộc MVP; không dùng cho 47 Must trong bảng này.
- `Unverified`: có dấu hiệu implementation nhưng chưa có bằng chứng test đủ để xác nhận.

## Ma trận 47 Must

| # | FR | Status | Evidence hiện tại | Gap để đạt Must |
|---:|---|---|---|---|
| 1 | FR-CFG-01 | Partial | `factory-management/src/main/java/com/factory/management/entity/Shift.java:24`; `factory-management/src/main/resources/application.yaml:2`; không có company-settings controller/entity | Có cấu hình runtime kỹ thuật và danh mục ca rời rạc; thiếu cấu hình công ty, ngày sản xuất và thuật ngữ không cần sửa code. |
| 2 | FR-CFG-02 | Missing | Header Excel hardcode tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/StagingReportExcelService.java:43` | Thiếu template version và mapping cột theo công ty/mẫu file. |
| 3 | FR-CFG-03 | Partial | Có danh mục downtime; KPI được tính hardcode tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:46` | Thiếu ngưỡng cảnh báo, taxonomy trạng thái máy, KPI bật-tắt và cấu hình theo company. |
| 4 | FR-CFG-04 | Missing | `factory-management/src/main/java/com/factory/management/entity/ProductionReport.java:23` lưu shift/factory/department/line/team/machine nhưng không có `companyId` | Thiếu `companyId` trên dữ liệu và cấu hình. |
| 5 | FR-CFG-05 | Blocked | `factory-management/src/main/java/com/factory/management/entity/Shift.java:30`, `factory-management/src/main/java/com/factory/management/entity/Shift.java:33` và `factory-management/src/main/java/com/factory/management/entity/Shift.java:36` chỉ lưu giờ bắt đầu/kết thúc và active | Thiếu production calendar/effective dating; production-day và night-shift rule vẫn là quyết định mở. |
| 6 | FR-MDM-01 | Partial | Có CRUD master và soft deactivate, ví dụ `factory-management/src/main/java/com/factory/management/controller/FactoryController.java:32`, `factory-management/src/main/java/com/factory/management/controller/MachineController.java:24` | Chưa đủ định mức có version/effective date và kiểm soát lịch sử toàn bộ danh mục. |
| 7 | FR-MDM-02 | Missing | Máy trỏ trực tiếp team hiện tại tại `factory-management/src/main/java/com/factory/management/entity/Machine.java:52` | Thiếu `validFrom`/`validTo` và lịch sử assignment; báo cáo quá khứ có thể sai cơ cấu. |
| 8 | FR-MDM-04 | Partial | Có `factory-management/src/main/java/com/factory/management/entity/UserDataScope.java` và `factory-management/src/main/java/com/factory/management/security/AuthorizationScope.java:138` | Master GET chỉ cần authenticated; update staging kiểm record cũ rồi cho đổi team mới ngoài scope. |
| 9 | FR-MDM-05 | Partial | Có `factory-management/src/main/java/com/factory/management/entity/Product.java:22` và Product API | ProductionReport/import/dashboard không gắn `productId`, chưa có tiêu chí bật dimension và ẩn filter khi danh mục chưa đáng tin cậy. |
| 10 | FR-ING-01 | Partial | Form và API tạo staging tại `factory-management/src/main/java/com/factory/management/controller/ProductionReportStagingController.java:25`; UI tại `factory-management-frontend/src/pages/TeamLeaderReportPage.tsx:321` | Thiếu product/production-state theo canonical model và provenance đầy đủ. |
| 11 | FR-ING-02 | Partial | Có template/preview/import tại `factory-management/src/main/java/com/factory/management/controller/StagingReportExcelController.java:29` | Chỉ một dòng lỗi làm toàn file không lưu tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/StagingReportExcelService.java:113`; không có file lỗi tải xuống. |
| 12 | FR-ING-03 | Missing | Official chỉ liên kết source staging cấp record tại `factory-management/src/main/java/com/factory/management/entity/ProductionReport.java:20` | Thiếu provenance theo từng giá trị, loại nguồn, người xác nhận và correction source. |
| 13 | FR-ING-05 | Blocked | Có business key staging tại `factory-management/src/main/java/com/factory/management/entity/ProductionReportStaging.java:10` và upsert DRAFT | Logical identity/matching rule chưa được chốt; thiếu source batch, idempotency và dedup qua lần import lỗi một phần. |
| 14 | FR-VAL-01 | Partial | Staging tách bảng tại `factory-management/src/main/java/com/factory/management/entity/ProductionReportStaging.java:9`; dashboard đọc `ProductionReport` official | Có isolation cơ bản nhưng state/provenance và acceptance E2E chưa đủ để ghi Implemented. |
| 15 | FR-VAL-02 | Blocked | `factory-management/src/main/java/com/factory/management/entity/ProductionReportStatus.java:3` không có Error/Warning/Information | Severity/threshold rule còn quyết định mở; thiếu rule engine, Warning override và lý do override. |
| 16 | FR-VAL-03 | Blocked | Import dùng lại entity hiện có tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/StagingReportExcelService.java:518` rồi đặt lại DRAFT tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/StagingReportExcelService.java:534` | Identity/precedence còn mở; current ghi đè thay vì tạo conflict và `Needs Review`. |
| 17 | FR-VAL-04 | Missing | Excel controller chỉ có template/preview/import tại `factory-management/src/main/java/com/factory/management/controller/StagingReportExcelController.java:29` | Thiếu conflict-group API/UI và so sánh field/source/user/time. |
| 18 | FR-CLO-01 | Blocked | Approve từng staging tạo official ngay tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportStagingServiceImpl.java:210` | Coverage/closure grain còn quyết định mở; current thiếu closure theo ngày + scope và Error blocker. |
| 19 | FR-CLO-02 | Blocked | Không có `/closures`; dashboard response không có coverage/partial | Coverage và hierarchy close còn mở; thiếu partial close và trạng thái cấp trên. |
| 20 | FR-CLO-03 | Missing | Official unique theo source staging tại `factory-management/src/main/java/com/factory/management/entity/ProductionReport.java:10` | Thiếu reopen request/approval, correction workspace, reason, late-data window và version cũ. |
| 21 | FR-CLO-04 | Missing | Không có recalc/reclose endpoint hoặc official version fields | Thiếu propagation KPI, trạng thái adjusted và lịch sử phiên bản. |
| 22 | FR-KPI-01 | Blocked | KPI hardcode tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:46`; không có KPI Dictionary artifact/entity | Cần dictionary được nghiệp vụ phê duyệt và implementation có version. |
| 23 | FR-KPI-02 | Partial | Core production dashboard chưa xếp hạng cá nhân, nhưng có `factory-management/src/main/java/com/factory/management/entity/EmployeeKpi.java:26` và UI “KPI cá nhân” tại `factory-management-frontend/src/routes/roleNavigation.ts:121` | Phải cô lập hoặc loại capability cá nhân khỏi MVP để không xung đột guardrail. |
| 24 | FR-KPI-03 | Blocked | Dashboard lấy trung bình KPI record tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:172` | Aggregation/weighting/missing rule chưa được quyết định; current simple average không đủ. |
| 25 | FR-KPI-05 | Blocked | Công thức được code cố định tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:46`; không có formula editor | Guardrail công thức kiểm soát có nền tảng, nhưng formula type/KPI Dictionary target chưa được chốt và chưa có test. |
| 26 | FR-DASH-01 | Partial | Có `/dashboard/my-scope` tại `factory-management/src/main/java/com/factory/management/controller/ProductionReportController.java:39` và filter scope tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:211` | Chưa chứng minh đầy đủ mọi read path; `factory-management-frontend/src/pages/RoleOverviewPage.tsx:38` bỏ qua error sau loading và `factory-management-frontend/src/utils/format.ts:2` đổi missing thành `0`. |
| 27 | FR-DASH-02 | Partial | Search chỉ có date/factory/team/machine tại `factory-management/src/main/java/com/factory/management/controller/ProductionReportController.java:29` | Thiếu line, shift, product, downtime reason và đồng bộ filter toàn dashboard. |
| 28 | FR-DASH-03 | Missing | Không có comparison endpoint trong `factory-management/src/main/java/com/factory/management/controller/ProductionReportController.java` | Thiếu kỳ trước cùng scope/filter và điều kiện completeness. |
| 29 | FR-DASH-04 | Partial | Có report details tại `factory-management/src/main/java/com/factory/management/controller/ProductionReportController.java:24`; frontend gọi route không tồn tại tại `factory-management-frontend/src/api/dashboardApi.ts:21` | Trang Reports theo scope hiện lỗi contract; target còn thiếu hierarchy → contributor → source/provenance → audit summary. |
| 30 | FR-DASH-05 | Partial | Có tổng hợp quantity/downtime/KPI tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/ProductionReportServiceImpl.java:166` | Chưa tổng hợp thành danh sách máy/tổ dưới ngưỡng và top loss reason có drill-down. |
| 31 | FR-NOTI-02 | Missing | Notification hiện chủ yếu HR/manual tại `factory-management/src/main/java/com/factory/management/controller/HrManagementController.java:142`; không có scheduler | Thiếu nhắc hạn chốt và danh sách dữ liệu đang chặn closure. |
| 32 | FR-NOTI-03A | Partial | Excel import xử lý đồng bộ và trả kết quả tại `factory-management/src/main/java/com/factory/management/service/ServiceImpl/StagingReportExcelService.java:111` | Có feedback trong response nhưng thiếu notification async success/partial/failure và trạng thái job. |
| 33 | FR-SEC-01 | Unverified | JWT stateless và authenticated-by-default tại `factory-management/src/main/java/com/factory/management/config/SecurityConfig.java:30` | Có implementation nền nhưng không có security acceptance test; cần bỏ secret/admin fallback. |
| 34 | FR-SEC-02 | Partial | Có method security và `AuthorizationScope`; ví dụ `factory-management/src/main/java/com/factory/management/controller/ProductionReportStagingController.java:24` | Master GET không data-scope; update staging có thể đổi sang team ngoài phạm vi. |
| 35 | FR-SEC-03 | Partial | Core production flow không dùng trực tiếp cho lương/kỷ luật, nhưng `factory-management/src/main/java/com/factory/management/entity/EmployeeKpi.java:26` và trang KPI cá nhân vẫn tồn tại | Phải cô lập/loại module cá nhân khỏi release MVP và chứng minh không dùng cho quyết định bị cấm. |
| 36 | FR-AUD-01 | Partial | Có append-only `factory-management/src/main/java/com/factory/management/entity/AuditEvent.java:18`; Excel import/delete draft có ghi audit | Create/update/submit/approve/close/reopen/reclose/warning/conflict chưa đủ before/after/reason. |
| 37 | FR-AUD-02 | Partial | Role/scope và một số master changes có audit | Thiếu audit đăng nhập thất bại, auto-lock và lịch sử đầy đủ định mức/danh mục. |
| 38 | FR-AUD-03 | Partial | Audit API chỉ ADMIN tại `factory-management/src/main/java/com/factory/management/controller/AuditController.java:20` | Thiếu AUDITOR/scoped permission và export được kiểm quyền/audit. |
| 39 | FR-NAV-01 | Missing | `factory-management-frontend/src/routes/roleNavigation.ts` dùng label khác nhau theo role | Thiếu module catalog và nhãn chuẩn PRD 4.4. |
| 40 | FR-NAV-02 | Missing | Current có `TEAM_LEADER` trong backend enum và workspace riêng | Chưa chuyển Ca trưởng/Tổ trưởng thành employee position có hiệu lực, tách khỏi security role. |
| 41 | FR-NAV-03 | Partial | Frontend có menu theo role; backend có guard rời rạc | Chưa capability-gate module thống nhất; ẩn menu chưa chứng minh API deny ngoài quyền. |
| 42 | FR-MDM-06 | Partial | Có `EmployeeAssignment` và leader relation trong `Team` | Chưa có position history contract tách chức danh khỏi quyền và test giữ lịch sử. |
| 43 | FR-ING-06 | Missing | Current có `reportDate` nhưng không có `businessDataCode`/`dataTimeClass` | Thiếu bốn lớp thời gian, source/received time và classification policy versioned. |
| 44 | FR-VAL-05 | Missing | Không có match-candidate/scorer resource | Thiếu warning boundary từ 90% đến dưới 100%, explanation/model-policy version, human-only resolution và ADR-13. |
| 45 | FR-VAL-06 | Missing | Excel import hiện không tạo reconciliation run/item | Thiếu Matched/Mismatch/Missing, OCR secondary evidence, idempotent resolution và audit. |
| 46 | FR-DASH-08 | Partial | Có vài card/chart trong overview và executive dashboard | Thiếu catalog tối thiểu tám view, shared filter/status, capability state, drill-down/table fallback. |
| 47 | FR-SEC-05 | Missing | Current có tám enum role và `TEAM_LEADER` là role bảo mật | Thiếu mapping ba cấp vận hành, functional action roles và negative migration tests không nâng quyền. |

## Tổng hợp

| Status | Số Must |
|---|---:|
| Implemented | 0 |
| Partial | 22 |
| Missing | 15 |
| Blocked | 9 |
| Unverified | 1 |
| **Tổng** | **47** |

Các con số là phân loại traceability, không phải phần trăm hoàn thành. Một yêu cầu `Partial` vẫn chưa đạt acceptance. Vì còn `Missing` và `Blocked`, release hiện tại không đạt sign-off MVP.
