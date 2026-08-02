# Mục lục tài liệu FactoryManager

## Bắt đầu theo nhu cầu

| Nhu cầu | Đọc theo thứ tự |
|---|---|
| Hiểu sản phẩm | [Mô tả dự án](../MO_TA_DU_AN.md) → [PRD](../PRD_He_thong_hieu_suat_nha_may.md) |
| Hiểu thiết kế đích | [TDD](../TDD_He_thong_hieu_suat_nha_may.md) → [Decisions](../DECISIONS.md) → [Architecture](../ARCHITECTURE.md) |
| Làm backend/API | [API Contracts](../API_Contracts.md) → [backend docs](../factory-management/docs/) |
| Làm frontend | [Screen map](ui/SCREEN_MAP.md) → [Design system](../DESIGN_SYSTEM.md) |
| Chọn việc tiếp theo | [Decisions](../DECISIONS.md) → [Backlog](../Backlog_He_thong_hieu_suat_nha_may.md) |
| Chuẩn bị test/demo | [Hướng dẫn test](../HUONG_DAN_TEST.md) → [Demo runbook](release/DEMO_RUNBOOK.md) |
| Đánh giá release | [Must traceability](release/RELEASE_MUST_TRACEABILITY.md) → [Final status](release/FINAL_STATUS.md) |
| Triển khai | [Deployment runbook](release/DEPLOYMENT_RUNBOOK.md) |
| Thay đổi lớn | [PLANS](../PLANS.md) |

## Nguồn và thứ tự ưu tiên

### Target

Nguồn authoritative, theo thứ tự:

1. [PRD hệ thống hiệu suất nhà máy](../PRD_He_thong_hieu_suat_nha_may.md) cho
   phạm vi, hành vi và acceptance.
2. [TDD hệ thống hiệu suất nhà máy](../TDD_He_thong_hieu_suat_nha_may.md) cùng
   các quyết định `Accepted` trong [DECISIONS.md](../DECISIONS.md) cho thiết kế,
   schema, API, hard limit và test.

Nguồn hỗ trợ đứng sau hai nguồn trên:

3. [Cây domain, acceptance scenario và stable ID](../content-skeleton.yaml)
   cùng [ma trận requirement/evidence](../factory-performance-feature-matrix.csv).
4. [Backlog](../Backlog_He_thong_hieu_suat_nha_may.md) chỉ quyết định thứ tự
   thực hiện, không mở rộng phạm vi.

### Current

Ưu tiên code/config/migration đang build, sau đó tới test có thể chạy lại, bằng
chứng build/release và cuối cùng mới là tài liệu cạnh implementation.

### Tài liệu dẫn xuất

- [Kiến trúc hệ thống](../ARCHITECTURE.md)
- [Hợp đồng API](../API_Contracts.md)
- [Design system](../DESIGN_SYSTEM.md)
- [Screen map](ui/SCREEN_MAP.md)

Các tài liệu này giải thích hoặc cụ thể hóa nguồn authoritative. Chúng không tự
khóa một quyết định đang Open và không phải bằng chứng rằng Target đã tồn tại.

### Trạng thái và bằng chứng

- [Release Must traceability](release/RELEASE_MUST_TRACEABILITY.md)
- [Demo runbook](release/DEMO_RUNBOOK.md)
- [Deployment runbook](release/DEPLOYMENT_RUNBOOK.md)
- [Final status](release/FINAL_STATUS.md)

Release docs là snapshot theo bằng chứng, không phải nguồn định nghĩa sản phẩm.

## Tài liệu cạnh implementation

Thư mục [`factory-management/docs/`](../factory-management/docs/) mô tả nhiều endpoint hiện tại như
authentication, authorization, master data, staging, production report và Excel. Đây là tài liệu gần code,
phải được đối chiếu với controller/service trước khi sử dụng.

Các trang về HR, tài chính, kho, bảo trì hoặc chatbot mô tả capability đã tồn tại trong repository.
PRD v2.5 chỉ đưa nhân sự/vật tư ở phạm vi vận hành vào lõi; kho đầy đủ và bảo trì đầy đủ cần capability
riêng, còn chấm công/lương/KPI cá nhân, tài chính và chatbot không thuộc Must. Không dùng code hiện hữu để tự mở rộng release scope.

## Quy tắc cập nhật

1. Thay đổi hành vi bắt đầu ở PRD.
2. Thay đổi schema/API/NFR bắt đầu ở TDD hoặc một quyết định được ghi nhận.
3. Cập nhật baseline, architecture, API contract và screen map trước hoặc cùng implementation.
4. Cập nhật test, hướng dẫn và traceability từ hành vi đã kiểm chứng.
5. Không ghi `Implemented` nếu chỉ có code mà chưa có evidence phù hợp acceptance.
6. Khi tài liệu mâu thuẫn, áp dụng trực tiếp thứ tự Target/Current ở trên và ghi
   quyết định còn mở trong `DECISIONS.md`.
