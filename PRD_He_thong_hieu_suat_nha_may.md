# TÀI LIỆU YÊU CẦU SẢN PHẨM (PRD)
## Hệ thống theo dõi hiệu suất nhà máy

| Thông tin | Nội dung |
|---|---|
| Phiên bản | 2.5 |
| Ngày cập nhật | 01/08/2026 |
| Người soạn thảo | [Tên sinh viên thực tập] |
| Người phê duyệt | [Tên mentor] |
| Trạng thái | Bản nháp - chờ mentor phê duyệt các quyết định còn mở |
| Tài liệu kỹ thuật liên quan | [TDD_He_thong_hieu_suat_nha_may.md](./TDD_He_thong_hieu_suat_nha_may.md) |
| Đội ngũ & timeline | Dự án dài hạn, 3 người thực hiện, tiếp tục sau kỳ thực tập theo nhịp độ tăng dần từng ngày — không bị ép vào deadline cứng của kỳ thực tập. Xem mục 15 và mục 16. |
| Thay đổi chính | Xem bảng lịch sử phiên bản bên dưới |

---

## Lịch Sử Phiên Bản

| Phiên bản | Thay đổi chính |
|---|---|
| 2.0 | Tách PRD/TDD; bổ sung business rules, quản trị dữ liệu, danh mục, chốt, conflict, drill-down, privacy, RACI. |
| 2.1 | Tách trạng thái theo đúng đối tượng nghiệp vụ; làm rõ chốt thiếu dữ liệu, KPI cấp trên khi chốt một phần, scope single-tenant MVP, identity/matching rule cho import, quyền thao tác, KPI Dictionary và profile tải. |
| 2.2 | Sửa MoSCoW/scope MVP, quản trị Open Questions, KPI versioning, TDD outbox/locking/API consistency và danh sách câu hỏi sign-off. |
| 2.3 | Ghi rõ bối cảnh dự án dài hạn/đội 3 người, cập nhật khung lộ trình cho phát triển tăng dần thay vì ép theo kỳ thực tập; thêm TBD-21 (chính sách lưu trữ dài hạn, khớp ADR-12 trong TDD). |
| 2.4 | Sửa TBD-02 bị thiếu khỏi bảng phân loại sign-off ở mục 17 (đã xác minh lại toàn bộ 21 TBD bằng script, không còn sai/thiếu); sửa mâu thuẫn vai trò mentor/Product approver ("chỉ trong giai đoạn thực tập" mâu thuẫn với giả định dự án dài hạn ở mục 15.2); sửa câu chữ "nếu còn thời gian" còn sót lại từ khung thực tập cũ ở mục 4.1; bổ sung Guardrail và lưu trữ nóng/lạnh vào bảng thuật ngữ. |
| 2.5 | Chuẩn hóa tên tab và ba cấp quyền vận hành; chuyển Ca trưởng/Tổ trưởng thành chức danh trong Quản lý nhân sự; bổ sung phân loại dữ liệu hiện tại/lịch sử, cảnh báo tương đồng 90–100%, đối soát Excel–web–OCR và catalog biểu đồ mở rộng. |

---

## 1. Tóm Tắt Điều Hành

Hệ thống theo dõi hiệu suất nhà máy là ứng dụng web giúp cấp quản lý và vận hành theo dõi tình trạng máy móc, sản lượng, chất lượng, sự cố và hiệu suất cấp tổ/ca theo dữ liệu đã được kiểm soát.

MVP tập trung vào ba giá trị chính:

- Tập trung hóa dữ liệu vận hành đang nằm rải rác ở sổ tay, tin nhắn và file Excel.
- Tạo quy trình nhập, kiểm tra, chốt, sửa sau chốt và truy vết dữ liệu rõ ràng.
- Cung cấp dashboard có thể truy ngược từ KPI tổng quan về ca, máy, tổ, bản ghi nguồn và người xác nhận.
- Phân biệt dữ liệu hiện tại, dữ liệu lịch sử nhập bù, dữ liệu đến muộn và dữ liệu sửa sau chốt; cảnh báo ứng viên trùng gần giống trước khi người dùng quyết định.
- Đối soát dữ liệu nhập trên web và file Excel với báo cáo giấy OCR như một nguồn bằng chứng thứ yếu, không tự động chọn nguồn thắng.

Hệ thống được định hướng như một sản phẩm có thể triển khai cho nhiều công ty sản xuất khác nhau. Điều này không có nghĩa là mọi công ty đều dùng được mà không cấu hình. Yêu cầu sản phẩm là: phần lõi phải ổn định, còn các yếu tố khác biệt giữa công ty như cơ cấu nhà máy, ca làm việc, mẫu file, danh mục máy, công thức KPI, ngưỡng cảnh báo và nguồn dữ liệu phải được cấu hình hoặc tích hợp qua cơ chế mapping/adapter chuẩn, hạn chế sửa code lõi.

MVP mặc định chạy cho **một công ty**. Hệ thống có thể lưu `companyId` và cấu hình theo công ty để chuẩn bị mở rộng, nhưng quản trị nhiều công ty cùng dùng một instance, cách ly tenant và vận hành multi-tenant không thuộc phạm vi MVP.

Điều khiển bằng giọng nói không còn là mục tiêu lõi của MVP. Đây là tính năng thử nghiệm sau MVP, chỉ triển khai khi có pain point rõ ràng và pilot chứng minh người dùng thực sự cần.

Dự án được thực hiện bởi **3 người** và được mentor định hướng phát triển **dài hạn, tiếp tục sau kỳ thực tập** — mỗi ngày hoàn thiện thêm một phần thay vì phải dồn toàn bộ phạm vi vào một mốc deadline cố định. Vì vậy, phạm vi `Must` trong tài liệu này nên được hiểu là **mục tiêu cuối cùng của giai đoạn nền tảng**, không phải mục tiêu bắt buộc hoàn thành trong những tuần đầu — mục 16 (Lộ trình đề xuất) trình bày cách chia nhỏ theo nhịp độ phù hợp với nhóm 3 người làm liên tục, thay vì theo áp lực rút gọn của một kỳ thực tập ngắn hạn.

---

## 2. Bối Cảnh Và Vấn Đề Kinh Doanh

Hiện tại, thông tin vận hành nhà máy thường được ghi nhận qua nhiều kênh riêng lẻ: sổ tay, phiếu giấy, tin nhắn, file Excel hoặc báo cáo thủ công của từng tổ. Không có một nơi duy nhất để kiểm soát trạng thái dữ liệu từ lúc phát sinh đến khi trở thành số liệu chính thức.

Các vấn đề chính:

- Quản lý phải chờ tổng hợp thủ công trước khi có số liệu đáng tin cậy.
- Dữ liệu giữa nhiều nguồn có thể lệch nhau nhưng không có quy tắc xác định nguồn nào thắng.
- Dữ liệu thiếu hoặc nhập muộn không được phân biệt rõ với giá trị bằng 0.
- Khi KPI thấp, người quản lý khó truy ngược về ca, máy, nguyên nhân dừng, người nhập và dữ liệu nguồn.
- Danh mục máy, tổ, định mức và cơ cấu tổ chức thay đổi theo thời gian nhưng báo cáo lịch sử cần giữ đúng bối cảnh tại thời điểm phát sinh.
- Chưa có quy trình nhất quán cho chốt ngày, mở lại, sửa sau chốt và xử lý dữ liệu đến muộn.

---

## 3. Mục Tiêu Và Nguyên Tắc Sản Phẩm

### 3.1. Mục tiêu sản phẩm

- Tạo một hệ thống trung tâm cho dữ liệu vận hành nhà máy.
- Đảm bảo dữ liệu lên dashboard chính thức đã qua kiểm tra, chốt và có thể truy vết.
- Cho phép quản lý xem KPI theo nhà máy, dây chuyền, tổ, máy, ca và khoảng thời gian.
- Cho phép drill-down từ KPI tổng quan về dữ liệu nguồn để điều tra nguyên nhân.
- Chuẩn hóa quy trình xử lý dữ liệu thiếu, dữ liệu trễ, dữ liệu xung đột và dữ liệu sai sau chốt.
- Hỗ trợ triển khai cho nhiều công ty bằng cấu hình nghiệp vụ và cơ chế mapping/tích hợp nguồn dữ liệu chuẩn.

### 3.2. Nguyên tắc sản phẩm

- PRD mô tả nhu cầu người dùng, quy tắc nghiệp vụ và tiêu chí nghiệm thu.
- TDD mô tả module kỹ thuật, API, database, cache, queue, partitioning, công nghệ triển khai và chiến lược vận hành.
- Dashboard tổng quan phải nhanh và không suy giảm đáng kể khi dữ liệu thô tăng qua nhiều năm.
- Mọi dữ liệu chính thức phải có nguồn gốc, trạng thái, người chịu trách nhiệm và lịch sử thay đổi.
- Không hiển thị dữ liệu thiếu, chưa nhập hoặc đang xử lý thành giá trị 0 nếu chưa có xác nhận nghiệp vụ.
- Báo cáo lịch sử dùng cơ cấu tổ chức, định mức và phân loại có hiệu lực tại thời điểm phát sinh dữ liệu, trừ khi có quy trình điều chỉnh hồi tố được phê duyệt.

---

## 4. Phạm Vi

### 4.1. Trong phạm vi MVP

- Đăng nhập, phân quyền theo vai trò và phạm vi dữ liệu.
- Quản lý danh mục nền tảng: nhà máy, dây chuyền, tổ, máy, ca, định mức, nguyên nhân dừng máy, người dùng và phạm vi phụ trách.
- Nhập dữ liệu vận hành bằng form web.
- Import file dữ liệu theo mẫu đã cấu hình.
- Lưu dữ liệu vào trạng thái tạm, kiểm tra lỗi/cảnh báo, chốt dữ liệu và sửa sau chốt.
- Dashboard KPI cấp nhà máy, dây chuyền, tổ, máy theo ngày, tuần, tháng, năm và khoảng tùy chỉnh.
- Bộ lọc, so sánh kỳ, bộ biểu đồ vận hành đa dạng và drill-down về ca/máy/bản ghi nguồn.
- Phân loại dữ liệu hiện tại, lịch sử nhập bù, đến muộn và sửa sau chốt; phát hiện trùng chính xác và cảnh báo ứng viên giống từ 90% đến dưới 100%.
- Đối soát file Excel từ máy quản lý với dữ liệu nhập trên web; lưu kết quả khớp, lệch, thiếu nguồn và quyết định xử lý.
- Quản lý nhân sự ở mức danh mục, phân công ca/tổ và chức danh vận hành; `Ca trưởng`/`Tổ trưởng` là chức danh của nhân viên, không phải tab hoặc cấp quyền riêng.
- Quản lý vật tư ở mức danh mục, sử dụng thực tế và sự cố vật tư gắn với báo cáo sản xuất; không đồng nghĩa với quản trị kho đầy đủ.
- Audit log cho các hành động quan trọng.
- Cảnh báo dữ liệu thiếu tại màn hình kiểm tra/chốt và thông báo trạng thái import; nhắc chủ động và escalation triển khai theo thứ tự ưu tiên ở lộ trình (mục 16), không phải điều kiện "nếu còn thời gian".
- Thiết lập ban đầu để triển khai single-tenant cho một công ty và ít nhất một nhà máy; mô hình dữ liệu chuẩn bị cho mở rộng nhiều nhà máy trong cùng công ty.

### 4.2. Sau MVP hoặc tùy chọn

- OCR đọc phiếu giấy để tạo bằng chứng đối soát thứ yếu; chỉ bật sau khi có mẫu phiếu, ngưỡng tin cậy và quy trình xác nhận được duyệt.
- Điều khiển bằng giọng nói.
- Xuất dashboard ra PDF/ảnh.
- Tích hợp trực tiếp với hệ thống ERP/MES/HRM của công ty.
- Phân tích nâng cao, dự báo, khuyến nghị bảo trì.
- Quản lý kho đầy đủ gồm nhập, xuất, tồn, kiểm kê và cảnh báo tồn.
- Bảo trì đầy đủ gồm yêu cầu, lập lịch, phiếu công việc và lịch sử bảo trì.

### 4.3. Ngoài phạm vi

- Chấm công chi tiết, tính lương, kỷ luật lao động.
- Mua hàng, định giá tồn kho và kế toán kho; phần vật tư trong MVP chỉ phục vụ bối cảnh sản xuất.
- Kế toán, tài chính, giá vốn, công nợ.
- Chatbot hỏi đáp tự do bằng ngôn ngữ tự nhiên.
- Đánh giá hiệu suất cá nhân trong MVP.

### 4.4. Kiến trúc thông tin và tên tab cấp cao

Navigation được đặt theo năng lực nghiệp vụ, không đặt theo chức danh người dùng. Thứ tự và nhãn chuẩn:

| Thứ tự | Tên tab chuẩn | Phạm vi hiển thị | Ranh giới release |
|---:|---|---|---|
| 1 | Tổng quan điều hành | KPI, cảnh báo, coverage và việc cần xử lý | MVP |
| 2 | Quản lý sản xuất | Kế hoạch–thực tế, người + máy, báo cáo ca, staging và chốt | MVP |
| 3 | Quản lý vận hành | Trạng thái máy, thời gian chạy/dừng, sự cố và bàn giao bảo trì | MVP |
| 4 | Quản lý nhân sự | Hồ sơ, tổ/ca, phân công và chức danh; gồm Ca trưởng/Tổ trưởng | MVP ở phạm vi vận hành; chấm công/lương ngoài phạm vi |
| 5 | Quản lý vật tư | Danh mục, lượng dùng và sự cố vật tư gắn với sản xuất | MVP ở phạm vi vận hành |
| 6 | Quản lý kho | Kho, nhập–xuất–tồn và kiểm kê | Sau MVP hoặc khi capability được bật |
| 7 | Bảo trì & lập lịch | Yêu cầu, lịch, phiếu công việc và lịch sử bảo trì | Sau MVP hoặc khi capability được bật |
| 8 | Dữ liệu & đối soát | Import Excel, dữ liệu lịch sử, ứng viên trùng, OCR và conflict | Excel/web trong MVP; OCR theo capability |

Không tạo tab riêng tên `Ca trưởng/Tổ trưởng`. Một tab sau MVP chỉ xuất hiện khi capability tương ứng được bật và người dùng có quyền; không hiển thị placeholder như chức năng đã sẵn sàng.

---

## 5. Vai Trò Người Dùng

| Vai trò | Phạm vi dữ liệu | Nhu cầu chính |
|---|---|---|
| Quản lý điều hành | Toàn bộ công ty/nhà máy được phân quyền | Xem tổng quan, phát hiện khu vực hiệu suất thấp, điều tra nguyên nhân, theo dõi chốt và duyệt mở lại |
| Quản lý vận hành | Dây chuyền hoặc khu vực phụ trách | Kiểm tra dữ liệu tạm, xử lý cảnh báo/xung đột, chốt dữ liệu và điều phối người + máy |
| Nhân viên vận hành | Tổ, ca hoặc máy được giao | Nhập dữ liệu vận hành, xác nhận không sản xuất và xem tình hình phạm vi của mình |
| Quản trị dữ liệu | Danh mục và cấu hình nghiệp vụ được giao | Quản lý danh mục, định mức, mapping nguồn dữ liệu, quy tắc cảnh báo |
| Quản trị hệ thống | Cấu hình tài khoản và quyền | Quản lý người dùng, role, phạm vi truy cập, chính sách bảo mật |
| Người kiểm toán | Phạm vi được cấp | Xem audit log, đối chiếu dữ liệu, hỗ trợ nghiệm thu và kiểm tra tuân thủ |

Ba vai trò đầu tạo thành cấp bậc vận hành `Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`. `Ca trưởng` và `Tổ trưởng` là chức danh hoặc phân công có ngày hiệu lực của Nhân viên vận hành trong tab Quản lý nhân sự, không phải role hệ thống độc lập. Quản trị dữ liệu, Quản trị hệ thống và Người kiểm toán là quyền chức năng trực giao, không chen vào chuỗi cấp bậc này.

Mentor/Product approver là stakeholder phê duyệt nghiệp vụ xuyên suốt dự án (bao gồm cả sau kỳ thực tập, theo giả định ở mục 15.2), không phải vai trò vận hành sau khi hệ thống được đưa vào sử dụng thật cho người dùng cuối.

### 5.1. RACI tối giản

| Nghiệp vụ | Responsible | Accountable | Consulted | Informed |
|---|---|---|---|---|
| Nhập dữ liệu ca | Nhân viên vận hành | Quản lý vận hành | Quản trị dữ liệu | Quản lý điều hành |
| Kiểm tra cảnh báo và đối soát | Quản lý vận hành | Quản lý vận hành | Nhân viên vận hành, Quản trị dữ liệu | Quản lý điều hành |
| Chốt ngày | Quản lý vận hành | Quản lý điều hành | Nhân viên vận hành | Người kiểm toán |
| Mở lại và sửa sau chốt | Quản lý vận hành | Quản lý điều hành | Quản trị dữ liệu | Người kiểm toán |
| Quản lý danh mục | Quản trị dữ liệu | Quản lý điều hành | Quản lý vận hành | Nhân viên vận hành |
| Thay đổi công thức KPI | Quản trị dữ liệu | Quản lý điều hành | Quản lý vận hành, mentor/Product approver | Các vai trò dùng dashboard |
| Xem audit log | Người kiểm toán / quản lý được cấp quyền | Quản lý điều hành | Quản trị hệ thống | Bên liên quan |

### 5.2. Ma trận quyền thao tác tối thiểu

RACI mô tả trách nhiệm nghiệp vụ, không thay thế phân quyền hệ thống. MVP cần ma trận quyền tối thiểu sau:

| Thao tác | Nhân viên vận hành | Quản lý vận hành | Quản lý điều hành | Quản trị dữ liệu | Quản trị hệ thống | Người kiểm toán |
|---|---|---|---|---|---|---|
| Nhập dữ liệu ca | Có, trong phạm vi tổ/ca | Có, trong phạm vi phụ trách | Có, nếu được cấp | Không mặc định | Không | Không |
| Upload file vận hành | Không mặc định | Có, trong phạm vi phụ trách | Có | Có, nếu được giao import | Không | Không |
| Xem staging | Giới hạn dữ liệu mình nhập/phụ trách | Có | Có | Có, theo phạm vi | Không mặc định | Có, chỉ đọc |
| Bỏ qua Warning | Không | Có, nếu được cấp | Có | Không mặc định | Không | Không |
| Chốt dữ liệu | Không | Có, trong phạm vi phụ trách | Có | Không | Không | Không |
| Tạo yêu cầu mở lại | Có, với dữ liệu mình phụ trách | Có | Có | Không mặc định | Không | Không |
| Phê duyệt mở lại | Không | Không mặc định | Có | Không | Không | Không |
| Sửa danh mục/định mức | Không | Không mặc định | Phê duyệt | Có | Không mặc định | Xem |
| Quản lý tài khoản/role | Không | Không | Không mặc định | Không | Có | Xem nếu được cấp |
| Xem dashboard | Tổ/ca/máy của mình | Phạm vi phụ trách | Phạm vi công ty/nhà máy | Theo quyền được cấp | Không mặc định | Chỉ đọc theo phạm vi |
| Xem audit log | Giới hạn bản ghi liên quan nếu được cấp | Giới hạn phạm vi | Có | Giới hạn phần danh mục/cấu hình | Có | Có |
| Xuất báo cáo | Không mặc định | Theo quyền | Theo quyền | Không mặc định | Không mặc định | Theo quyền |

---

## 6. Luồng Nghiệp Vụ Chính

PRD này tự chứa các scenario chính. Nếu có tài liệu mô tả chi tiết bên ngoài, tài liệu đó chỉ có giá trị tham khảo khi đã ghi rõ phiên bản, đường dẫn, ngày duyệt và người duyệt.

### 6.1. Nhập tay và chốt ngày bình thường

1. Nhân viên vận hành chọn ngày sản xuất, ca, tổ và máy trong phạm vi được phân quyền.
2. Nhân viên vận hành nhập trạng thái máy, sản lượng, số lượng đạt/lỗi, nguyên nhân dừng nếu có.
3. Hệ thống kiểm tra dữ liệu đầu vào.
4. Dữ liệu hợp lệ được lưu ở trạng thái tạm.
5. Cuối ca hoặc cuối ngày, Quản lý vận hành xem danh sách dữ liệu tạm.
6. Nếu không còn lỗi chặn chốt, Quản lý vận hành chốt dữ liệu theo phạm vi được giao.
7. Hệ thống chuyển dữ liệu sang trạng thái chính thức và cập nhật dashboard trong thời hạn quy định.
8. Dashboard hiển thị số liệu mới kèm thời điểm cập nhật cuối cùng.

### 6.2. Upload file có lỗi một phần

1. Người dùng có quyền upload file theo mẫu đã cấu hình.
2. Hệ thống đọc file và kiểm tra từng dòng.
3. Các dòng hợp lệ được đưa vào staging nếu không xung đột không xử lý được.
4. Các dòng lỗi bị giữ lại, không đưa vào staging.
5. Người dùng thấy tổng số dòng, số dòng hợp lệ, số dòng lỗi và lý do lỗi.
6. Người dùng có thể tải danh sách lỗi để sửa file và upload lại.
7. Khi upload lại file đã sửa, hệ thống không được nhập trùng 95 dòng đã xử lý trước đó. Dòng mới được đối chiếu bằng logical identity hoặc matching rule của từng loại dữ liệu, không chỉ bằng file hash.

Ví dụ nghiệm thu: file có 100 dòng, trong đó 95 dòng hợp lệ và 5 dòng thiếu mã máy. Hệ thống phải đưa 95 dòng vào staging, giữ 5 dòng lỗi ngoài staging, hiển thị từng dòng lỗi, cho phép tải file lỗi, và khi người dùng upload lại đủ 100 dòng thì không tạo trùng 95 dòng cũ.

### 6.3. Dữ liệu thiếu vào cuối ngày

1. Đến thời hạn chốt, hệ thống kiểm tra danh sách tổ, máy hoặc ca bắt buộc có dữ liệu.
2. Nếu thiếu dữ liệu, hệ thống hiển thị trạng thái thiếu theo từng tổ/máy/ca.
3. Người chịu trách nhiệm phải chọn một trong các trạng thái: chưa nhập, không sản xuất đã xác nhận, mất mạng/chờ nhập bù, đang xử lý import, hoặc cần quản lý xác nhận.
4. Dữ liệu thiếu không được tự động coi là 0.
5. "Chưa nhập", "đang xử lý import" và "mất mạng/chờ nhập bù" không được coi là dữ liệu đầy đủ.
6. Quản lý vận hành chỉ được đóng kỳ ngoại lệ nếu chính sách cho phép; dashboard phải hiển thị trạng thái thiếu dữ liệu và tỷ lệ bao phủ.

### 6.4. Chốt khi có cảnh báo

1. Hệ thống phân loại kiểm tra thành Error, Warning và Information.
2. Error chặn chốt cho đến khi dữ liệu được sửa.
3. Warning cho phép chốt nếu người có quyền nhập lý do và xác nhận.
4. Information chỉ hiển thị để tham khảo, không yêu cầu xác nhận.
5. Mọi thao tác bỏ qua Warning phải ghi audit log.

### 6.5. Phát hiện sai sau khi chốt

1. Người dùng phát hiện dữ liệu chính thức sai.
2. Người có quyền tạo yêu cầu mở lại, nhập lý do và phạm vi ảnh hưởng.
3. Quản lý có thẩm quyền phê duyệt mở lại.
4. Dữ liệu được sửa trong quy trình có kiểm soát, giữ lại phiên bản cũ.
5. Sau khi chốt lại, hệ thống cập nhật lại KPI và đánh dấu báo cáo đã thay đổi.
6. Mọi báo cáo hiển thị phiên bản dữ liệu, thời điểm cập nhật cuối và nhãn "đã điều chỉnh sau chốt" nếu kỳ báo cáo đã thay đổi. Nếu capability export đã được triển khai, file export cũ được đánh dấu outdated khi người dùng xem lại lịch sử export.

### 6.6. Dữ liệu đến muộn sau thời hạn chốt

1. Dữ liệu đến muộn được nhập hoặc import với nhãn "late data".
2. Nếu ngày sản xuất chưa chốt, dữ liệu xử lý như dữ liệu tạm bình thường.
3. Nếu ngày sản xuất đã chốt, dữ liệu phải đi qua quy trình sửa sau chốt.
4. Dashboard phải thể hiện rõ kỳ báo cáo đã được điều chỉnh sau chốt nếu dữ liệu muộn làm thay đổi KPI.

### 6.7. Nhiều nguồn cung cấp cùng một dữ liệu

1. Hệ thống nhận dữ liệu từ form web, file Excel hoặc OCR phiếu giấy.
2. Nếu dữ liệu có cùng logical identity hoặc matching rule nhưng giá trị khác nhau, hệ thống đánh dấu xung đột.
3. Hệ thống không tự ghi đè dữ liệu chính thức nếu chưa có quy tắc hoặc người có thẩm quyền xác nhận.
4. Người xử lý thấy từng giá trị, nguồn, thời điểm nhận, người nhập và thay đổi so với giá trị hiện có.
5. Quyết định giữ giá trị nào phải được lưu audit log.

### 6.8. Mất mạng hoặc gián đoạn vận hành

1. Nếu khu vực sản xuất mất mạng, Nhân viên vận hành tiếp tục ghi nhận bằng mẫu giấy hoặc file offline theo quy trình công ty.
2. Khi hệ thống hoạt động lại, dữ liệu được nhập bù và đánh dấu "late/offline".
3. Dữ liệu nhập bù được gắn với ngày sản xuất thực tế, không phải ngày nhập bù.
4. Nếu kỳ đã chốt, dữ liệu nhập bù đi qua quy trình sửa sau chốt.
5. Dashboard phải hiển thị khoảng thời gian có dữ liệu thiếu hoặc nhập bù để người xem không hiểu nhầm.

### 6.9. Phân loại và so sánh dữ liệu hiện tại với dữ liệu lịch sử

1. Mỗi record nguồn được gắn ngày sản xuất, thời điểm hệ thống nhận, nguồn và `dataTimeClass`.
2. Hệ thống phân loại thành `Current`, `Historical Backfill`, `Late Arrival` hoặc `Correction`; không suy ra dữ liệu cũ chỉ từ ngày upload file.
3. Với sản lượng theo ngày/ca, hệ thống tạo `businessDataCode` ổn định từ company, ngày sản xuất, ca, máy, sản phẩm và loại record. Dữ liệu dạng sự kiện vẫn ưu tiên stable/external ID theo mục 8.3.
4. Hệ thống tìm bản ghi trùng chính xác theo identity trước, sau đó mới tìm ứng viên gần giống trong cùng phạm vi nghiệp vụ.
5. Ứng viên có độ tương đồng từ 90% đến dưới 100% được cảnh báo để người có quyền review; cùng identity và cùng giá trị được liên kết provenance, không tạo bản ghi mới.
6. Nếu cùng identity nhưng khác giá trị, bản ghi luôn vào `Needs Review` dù điểm tương đồng cao hay thấp.
7. AI chỉ đưa ra điểm, trường giống/khác và lý do cảnh báo; không tự gộp, xóa, ghi đè hoặc quyết định nguồn thắng.

Ví dụ nguyên nhân–kết quả: file ngày 01/08 chứa dòng của ngày sản xuất 15/06 thì dòng đó là `Historical Backfill`, không phải dữ liệu hiện tại chỉ vì vừa được upload. Nếu dòng này giống 96% một record đã có, hệ thống tạo cảnh báo; người review mới quyết định liên kết, sửa hay giữ thành conflict.

### 6.10. Đối soát Excel, dữ liệu web và báo cáo giấy OCR

1. Người có quyền lấy file Excel từ máy quản lý và upload theo template đã cấu hình.
2. Hệ thống đưa Excel vào source batch, normalize và so sánh với dữ liệu người dùng đã nhập trên web theo `businessDataCode`/logical identity.
3. Khi capability OCR được bật, báo cáo giấy của Nhân viên vận hành được OCR thành bằng chứng thứ yếu và gắn confidence theo từng trường.
4. Màn hình đối soát hiển thị `Matched`, `Value Mismatch`, `Missing in Excel`, `Missing on Web`, `OCR Mismatch` và `OCR Low Confidence`.
5. Excel và dữ liệu web đều phải qua staging/chốt; nhãn “nguồn chính” không cho phép một nguồn tự ghi đè nguồn kia.
6. OCR không được tạo official record trực tiếp. Người có quyền phải xác nhận trường OCR trước khi dùng nó làm bằng chứng xử lý conflict.
7. Quyết định đối soát lưu actor, thời điểm, giá trị trước/sau, lý do, nguồn và model/template version vào audit log.

---

## 7. Trạng Thái Dữ Liệu

PRD chỉ mô tả trạng thái nghiệp vụ ở mức người dùng cần hiểu. TDD có thể tách chi tiết hơn thành state machine kỹ thuật.

### 7.1. Trạng thái nguồn/import

| Trạng thái | Ý nghĩa | Tác động nghiệp vụ |
|---|---|---|
| Received | Hệ thống đã nhận file/dữ liệu nguồn | Chưa tạo dữ liệu tạm |
| Processing | Đang đọc, mapping hoặc kiểm tra dữ liệu nguồn | Có thể chặn chốt nếu dữ liệu này thuộc phạm vi bắt buộc |
| Partially Processed | Một phần dòng hợp lệ, một phần lỗi | Dòng hợp lệ vào staging; dòng lỗi cần sửa |
| Failed | Xử lý thất bại toàn bộ | Không tạo dữ liệu tạm; cần upload/xử lý lại |

### 7.2. Trạng thái dữ liệu tạm

| Trạng thái | Ý nghĩa | Có lên dashboard chính thức không? |
|---|---|---|
| Draft | Người dùng đang nhập, chưa gửi | Không |
| Staging | Dữ liệu tạm, đã gửi nhưng chưa chốt | Không |
| Needs Review | Có Warning hoặc xung đột cần người có quyền xử lý | Không |
| Rejected | Có Error hoặc không đạt kiểm tra bắt buộc | Không |
| Ready to Close | Đủ điều kiện để chốt trong phạm vi liên quan | Chưa, cho đến khi chốt |

### 7.3. Trạng thái chốt kỳ/phạm vi

| Trạng thái | Ý nghĩa | Dashboard cấp tương ứng |
|---|---|---|
| Open | Còn trong thời gian nhập/chỉnh dữ liệu tạm | Chưa có KPI official mới |
| Pending Review | Còn thiếu dữ liệu, Warning, xung đột hoặc import đang xử lý | Hiển thị cảnh báo chưa sẵn sàng chốt |
| Closed Complete | Tất cả phạm vi con bắt buộc đã đủ điều kiện và đã chốt | KPI được gắn nhãn Official - Complete |
| Closed with Exceptions | Có dữ liệu thiếu/chờ nhập bù được quản lý chấp nhận theo chính sách | KPI nếu hiển thị phải gắn nhãn thiếu dữ liệu và tỷ lệ bao phủ |
| Partially Closed | Một phần tổ/dây chuyền đã chốt, phần khác chưa chốt | KPI cấp trên chỉ hiển thị dạng Partial hoặc giữ phiên bản cũ theo quy tắc mục 8.1 |
| Reopened | Phạm vi đã chốt được mở lại để sửa | Báo cáo giữ phiên bản official gần nhất và gắn nhãn đang điều chỉnh |
| Adjusted | Đã chốt lại sau sửa | Báo cáo gắn nhãn đã điều chỉnh sau chốt |

### 7.4. Trạng thái dữ liệu chính thức

| Trạng thái | Ý nghĩa | Có dùng cho KPI hiện hành không? |
|---|---|---|
| Official Current | Phiên bản official hiện hành | Có |
| Official Superseded | Phiên bản cũ sau khi sửa/chốt lại | Không, nhưng phải tra cứu được |
| Official Under Correction | Phiên bản đang trong quy trình sửa | Không dùng làm kết quả mới cho đến khi chốt lại |

Khi một kỳ được mở lại, phiên bản official gần nhất vẫn tiếp tục phục vụ dashboard và được gắn nhãn "đang điều chỉnh". Các thay đổi trong correction workspace không ảnh hưởng KPI cho đến khi được chốt lại.

---

## 8. Quy Tắc Nghiệp Vụ Cốt Lõi

### 8.1. Quy tắc chốt dữ liệu

| Chủ đề | Quy tắc |
|---|---|
| Phạm vi chốt | MVP cho phép chốt theo ngày sản xuất và phạm vi quản lý: tổ, dây chuyền hoặc nhà máy. |
| Điều kiện đủ dữ liệu | Mỗi máy/ca bắt buộc phải có dữ liệu sản xuất/dừng máy hợp lệ hoặc trạng thái "không sản xuất đã xác nhận". |
| Error | Không được chốt nếu còn Error. |
| Warning | Được chốt nếu người có quyền nhập lý do bỏ qua. |
| Chốt một phần | Được phép chốt từng tổ/dây chuyền nếu phạm vi đó đủ điều kiện; KPI cấp trên phải thể hiện rõ trạng thái Partial, không được trình bày như kỳ hoàn chỉnh. |
| Chốt thiếu dữ liệu | "Chưa nhập", "đang xử lý import" và "mất mạng/chờ nhập bù" không được coi là đủ dữ liệu. Chỉ được đóng kỳ với ngoại lệ nếu quản lý có thẩm quyền chấp nhận, và dashboard phải gắn nhãn thiếu dữ liệu. |
| Dữ liệu thiếu | Không được coi dữ liệu thiếu là 0 nếu chưa có xác nhận "không sản xuất". |
| Dữ liệu đến muộn | Nếu kỳ đã chốt, phải xử lý qua quy trình sửa sau chốt. |
| Trách nhiệm | Người chốt chịu trách nhiệm nghiệp vụ với dữ liệu trong phạm vi chốt; hệ thống ghi nhận người chốt và thời điểm chốt. |

| Trạng thái vận hành máy/ca | Được chốt hoàn chỉnh? | Có tính KPI? | Ghi chú |
|---|---|---|---|
| Có sản xuất | Có | Có | Dùng dữ liệu sản lượng, chất lượng, thời gian chạy/dừng. |
| Không sản xuất đã xác nhận | Có | Theo KPI Dictionary | Ví dụ máy không có kế hoạch sản xuất có thể bị loại khỏi mẫu số. |
| Dừng máy có lý do | Có | Có | Tính theo quy tắc downtime/KPI đã duyệt. |
| Chưa nhập có lý do | Không | Không | Chỉ cho phép đóng kỳ ngoại lệ nếu được phê duyệt. |
| Chờ xử lý import | Không | Không | Có thể chặn chốt hoặc đóng kỳ ngoại lệ theo chính sách. |
| Mất mạng/chờ nhập bù | Không | Không | Dữ liệu nhập bù gắn với ngày sản xuất thực tế và đi qua quy trình late data. |

Quy tắc KPI cấp trên khi chốt một phần:

- KPI cấp trên chỉ được gắn nhãn `Official - Complete` khi toàn bộ phạm vi con bắt buộc đã chốt hoàn chỉnh.
- Nếu mới chốt một phần, dashboard cấp trên hiển thị `Partial`, tỷ lệ phạm vi đã chốt và danh sách phạm vi còn thiếu.
- Không so sánh KPI `Partial` với kỳ hoàn chỉnh nếu điều đó có thể gây hiểu nhầm.
- Nếu hệ thống giữ KPI cũ của cấp trên, phải hiển thị rõ `stale` hoặc "chưa cập nhật do kỳ hiện tại chưa chốt đủ".

### 8.2. Quy tắc kiểm tra dữ liệu

| Mức | Ví dụ | Hành vi hệ thống |
|---|---|---|
| Error | Thời gian kết thúc trước thời gian bắt đầu; mã máy không tồn tại; sản lượng âm; ngày sản xuất ngoài kỳ được phép | Chặn lưu hoặc chặn chốt |
| Warning | Sản lượng vượt kế hoạch bất thường; thời gian chạy gần vượt thời lượng ca; tỷ lệ lỗi vượt ngưỡng | Cho phép chốt nếu người có quyền nhập lý do |
| Information | Dữ liệu thấp hơn trung bình nhiều ngày; máy ít hoạt động hơn tuần trước | Hiển thị để tham khảo |

Ngưỡng Warning và Information phải do quản trị dữ liệu cấu hình theo công ty, nhà máy, dây chuyền, tổ hoặc loại máy nếu cần.

### 8.3. Identity và quy tắc nhận diện dữ liệu trùng

Mục này xác định cách nhận diện "cùng một dữ liệu" khi import lại, nhập tay trùng hoặc nhiều nguồn gửi cùng thông tin. Với dữ liệu dạng event, stable record ID, external source ID và natural matching rule không nhất thiết trở thành unique constraint; TDD phải chọn unique constraint hoặc logic dedup phù hợp cho từng loại dữ liệu.

| Loại dữ liệu | Logical identity / matching rule đề xuất | Ghi chú cần chốt thêm |
|---|---|---|
| Sản lượng theo ca | company + productionDate + shift + machine + product | Nếu một máy sản xuất nhiều sản phẩm trong cùng ca, product là bắt buộc. |
| Trạng thái máy theo khoảng thời gian | Stable record ID hoặc external source ID; thời gian chồng lấn dùng để phát hiện ứng viên trùng | Cần rule cho overlap/gap và sự kiện kéo dài qua hai ca. |
| Sự cố | Stable record ID hoặc external source ID; thời điểm bắt đầu và loại sự cố dùng để matching | Nếu một sự cố có nhiều cập nhật, dùng version hoặc event sequence. |
| Chất lượng/lỗi | company + productionDate + shift + machine + product + defectType | Cần phân biệt số lượng lỗi và tỷ lệ lỗi. |
| Kế hoạch sản xuất | company + productionDate + shift + machine + product | `planVersion` là phiên bản, không nằm trong identity. |

Các identity và matching rule cuối cùng là blocker cho import, chống trùng, conflict resolution và drill-down.

`businessDataCode` là mã hỗ trợ đối soát cho record tổng hợp theo ngày/ca, không thay thế identity của mọi loại dữ liệu. Mã tối thiểu gồm `companyId + productionDate + shiftId + machineId + productId + recordType`; version kế hoạch, thời điểm upload và file hash không được đưa vào mã này. Hệ thống cũng lưu:

- `dataTimeClass`: `current`, `historical_backfill`, `late_arrival` hoặc `correction`;
- `receivedAt` và `sourceOccurredAt` để giải thích vì sao record được phân loại;
- `sourceType`, `sourceBatchId`, template/model version và actor xác nhận;
- liên kết tới record ứng viên khi phát hiện trùng hoặc gần trùng.

### 8.4. Quy tắc ưu tiên và xử lý xung đột nguồn dữ liệu

| Tình huống | Quy tắc mặc định cho MVP |
|---|---|
| Dữ liệu form web và file Excel trùng logical identity/matching rule, cùng giá trị | Gộp thành một bản ghi logic, giữ đầy đủ nguồn gốc. |
| Dữ liệu form web và file Excel trùng logical identity/matching rule nhưng khác giá trị | Đưa vào Needs Review, không tự ghi đè; “Excel từ máy quản lý” không tự động đồng nghĩa với official. |
| OCR khớp dữ liệu web/Excel | Ghi nhận bằng chứng đối soát và confidence; không tự nâng record thành official. |
| OCR khác dữ liệu web/Excel | OCR là nguồn thứ yếu; tạo cảnh báo theo từng trường và yêu cầu người có quyền xác nhận. |
| Dữ liệu mới đến sau khi đã chốt | Đi qua quy trình sửa sau chốt. |
| Chỉ một số trường khác nhau | Hiển thị so sánh từng trường; người có quyền chọn giữ trường nào hoặc sửa thủ công. |
| Không có quy tắc precedence cấu hình | Không tự động chọn nguồn thắng. |
| Upload lại file đã sửa | Merge theo logical identity/matching rule từng dòng; không tạo trùng bản ghi đã vào staging trước đó. Nếu bản ghi cũ đã được sửa thủ công, dữ liệu upload lại phải vào Needs Review. |

#### 8.4.1. Cảnh báo tương đồng có hỗ trợ AI

Quy trình luôn chạy deterministic identity/dedup trước AI. AI hoặc mô hình similarity chỉ được dùng để tìm ứng viên mà identity không khớp hoàn toàn, ví dụ khác cách viết mã sản phẩm, sai một ký tự mã máy hoặc OCR đọc nhầm một trường.

| Điểm/điều kiện | Hành vi |
|---|---|
| Cùng identity và cùng giá trị chuẩn hóa | Liên kết provenance, không tạo duplicate. |
| Cùng identity nhưng khác giá trị | Tạo conflict bắt buộc; không phụ thuộc điểm AI. |
| Từ `0.90` đến dưới `1.00` | Cảnh báo “ứng viên gần trùng”, hiển thị điểm và trường tạo ra điểm để người dùng review. |
| Dưới `0.90` | Không cảnh báo AI mặc định; vẫn áp dụng validation và conflict rule deterministic. |

Ngưỡng `0.90` là baseline sản phẩm, phải cấu hình/version thay vì hardcode trong UI hoặc model. Thay ngưỡng cần audit và đo precision/recall trên fixture đã ẩn danh. Điểm `1.00` của mô hình không tự cho phép merge nếu identity hoặc giá trị canonical còn xung đột.

### 8.5. Quy tắc hiệu lực lịch sử

- Máy, tổ, dây chuyền, định mức, phân loại nguyên nhân dừng và phạm vi người dùng phải có ngày hiệu lực.
- Báo cáo lịch sử dùng thông tin có hiệu lực tại thời điểm phát sinh dữ liệu.
- Không được sửa danh mục theo cách làm thay đổi ý nghĩa báo cáo lịch sử mà không có quy trình điều chỉnh hồi tố.
- Máy đã có dữ liệu lịch sử không được xóa cứng; chỉ được ngừng sử dụng.
- Khi máy chuyển dây chuyền, dữ liệu trước ngày chuyển vẫn thuộc dây chuyền cũ.
- Khi đổi tên hoặc mã hiển thị, báo cáo lịch sử phải thể hiện được tên/mã có hiệu lực tại kỳ báo cáo hoặc có chú thích rõ.
- Các khoảng hiệu lực của cùng một máy/định mức/phân công không được chồng lấn nếu cùng loại quan hệ.
- Ca làm việc và lịch sản xuất thay đổi theo thời gian cũng phải có ngày hiệu lực.

### 8.6. Quy tắc về dữ liệu "không có"

Các trạng thái sau phải được phân biệt trên giao diện và báo cáo:

- Chưa nhập dữ liệu.
- Đang xử lý.
- Không có kế hoạch sản xuất.
- Đã xác nhận không sản xuất.
- Có dữ liệu nhưng chưa chốt.
- Đã chốt nhưng tổng hợp chưa cập nhật.
- Xử lý thất bại.

Không trạng thái nào trong nhóm trên được tự động hiển thị thành 0 nếu chưa có quy tắc KPI xác định rõ.

---

## 9. Yêu Cầu Chức Năng

Mức ưu tiên dùng MoSCoW cho MVP. Riêng `Guardrail` là yêu cầu kiểm soát bắt buộc khi capability tương ứng được triển khai sau này, nhưng không thuộc acceptance scope của MVP nếu capability đó chưa bật.

### 9.1. Thiết lập công ty và khả năng cấu hình

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-CFG-01 | Hệ thống cho phép thiết lập cấu hình công ty cho deployment MVP: nhà máy, múi giờ, ngày sản xuất, lịch ca và thuật ngữ hiển thị. | Must | Deployment MVP có thể cấu hình ngày sản xuất, ca ngày/ca đêm và tên gọi cấp tổ chức mà không sửa code lõi. |
| FR-CFG-02 | Hệ thống cho phép cấu hình mapping dữ liệu đầu vào theo từng công ty hoặc từng mẫu file. | Must | Cùng một trường sản lượng có thể map từ các tên cột khác nhau như `actual_output`, `SL thực tế`, `quantity_actual`. |
| FR-CFG-03 | Hệ thống cho phép cấu hình ngưỡng cảnh báo, trạng thái máy, nguyên nhân dừng và KPI được bật/tắt cho deployment hiện tại. | Must | Việc thay đổi bộ nguyên nhân dừng và ngưỡng cảnh báo của deployment hiện tại không yêu cầu sửa code lõi; cấu hình được lưu cùng `companyId` để chuẩn bị mở rộng sau MVP. |
| FR-CFG-04 | MVP chạy single-tenant cho một công ty, nhưng dữ liệu và cấu hình có `companyId` để chuẩn bị mở rộng. | Must | Không có màn hình quản trị nhiều tenant trong MVP; mọi dữ liệu vẫn gắn được với một company hiện hành. |
| FR-CFG-05 | Hệ thống cho phép cấu hình ngày sản xuất, lịch ca và ngày hiệu lực của ca. | Must | Ca đêm, ngày nghỉ và thay đổi giờ ca theo thời kỳ được biểu diễn bằng cấu hình đã duyệt. |

### 9.1A. Điều hướng và ranh giới module

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-NAV-01 | Navigation dùng đúng các nhãn chuẩn ở mục 4.4 và nhóm chức năng theo năng lực nghiệp vụ, không theo tên role. | Must | `Quản lý sản xuất`, `Quản lý vận hành`, `Quản lý nhân sự`, `Quản lý vật tư`, `Quản lý kho`, `Bảo trì & lập lịch` và `Dữ liệu & đối soát` không bị đổi tên tùy tiện giữa các role. |
| FR-NAV-02 | Không có tab hoặc role riêng `Ca trưởng/Tổ trưởng`; hai tên này là chức danh/phân công trong Quản lý nhân sự. | Must | Đổi một Nhân viên vận hành thành Ca trưởng theo khoảng hiệu lực không tạo loại tài khoản hoặc workspace mới. |
| FR-NAV-03 | Tab chỉ xuất hiện khi capability được bật và người dùng có action + data scope tương ứng. | Must | Người không có quyền kho không thấy tab Quản lý kho và gọi API trực tiếp vẫn bị từ chối; capability chưa bật không hiện placeholder như chức năng hoàn chỉnh. |

### 9.2. Quản lý danh mục

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-MDM-01 | Người có quyền được xem, thêm, sửa và ngừng sử dụng nhà máy, dây chuyền, tổ, máy, ca, nguyên nhân dừng và định mức. | Must | Không thể xóa cứng máy đã có dữ liệu lịch sử; chỉ được ngừng sử dụng. |
| FR-MDM-02 | Mọi thay đổi danh mục quan trọng phải có ngày hiệu lực. | Must | Máy chuyển từ dây chuyền A sang B ngày 01/07/2026 thì báo cáo tháng 06/2026 vẫn thuộc dây chuyền A. |
| FR-MDM-03 | Hệ thống hỗ trợ import danh mục ban đầu từ file mẫu. | Should | File danh mục lỗi một phần được xử lý giống quy tắc upload file có lỗi một phần. |
| FR-MDM-04 | Quản trị dữ liệu được cấu hình phạm vi phụ trách cho người dùng. | Must | Nhân viên vận hành tổ A không thể nhập hoặc xem dữ liệu tổ B nếu không được cấp quyền. |
| FR-MDM-05 | Hệ thống quản lý danh mục sản phẩm để phục vụ import, KPI và bộ lọc sản phẩm. | Must | Nếu công ty không phân tích theo sản phẩm, cấu hình có thể dùng một sản phẩm mặc định như "N/A"; không được có bộ lọc sản phẩm nếu danh mục sản phẩm chưa đáng tin cậy. |
| FR-MDM-06 | Hệ thống quản lý chức danh và phân công Ca trưởng/Tổ trưởng trong hồ sơ nhân sự có ngày hiệu lực. | Must | Một người đổi từ Nhân viên vận hành sang Ca trưởng ngày 01/08 không làm thay đổi người chịu trách nhiệm hiển thị cho dữ liệu tháng 07. |

### 9.3. Thu thập dữ liệu

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-ING-01 | Nhân viên vận hành nhập dữ liệu vận hành bằng form web. | Must | Form bắt buộc chọn ngày sản xuất, ca, tổ, máy và trạng thái sản xuất hợp lệ trong phạm vi được giao. |
| FR-ING-02 | Người có quyền upload file dữ liệu theo mẫu đã cấu hình. | Must | File có dòng lỗi một phần phải hiển thị số dòng thành công, số dòng lỗi, lý do lỗi và cho phép tải danh sách lỗi. |
| FR-ING-03 | Hệ thống ghi nhận nguồn gốc từng giá trị quan trọng: nhập tay, file import, OCR, nhập bù, sửa sau chốt. | Must | Khi drill-down một KPI, người xem thấy dữ liệu đến từ nguồn nào và ai xác nhận. |
| FR-ING-04 | OCR đọc phiếu giấy là capability đối soát thứ yếu sau lõi MVP. | Should | Nếu bật OCR, kết quả lưu theo từng trường cùng confidence/model/template version và không được tạo official record nếu chưa được người dùng có quyền xác nhận. |
| FR-ING-05 | Upload lại file đã sửa phải chống tạo trùng theo logical identity/matching rule từng dòng. | Must | Upload lại file đủ 100 dòng sau khi 95 dòng đã vào staging không tạo thêm 95 bản ghi trùng. |
| FR-ING-06 | Mỗi record nguồn được phân loại `Current`, `Historical Backfill`, `Late Arrival` hoặc `Correction` từ ngày nghiệp vụ, thời điểm nhận và trạng thái chốt. | Must | File vừa upload chứa dữ liệu ngày cũ được gắn `Historical Backfill`; dữ liệu đến sau close được gắn `Late Arrival` và đi qua correction. |

### 9.4. Kiểm tra, staging và xử lý xung đột

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-VAL-01 | Dữ liệu hợp lệ được lưu vào staging trước khi chốt. | Must | Dữ liệu staging có nhãn rõ "chưa chính thức" và không được tính vào dashboard chính thức. |
| FR-VAL-02 | Hệ thống phân loại kiểm tra thành Error, Warning và Information. | Must | Error chặn chốt; Warning yêu cầu người có quyền nhập lý do nếu muốn chốt. |
| FR-VAL-03 | Hệ thống phát hiện dữ liệu trùng hoặc xung đột từ nhiều nguồn. | Must | Cùng máy/ca có sản lượng 500 từ form và 510 từ file thì trạng thái chuyển Needs Review, không tự ghi đè. |
| FR-VAL-04 | Người xử lý được xem so sánh các giá trị xung đột theo từng trường. | Must | Màn hình review hiển thị nguồn, người nhập, thời điểm, giá trị hiện tại và giá trị đề xuất. |
| FR-VAL-05 | Hệ thống cảnh báo ứng viên gần trùng có điểm tương đồng từ 90% đến dưới 100%; AI chỉ hỗ trợ review. | Must | Cảnh báo hiển thị điểm, trường giống/khác và model/rule version; không có nhánh tự merge/xóa/ghi đè dựa riêng vào điểm AI. |
| FR-VAL-06 | Hệ thống đối soát Excel từ máy quản lý với dữ liệu web và, khi bật, OCR giấy thứ yếu. | Must | Mỗi item có trạng thái khớp/lệch/thiếu nguồn/OCR confidence thấp; resolve cần quyền, lý do và audit. OCR chưa bật không chặn đối soát Excel–web. |

### 9.5. Chốt, mở lại và sửa sau chốt

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-CLO-01 | Quản lý vận hành chốt dữ liệu theo ngày sản xuất và phạm vi được giao. | Must | Không chốt được nếu còn Error trong phạm vi chốt. |
| FR-CLO-02 | Hệ thống cho phép chốt một phần theo tổ hoặc dây chuyền khi phần đó đủ điều kiện. | Must | Dây chuyền A đủ dữ liệu có thể chốt dù dây chuyền B còn thiếu; dashboard cấp nhà máy hiển thị Partial, tỷ lệ bao phủ và phạm vi chưa chốt. |
| FR-CLO-03 | Sửa dữ liệu sau chốt là quy trình bắt buộc của MVP. | Must | Không được sửa trực tiếp dữ liệu official; phải mở lại, ghi lý do, sửa, chốt lại và giữ phiên bản cũ. |
| FR-CLO-04 | Sau khi chốt lại, KPI và báo cáo liên quan phải được cập nhật lại. | Must | Báo cáo kỳ bị ảnh hưởng có trạng thái "đã điều chỉnh sau chốt" và có thể xem lịch sử phiên bản. |
| FR-CLO-05 | Hệ thống hỗ trợ đóng kỳ ngoại lệ khi còn dữ liệu thiếu được phê duyệt. | Should | Kỳ đóng ngoại lệ không được hiển thị như Official - Complete; dashboard hiển thị lý do, tỷ lệ bao phủ và dữ liệu còn thiếu. |

MVP sử dụng quy trình mở lại một cấp phê duyệt: người có quyền tạo yêu cầu mở lại kèm lý do và phạm vi ảnh hưởng; Quản lý điều hành phê duyệt hoặc từ chối yêu cầu trước khi người dùng được sửa trong correction workspace.

Nếu FR-CLO-05 chưa được triển khai trong MVP, hệ thống không cho phép đóng kỳ khi còn dữ liệu thiếu/chờ import/chờ nhập bù; kỳ giữ trạng thái `Pending Review` cho đến khi đủ dữ liệu hoặc yêu cầu này được triển khai.

### 9.6. KPI và quy tắc tổng hợp

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-KPI-01 | KPI Dictionary là deliverable nghiệp vụ bắt buộc trước khi triển khai dashboard chính thức. | Must | Mỗi KPI có định nghĩa, công thức, nguồn dữ liệu, chiều tổng hợp, trọng số, quy tắc thiếu dữ liệu, làm tròn và ví dụ tính tay đã được phê duyệt. |
| FR-KPI-02 | MVP chỉ đo hiệu suất cấp tổ/ca/máy, không xếp hạng hiệu suất cá nhân. | Must | Dashboard không hiển thị bảng xếp hạng nhân viên cá nhân. |
| FR-KPI-03 | KPI theo tháng/kỳ phải có quy tắc tổng hợp rõ ràng. | Must | Tài liệu KPI nói rõ KPI tháng tính lại từ tổng dữ liệu tháng hay tổng hợp từ KPI ngày. |
| FR-KPI-04 | Ngưỡng màu xanh/vàng/đỏ phải cấu hình được theo công ty và loại KPI. | Should | Một KPI có thể đặt ngưỡng khác nhau cho từng loại máy nếu được cấu hình. |
| FR-KPI-05 | MVP không có giao diện nhập công thức tự do cho quản trị viên. | Must | Hệ thống chỉ hỗ trợ các loại công thức đã code sẵn hoặc cấu hình có kiểm soát trong TDD. |

### 9.7. Dashboard, bộ lọc, so sánh và drill-down

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-DASH-01 | Dashboard hiển thị KPI theo phạm vi quyền của người dùng. | Must | Người dùng sửa URL hoặc tham số API vẫn không xem được dữ liệu ngoài phạm vi. |
| FR-DASH-02 | Người dùng lọc theo nhà máy, dây chuyền, tổ, máy, ca, sản phẩm, nguyên nhân dừng và khoảng ngày trong phạm vi được phép. | Must | Khi chọn tháng 06/2026 và dây chuyền A, mọi KPI chỉ dùng dữ liệu chính thức của dây chuyền A từ 01/06 đến hết 30/06 theo ngày sản xuất. |
| FR-DASH-03 | Dashboard hỗ trợ so sánh kỳ hiện tại với kỳ liền trước cùng phạm vi và cùng bộ lọc. | Must | Khi xem tháng 06/2026 của dây chuyền A, người dùng thấy chênh lệch so với tháng 05/2026 nếu hai kỳ đều đủ dữ liệu để so sánh. |
| FR-DASH-04 | Dashboard cho phép drill-down theo luồng: nhà máy -> dây chuyền -> tổ -> máy -> ca/ngày -> bản ghi vận hành -> nguồn dữ liệu/audit summary. | Must | Từ KPI "Máy A hiệu suất thấp", người dùng xem được các ca làm giảm KPI, thời gian dừng, nguyên nhân dừng, người nhập và nguồn dữ liệu trong phạm vi quyền. Liên kết tới audit log đầy đủ chỉ hiển thị cho người có quyền FR-AUD-03. |
| FR-DASH-05 | Dashboard làm nổi bật máy/tổ dưới ngưỡng và top nguyên nhân làm mất hiệu suất. | Must | Người quản lý thấy danh sách ưu tiên xử lý thay vì chỉ thấy biểu đồ tổng quan. |
| FR-DASH-06 | Bộ lọc được giữ khi chuyển giữa các biểu đồ trong cùng phiên phân tích. | Should | Chọn dây chuyền A và tháng 06/2026 rồi chuyển tab biểu đồ vẫn giữ bộ lọc đó. |
| FR-DASH-07 | Dashboard hỗ trợ so sánh hai máy hoặc hai tổ trong cùng phạm vi quyền. | Should | Người dùng có thể so sánh tổ A và tổ B nếu có quyền xem cả hai tổ. |
| FR-DASH-08 | Dashboard cung cấp bộ biểu đồ cốt lõi đa dạng, dùng cùng official filter và data-status contract. | Must | Có tối thiểu 8 view: xu hướng sản lượng, kế hoạch–thực tế, thành phần KPI đã duyệt, phân bố trạng thái máy, Pareto downtime, Pareto lỗi chất lượng, heatmap ca/ngày và coverage/đối soát; mỗi view có empty/error/partial state và drill-down phù hợp. |
| FR-DASH-09 | Khi capability liên quan được bật, dashboard bổ sung biểu đồ vật tư, kho và bảo trì thay vì nhồi vào dashboard lõi. | Should | Biểu đồ tiêu hao/chênh lệch vật tư, tồn kho/luân chuyển và backlog/hoàn thành bảo trì chỉ xuất hiện khi module + dữ liệu + quyền tương ứng sẵn sàng. |

Catalog biểu đồ không được dùng nhiều kiểu chart để lặp lại cùng một số liệu mà không thêm giá trị phân tích. Không có biểu đồ xếp hạng hiệu suất cá nhân. Mọi chart phải nêu scope, kỳ, đơn vị, completeness/freshness và đường drill-down hoặc bảng dữ liệu tương đương.

### 9.8. Nhắc việc và dữ liệu trễ

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-NOTI-01 | Hệ thống nhắc người phụ trách khi gần hết ca nhưng chưa nhập đủ dữ liệu bắt buộc. | Should | Trước hạn cấu hình, Nhân viên vận hành thấy danh sách máy/ca còn thiếu. |
| FR-NOTI-02 | Hệ thống nhắc Quản lý vận hành khi đến hạn chốt nhưng còn dữ liệu thiếu, lỗi hoặc đang xử lý. | Must | Màn hình chốt hiển thị rõ tổ/máy/ca nào đang chặn chốt. |
| FR-NOTI-03A | Hệ thống thông báo khi import file xử lý xong hoặc thất bại. | Must | Người upload biết file đã thành công, lỗi một phần hoặc thất bại toàn bộ. |
| FR-NOTI-03B | Hệ thống thông báo khi OCR xử lý xong hoặc thất bại. | Guardrail | Chỉ bắt buộc khi capability OCR được bật; không thuộc MVP acceptance scope nếu OCR chưa được triển khai. |
| FR-NOTI-04 | Hệ thống escalate dữ liệu quá hạn cho cấp quản lý được cấu hình. | Should | Nếu quá hạn chốt 2 giờ, Quản lý điều hành nhận danh sách phạm vi chưa chốt. |

### 9.9. Phân quyền, quyền riêng tư và sử dụng dữ liệu

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-SEC-01 | Mọi người dùng phải đăng nhập bằng tài khoản cá nhân. | Must | Không truy cập được hệ thống khi chưa đăng nhập. |
| FR-SEC-02 | Phân quyền phải kiểm tra ở server-side cho mọi dữ liệu đọc/ghi/xuất. | Must | Ẩn nút trên UI không được xem là đủ bảo mật. |
| FR-SEC-03 | MVP không dùng dữ liệu hệ thống để tính lương, kỷ luật hoặc đánh giá cá nhân nếu chưa có chính sách riêng được phê duyệt. | Must | Báo cáo chỉ hiển thị cấp tổ/ca/máy; không có ranking cá nhân. |
| FR-SEC-04 | Nếu capability export được triển khai, xuất báo cáo chứa dữ liệu nhạy cảm phải theo quyền. | Guardrail | Người không có quyền xem dữ liệu chi tiết không thể xuất file chứa dữ liệu chi tiết. |
| FR-SEC-05 | Ba cấp quyền vận hành là Quản lý điều hành, Quản lý vận hành và Nhân viên vận hành; quyền quản trị/audit được gán độc lập theo action. | Must | Đổi chức danh Ca trưởng/Tổ trưởng không tự cấp quyền; server resolve role + action + data scope, không suy quyền từ nhãn tab hoặc chức danh. |

### 9.10. Audit log

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-AUD-01 | Hệ thống ghi audit log cho nhập, import, sửa, chốt, mở lại, chốt lại, bỏ qua Warning và xử lý xung đột. | Must | Log lưu người thực hiện, thời điểm, hành động, dữ liệu trước/sau, lý do nếu có. |
| FR-AUD-02 | Hệ thống ghi audit log cho sửa danh mục, sửa định mức, thay đổi role/phạm vi và đăng nhập thất bại/khóa tài khoản. | Must | Có thể tra cứu lịch sử thay đổi của một máy hoặc một tài khoản. |
| FR-AUD-03 | Chỉ người có quyền mới được xem hoặc xuất audit log. | Must | Nhân viên vận hành không xem được log toàn nhà máy nếu không được cấp quyền. |
| FR-AUD-04 | Thời hạn lưu audit log phải được cấu hình theo chính sách công ty. | Should | Mặc định đề xuất giữ tối thiểu 24 tháng hoặc theo quy định công ty. |
| FR-AUD-05 | Nếu OCR hoặc export được triển khai, các hành động xác nhận OCR và export báo cáo phải có audit log. | Guardrail | Không thuộc acceptance scope MVP nếu OCR/export chưa được bật. |

### 9.11. Voice và export

| Mã | Yêu cầu | Ưu tiên | Tiêu chí chấp nhận |
|---|---|---|---|
| FR-VOICE-01 | Điều khiển giọng nói chỉ là pilot sau MVP, không chặn sign-off MVP. | Could | Chỉ triển khai khi có business rationale như thao tác khi đeo găng, di chuyển hoặc dashboard quá nhiều bước lọc. |
| FR-VOICE-02 | Nếu capability voice được triển khai, lệnh giọng nói không được vượt quyền người dùng và phải tương đương bộ lọc thủ công. | Guardrail | Lệnh yêu cầu dữ liệu ngoài phạm vi bị từ chối hoặc tự giới hạn về phạm vi hợp lệ. |
| FR-EXP-01 | Xuất dashboard ra PDF/ảnh là tùy chọn sau MVP. | Could | File xuất giữ đúng bộ lọc, kỳ báo cáo, thời điểm xuất và trạng thái dữ liệu. |

---

## 10. KPI Dictionary Ban Đầu

KPI Dictionary là deliverable nghiệp vụ bắt buộc, được mentor/Product approver và Quản lý điều hành phê duyệt trước khi dashboard chính thức được nghiệm thu.

Trong MVP, hệ thống lưu phiên bản KPI đã duyệt và dùng các loại công thức được code sẵn hoặc cấu hình có kiểm soát. MVP không bao gồm giao diện để quản trị viên tự nhập công thức tự do hoặc một formula engine tổng quát cho mọi công ty.

Quy tắc mặc định khi KPI Dictionary thay đổi:

- Mỗi phiên bản KPI có ngày hiệu lực.
- Báo cáo dùng phiên bản KPI có hiệu lực tại kỳ báo cáo.
- Công thức KPI mới chỉ áp dụng từ ngày hiệu lực, không tự tính lại lịch sử.
- Nếu cần tính lại lịch sử, phải có phê duyệt hồi tố riêng; các báo cáo bị ảnh hưởng phải được đánh dấu adjusted.

| Trường | Ý nghĩa |
|---|---|
| Tên KPI | Tên hiển thị trên dashboard |
| Mục đích | KPI trả lời câu hỏi nghiệp vụ nào |
| Cấp áp dụng | Máy, tổ, dây chuyền, nhà máy |
| Công thức | Công thức tính chi tiết |
| Nguồn dữ liệu | Trạng thái máy, sản lượng, chất lượng, kế hoạch, định mức |
| Chiều thời gian | Ngày, tuần, tháng, năm, khoảng tùy chỉnh |
| Quy tắc tổng hợp | Tính lại từ dữ liệu gốc hay tổng hợp từ KPI con |
| Trọng số | Theo thời gian chạy, sản lượng, kế hoạch hoặc không trọng số |
| Quy tắc thiếu dữ liệu | Bỏ qua, chặn tính, hiển thị thiếu dữ liệu hoặc tính là 0 nếu đã xác nhận |
| Làm tròn | Số chữ số thập phân |
| Ngưỡng màu | Xanh, vàng, đỏ |
| Ví dụ tính tay | Một ví dụ đã được mentor/quản lý xác nhận |

Các câu hỏi KPI cần chốt:

- KPI tháng là trung bình KPI ngày hay tính lại từ tổng dữ liệu tháng?
- KPI nhiều máy bình quân đơn giản hay theo trọng số?
- Máy không có kế hoạch sản xuất có tham gia mẫu số không?
- Không có dữ liệu khác gì giá trị bằng 0?
- Nếu hai tổ cùng điểm thì xếp hạng thế nào?
- Dữ liệu chưa chốt hoặc lỗi có bị loại khỏi KPI không?
- Kế hoạch sản lượng thay đổi giữa tháng có áp dụng hồi tố không?

---

## 11. Mô Hình Dữ Liệu Khái Niệm

PRD chỉ mô tả dữ liệu ở mức nghiệp vụ. Thiết kế bảng, index, partitioning và API nằm trong TDD.

| Nhóm dữ liệu | Mô tả |
|---|---|
| Company configuration | Thông tin công ty, nhà máy, lịch ca, ngày sản xuất, thuật ngữ, ngưỡng cảnh báo, KPI bật/tắt |
| Master data | Nhà máy, dây chuyền, tổ, máy, định mức, nguyên nhân dừng, sản phẩm, vật tư, người dùng, chức danh/phân công có hiệu lực và phạm vi quyền |
| Source data | File Excel, ảnh/phiếu giấy, form nhập tay, dữ liệu nhập bù, `businessDataCode`, `dataTimeClass` và metadata nguồn/model/template |
| Staging data | Dữ liệu tạm, lỗi, cảnh báo, ứng viên gần trùng, xung đột, trạng thái review |
| Reconciliation | Lần đối soát, item theo logical identity, nguồn web/Excel/OCR, điểm tương đồng, confidence, trạng thái và quyết định xử lý |
| Official data | Dữ liệu đã chốt, phiên bản chính thức theo ngày sản xuất và phạm vi |
| KPI aggregate | Kết quả KPI đã tổng hợp theo kỳ, phạm vi, chiều phân tích và phiên bản |
| Audit log | Lịch sử hành động, dữ liệu trước/sau, lý do, người thực hiện |

---

## 12. Yêu Cầu Phi Chức Năng

| Nhóm | Yêu cầu |
|---|---|
| Hiệu năng dashboard | Với profile tải đã thống nhất, dashboard tổng quan phản hồi p95 dưới 3 giây. Profile tải tối thiểu cần nêu số máy, ca/ngày, bản ghi/máy/ca, số người dùng đồng thời và tần suất refresh. |
| Hiệu năng drill-down | Màn hình chi tiết truy ngược về ca/máy/bản ghi nguồn phản hồi p95 dưới 5 giây với bộ lọc hợp lý. |
| Độ mới dữ liệu | Sau khi chốt hoặc chốt lại, KPI bị ảnh hưởng phải cập nhật trong vòng 5 phút, hoặc dashboard phải hiển thị rõ trạng thái đang cập nhật. |
| Khả dụng | Mục tiêu pilot: >= 99% trong giờ vận hành đã thống nhất, không tính lịch bảo trì đã thông báo trước. |
| Toàn vẹn dữ liệu | Không sửa trực tiếp dữ liệu official; mọi thay đổi sau chốt phải có quy trình, lý do và audit log. |
| Bảo mật | Mật khẩu lưu bằng cơ chế hash an toàn; quyền kiểm tra server-side; không log secrets hoặc dữ liệu nhạy cảm không cần thiết. |
| Khả năng cấu hình | Với deployment nằm trong capability hiện có, onboarding qua cấu hình, mapping và import danh mục; mọi thay đổi module lõi phải được ghi nhận là capability gap và được phê duyệt. |
| Tương thích thiết bị | Form nhập liệu hỗ trợ tối thiểu viewport mobile 390px trở lên và tablet 768px trở lên; dashboard hỗ trợ desktop 1366px trở lên. Trình duyệt tối thiểu cần chốt trước UAT. |
| Sao lưu và khôi phục | Dữ liệu chính thức, cấu hình và audit log phải có kế hoạch sao lưu, khôi phục và kiểm thử khôi phục; RPO/RTO cần được chốt trước triển khai production. |
| Giám sát vận hành | Có theo dõi import, chốt, cập nhật KPI, lỗi xử lý và thời gian phản hồi dashboard. |
| Minh bạch AI/đối soát | Điểm tương đồng và OCR confidence phải kèm rule/model/template version, trường tạo ra cảnh báo và quyết định của người review; không log nội dung nhạy cảm ngoài nhu cầu điều tra. |

Profile tải tối thiểu cần chốt cho kiểm thử hiệu năng:

| Tham số | Giá trị đề xuất ban đầu | Trạng thái |
|---|---|---|
| Thời gian dữ liệu giả lập | 2 năm | Cần xác nhận |
| Số máy | 100 | Cần xác nhận |
| Số ca/ngày | 3 | Cần xác nhận |
| Bản ghi trạng thái/máy/ca | TBD | Blocker cho test hiệu năng |
| Sản lượng record/máy/ca | TBD | Blocker cho test hiệu năng |
| Dòng tối đa mỗi file import | TBD | Blocker cho import |
| Số import đồng thời | TBD | Cần xác nhận |
| Người dùng đồng thời | TBD | Cần xác nhận |
| Tần suất refresh dashboard | TBD | Cần xác nhận |
| Dữ liệu lịch sử cần migrate | TBD tháng/năm | Cần xác nhận |
| RPO | TBD | Cần xác nhận trước production |
| RTO | TBD | Cần xác nhận trước production |

---

## 13. Tiêu Chí Thành Công

| Chỉ số | Mục tiêu đề xuất |
|---|---|
| Tỷ lệ dữ liệu bắt buộc được nhập đúng hạn | >= 95% trong giai đoạn pilot sau khi người dùng được hướng dẫn |
| Tỷ lệ ngày/phạm vi được chốt đúng hạn | >= 95% |
| Tỷ lệ dữ liệu phải sửa sau chốt | <= 5% bản ghi chính thức trong tháng pilot |
| Thời gian xem dashboard chính | p95 < 3 giây với bộ dữ liệu kiểm thử đã thống nhất |
| Khả năng truy nguyên KPI | 100% KPI thuộc MVP phải drill-down được về dữ liệu nguồn; ngoại lệ phải được phê duyệt trước trong KPI Dictionary |
| Chất lượng cảnh báo gần trùng | Precision/recall được đo trên fixture đã ẩn danh và đạt ngưỡng được duyệt trước khi dùng để hỗ trợ xử lý dữ liệu thật; không lấy riêng số lượng cảnh báo làm thước đo thành công |
| Tỷ lệ đối soát có kết luận | 100% item `Value Mismatch`, `Missing` hoặc `OCR Low Confidence` phải có người xử lý/lý do trước khi dữ liệu liên quan được chốt |
| Mức hài lòng người dùng thử nghiệm | >= 4/5 với nhóm Quản lý vận hành và Nhân viên vận hành tham gia pilot |
| Khả năng cấu hình khi triển khai công ty mới | Với một công ty có mô hình nghiệp vụ nằm trong capability hiện có, thiết lập cấu hình, mapping và import danh mục mà không thay đổi module lõi; mọi thay đổi code phải được ghi nhận là capability gap |

---

## 14. Open Questions Và Quản Trị Quyết Định

Open Questions không chỉ là ghi chú. Mỗi mục phải có owner, hạn chốt, trạng thái, mức độ blocker và quyết định cuối cùng.

| Mã | Câu hỏi | Owner | Due date | Blocker | Trạng thái | Quyết định |
|---|---|---|---|---|---|---|
| TBD-01 | Công thức KPI máy/tổ chính thức là gì? Có dùng OEE đầy đủ hay công thức đơn giản hơn? | Mentor / Quản lý điều hành | Trước khi làm KPI engine | Có | Open | Chỉ implement một KPI đã được duyệt |
| TBD-02 | MVP chỉ triển khai một nhà máy hay nhiều nhà máy trong cùng công ty? | Mentor | Trước khi chốt scope dashboard | Có | Open | Đề xuất: MVP một công ty, hỗ trợ cấu hình nhiều nhà máy |
| TBD-03 | MVP single-tenant hay nhiều công ty dùng chung một instance? | Mentor / IT công ty | 10/07/2026 | Không | Decided | MVP single-tenant; multi-tenant vận hành ngoài phạm vi MVP |
| TBD-04 | Danh sách dữ liệu bắt buộc để được chốt gồm những gì? | Quản lý vận hành | Trước closure workflow | Có | Open | Chưa chốt |
| TBD-05 | Ca 22:00-06:00 thuộc ngày bắt đầu hay ngày kết thúc? Tuần báo cáo bắt đầu thứ mấy? | Quản lý vận hành | Trước schema ca/ngày sản xuất | Có | Open | Không code theo giả định chưa duyệt |
| TBD-06 | Ca có được chồng lấn không, ngày nghỉ/ca đặc biệt xử lý thế nào, người dùng được nhập lùi bao lâu? | Quản lý vận hành | Trước schema ca/ngày sản xuất | Có | Open | Chưa chốt |
| TBD-07 | Logical identity và matching rule cuối cùng cho sản lượng, trạng thái máy, sự cố, chất lượng và kế hoạch là gì? | Quản trị dữ liệu | Trước module import/staging | Có | Open | Chưa chốt |
| TBD-08 | Mẫu file import thực tế và quy tắc mapping từng cột là gì? | Quản trị dữ liệu | Trước module import | Có | Open | Mentor cung cấp một file mẫu thật |
| TBD-09 | Khi upload lại file đã sửa, chính sách là merge theo logical identity/matching rule, chỉ nhập dòng lỗi hay thay thế batch cũ? | Quản trị dữ liệu | Trước module import | Có | Open | Đề xuất: merge theo logical identity/matching rule |
| TBD-10 | Quy trình phê duyệt sửa sau chốt gồm mấy bước và ai có quyền duyệt? | Quản lý điều hành | 11/07/2026 | Không | Decided | MVP dùng một bước phê duyệt; Quản lý điều hành phê duyệt hoặc từ chối yêu cầu mở lại |
| TBD-11 | Ngưỡng cảnh báo Error/Warning/Information ban đầu là gì? | Quản lý vận hành | Trước validation rules | Có | Open | Chưa chốt |
| TBD-12 | Quy định dữ liệu đến muộn sau chốt áp dụng trong bao lâu? | Quản lý điều hành | Trước late-data workflow | Có | Open | Chưa chốt |
| TBD-13 | Chính sách lưu audit log và báo cáo xuất file là bao lâu? | Mentor / IT công ty | Trước production sign-off | Không | Open | Chưa chốt |
| TBD-14 | Voice có pain point thật và người dùng pilot không? | Mentor / Người dùng thử nghiệm | Sau MVP | Không cho MVP | Open | Đề xuất: sau MVP |
| TBD-15 | Dữ liệu hiệu suất có được dùng cho đánh giá cá nhân, kỷ luật hoặc lương thưởng không? | Quản lý điều hành / HR nếu có | 10/07/2026 | Không | Decided | Không trong MVP; hệ thống không xếp hạng cá nhân và không dùng cho lương/kỷ luật |
| TBD-16 | Có import dữ liệu lịch sử từ Excel cũ không, backfill bao nhiêu tháng/năm và cửa sổ nào phân biệt `Current` với `Historical Backfill`? | Mentor / Quản trị dữ liệu | Trước migration/import lịch sử | Có nếu migrate lịch sử | Open | Bốn lớp thời gian và quy trình so sánh đã chốt; horizon/file cụ thể còn mở |
| TBD-17 | Nhắc việc/chốt trễ trong MVP chỉ dùng thông báo trong hệ thống hay cần email/Zalo/Teams? | Mentor / IT công ty | Trước notification workflow | Không | Open | Đề xuất: in-app cho MVP |
| TBD-18 | Khi KPI Dictionary đổi phiên bản, có tính lại lịch sử không? Ai có quyền phê duyệt hồi tố? | Quản lý điều hành / mentor | Trước KPI versioning nâng cao | Có nếu muốn hồi tố | Open | Mặc định: không hồi tố nếu chưa được phê duyệt |
| TBD-19 | Trạng thái máy theo khoảng thời gian có được chồng lấn hoặc có khoảng trống không? Máy có thể vừa running vừa có incident không? | Quản lý vận hành | Trước machine-state model | Có | Open | Chưa chốt |
| TBD-20 | Một trạng thái máy hoặc sự cố kéo dài qua nhiều ca được chia thành nhiều record hay giữ một record? | Quản lý vận hành | Trước machine-state/incident model | Có | Open | Chưa chốt |
| TBD-21 | Ngưỡng thời gian cụ thể để chuyển dữ liệu thô (`official_records`, `source_records`, file gốc) từ lưu trữ "nóng" sang "lạnh" là bao lâu (`hotRetentionMonths`/`rawRetentionMonths` ở TDD mục 12.4)? Có khác nhau theo loại dữ liệu không? | Mentor / IT công ty | Không chặn giai đoạn đầu; nên chốt trước khi dữ liệu thô đủ lớn để ảnh hưởng chi phí lưu trữ | Không | Open | Chưa chốt — không cần chặn phát triển sớm, nhưng nên rà lại định kỳ vì dự án dài hạn (xem PRD mục 15, TDD ADR-12) |
| TBD-22 | Mẫu phiếu giấy nào được OCR, ngưỡng confidence từng trường và bộ fixture ẩn danh nào dùng để nghiệm thu? | Quản trị dữ liệu / Quản lý vận hành | Trước khi bật capability OCR | Có nếu bật OCR | Open | OCR vẫn là nguồn đối soát thứ yếu; thiếu quyết định này không chặn Excel–web |

Quy tắc:

- Blocker = Có: không bắt đầu implement phần liên quan nếu chưa có quyết định hoặc giả định tạm thời được phê duyệt.
- Blocker = Không: có thể implement với giả định đã ghi rõ và được mentor chấp nhận.
- Không dùng mặc định kỹ thuật hoặc giả định của đội phát triển để thay thế quyết định nghiệp vụ lõi.

---

## 15. Rủi Ro Và Giả Định

### 15.1. Rủi ro

- Công thức KPI chưa chốt có thể làm thay đổi data model, dashboard và test case.
- Nếu quy tắc chốt thiếu dữ liệu không rõ, mỗi quản lý có thể chốt theo cách khác nhau.
- Nếu danh mục không có hiệu lực theo thời gian, báo cáo lịch sử có thể tự thay đổi khi sửa danh mục hiện tại.
- Nếu không có drill-down, dashboard chỉ phát hiện vấn đề nhưng không giúp điều tra nguyên nhân.
- Dù timeline không còn bị ép theo kỳ thực tập, phạm vi `Must` hiện tại (khoảng 40 yêu cầu, cộng hạ tầng kỹ thuật như outbox, hierarchical locking, KPI engine có versioning) vẫn là khối lượng lớn cho 3 người; nếu không có checkpoint định kỳ với mentor, dự án có thể trôi mà không có mốc nào thực sự "xong" để đánh giá tiến độ — nên dùng đúng cấu trúc 3 lớp ở mục 16 (demo slice → MVP → stretch) làm các mốc kiểm tra định kỳ, không chỉ là nhãn phân loại.
- Với đội 3 người làm liên tục nhiều tháng/năm, rủi ro thất thoát ngữ cảnh khi một thành viên tạm nghỉ hoặc rời nhóm cao hơn dự án ngắn hạn — cần giữ PRD/TDD/Open Questions luôn cập nhật làm nguồn sự thật chung, thay vì để quyết định chỉ nằm trong trao đổi miệng hoặc chat riêng lẻ.
- Mỗi công ty có quy trình và file dữ liệu khác nhau; mục tiêu "cắm được nhiều công ty" cần adapter và cấu hình tốt, không thể cam kết dùng ngay cho mọi công ty mà không khảo sát.
- Nếu cố làm multi-tenant vận hành ngay trong giai đoạn đầu, phạm vi bảo mật và vận hành sẽ tăng mạnh so với năng lực đội 3 người tại thời điểm đó; nên chỉ làm khi có nhu cầu thật (xem TDD mục 4, ADR-08).

### 15.2. Giả định

- MVP không xếp hạng hiệu suất cá nhân.
- Công ty cung cấp danh mục ban đầu và mẫu file thực tế trước khi nghiệm thu import.
- Người dùng có thiết bị phù hợp để nhập liệu hoặc có quy trình nhập bù khi mất mạng.
- Mentor hoặc Quản lý điều hành là người phê duyệt KPI Dictionary.
- TDD sẽ chốt thiết kế kỹ thuật sau khi PRD được duyệt về nghiệp vụ.
- MVP triển khai single-tenant cho một công ty; các `companyId`/cấu hình theo công ty là chuẩn bị mở rộng, không phải cam kết vận hành nhiều tenant.
- Dự án có 3 người tham gia xuyên suốt và mentor tiếp tục đồng hành sau kỳ thực tập; nếu số người hoặc vai trò phê duyệt thay đổi, ma trận RACI (mục 5.1) và danh sách owner trong Open Questions (mục 14) cần được rà soát lại.

---

## 16. Lộ Trình Đề Xuất

Dự án phát triển dài hạn, liên tục sau kỳ thực tập, nên kế hoạch triển khai không bị ép theo một deadline cố định — thay vào đó, chia thành ba lớp ưu tiên để nhóm 3 người luôn biết đang làm tới đâu và có thể dừng lại ở một trạng thái "dùng được" tại bất kỳ thời điểm nào, kể cả khi kỳ thực tập kết thúc. Tên lớp dưới đây không thay đổi MoSCoW: toàn bộ `Must` vẫn thuộc acceptance scope của MVP nếu không được mentor đổi scope.

| Lớp | Nội dung |
|---|---|
| Demo slice tối thiểu | Đăng nhập và phân quyền; danh mục cơ bản; nhập tay; staging và validation; chốt hoàn chỉnh; một KPI đã chốt công thức; dashboard và drill-down cơ bản; audit log. |
| MVP acceptance scope | Toàn bộ yêu cầu `Must`, bao gồm ba cấp quyền vận hành, navigation chuẩn, import/đối soát Excel–web, phân loại lịch sử, cảnh báo gần trùng, chốt/correction, catalog biểu đồ cốt lõi và các guardrail áp dụng cho capability đã bật. |
| Stretch scope | Các yêu cầu `Should`/`Could`: đóng kỳ ngoại lệ, nhắc chủ động/escalation, OCR giấy, biểu đồ module mở rộng, quản lý kho, bảo trì & lập lịch, voice, export và so sánh nâng cao. |

| Giai đoạn | Nội dung chính | Kết quả |
|---|---|---|
| Giai đoạn 1 - Nền tảng nghiệp vụ | Ba cấp quyền vận hành, navigation chuẩn, danh mục, cấu hình công ty, ngày sản xuất, ca, nhập tay | Có thể nhập dữ liệu đúng phạm vi |
| Giai đoạn 2 - Kiểm soát dữ liệu | Staging, validation, conflict review, chốt, chốt một phần, sửa sau chốt, audit | Có dữ liệu official đáng tin cậy |
| Giai đoạn 3 - KPI và dashboard | KPI Dictionary, catalog biểu đồ cốt lõi, bộ lọc, so sánh, drill-down và trạng thái dữ liệu thiếu | Quản lý xem và điều tra KPI bằng nhiều góc nhìn |
| Giai đoạn 4 - Import và đối soát | Import Excel, phân loại dữ liệu lịch sử, cảnh báo gần trùng, đối soát Excel–web, dữ liệu trễ và thông báo trạng thái | Giảm dữ liệu trùng/lệch và tăng khả năng truy nguyên |
| Giai đoạn 5 - Mở rộng sau MVP | OCR giấy, kho, bảo trì & lập lịch, biểu đồ module, voice, ERP/MES và phân tích nâng cao | Mở rộng khi lõi MVP đã ổn định |

### 16.1. Nhịp độ triển khai cho đội 3 người, dài hạn

Vì không có deadline cố định, cần một cơ chế khác để biết dự án có đang tiến triển đúng hướng hay không:

- Trong mỗi giai đoạn, ưu tiên hoàn thành trọn vẹn một lát cắt dọc (một luồng nghiệp vụ chạy được từ đầu đến cuối, ví dụ toàn bộ mục 6.1) trước khi mở rộng sang luồng khác — tránh tình trạng nhiều phần cùng dở dang song song.
- Định kỳ (ví dụ mỗi 2-4 tuần) đối chiếu lại với mentor: phần nào đã xong theo đúng tiêu chí chấp nhận ở mục 9, phần nào cần điều chỉnh. Đây thay thế cho áp lực deadline của kỳ thực tập.
- Mỗi khi một Open Question (mục 14) được quyết định, cập nhật ngay trạng thái trong bảng thay vì để tồn đọng — với timeline dài hạn, số lượng quyết định sẽ tăng dần và dễ thất lạc nếu không cập nhật liên tục.
- Vì chỉ có 3 người, nên phân công theo module rõ ràng (ví dụ: 1 người phụ trách Ingestion/Validation, 1 người phụ trách Closing/KPI Engine, 1 người phụ trách Dashboard/Frontend — xem TDD mục 5) để giảm xung đột code và trách nhiệm không rõ ràng, thay vì cả 3 người cùng sửa một module.

---

## 17. Câu Hỏi Cần Bạn/Mentor Trả Lời Trước Khi Sign-Off

Mục 14 là nguồn sự thật cho câu hỏi, owner, trạng thái và quyết định. Trước sign-off, cần xử lý hoặc chấp nhận giả định tạm thời cho các mã sau:

| Mức | Mã TBD | Ghi chú |
|---|---|---|
| Blocker nghiệp vụ | TBD-01, TBD-02, TBD-04, TBD-05, TBD-06, TBD-07, TBD-08, TBD-09, TBD-11, TBD-12, TBD-19, TBD-20 | Không nên bắt đầu phần liên quan nếu chưa có quyết định hoặc giả định được phê duyệt. |
| Blocker có điều kiện | TBD-16, TBD-18, TBD-22 | Chỉ chặn nếu triển khai import lịch sử, hồi tố KPI hoặc bật OCR. |
| Non-blocker / sau MVP | TBD-13, TBD-14, TBD-17, TBD-21 | Có thể giữ giả định hiện tại nếu mentor đồng ý. |
| Đã quyết định | TBD-03, TBD-10, TBD-15 | Không cần hỏi lại khi sign-off MVP, trừ khi phạm vi thay đổi. |

---

## 18. Phụ Lục - Thuật Ngữ

| Thuật ngữ | Giải thích |
|---|---|
| Ngày sản xuất | Ngày nghiệp vụ dùng cho báo cáo sản xuất, có thể khác ngày lịch nếu có ca đêm |
| Staging | Dữ liệu tạm, chưa chốt, chưa dùng cho dashboard chính thức |
| Official data | Dữ liệu đã chốt và được dùng cho báo cáo chính thức |
| Late data | Dữ liệu đến sau hạn nhập hoặc sau khi kỳ đã chốt |
| Drill-down | Luồng truy ngược từ KPI tổng quan về dữ liệu chi tiết tạo ra KPI |
| KPI Dictionary | Tập định nghĩa KPI gồm công thức, nguồn dữ liệu, quy tắc tổng hợp, xử lý thiếu dữ liệu và ví dụ |
| Effective dating | Quản lý ngày hiệu lực của danh mục, định mức hoặc cơ cấu tổ chức |
| Source precedence | Quy tắc xác định dữ liệu nào thắng khi nhiều nguồn cung cấp cùng một chỉ số |
| Business key | Khóa nghiệp vụ dùng để nhận diện hai dòng/bản ghi có đang nói về cùng một dữ liệu; chỉ áp dụng như unique constraint khi loại dữ liệu đó có identity ổn định |
| Matching rule | Quy tắc nhận diện ứng viên trùng cho dữ liệu dạng event khi thời gian hoặc trạng thái có thể được sửa sau này |
| Business data code | Mã ổn định để đối soát record tổng hợp theo company, ngày sản xuất, ca, máy, sản phẩm và loại record; không thay stable ID của event |
| Current data | Dữ liệu thuộc cửa sổ nghiệp vụ đang mở và đến đúng hạn theo cấu hình |
| Historical Backfill | Dữ liệu của kỳ cũ được đưa vào sau để bổ sung lịch sử, khác với late data của kỳ vừa quá hạn |
| Reconciliation | Đối soát các giá trị cùng logical identity giữa web, Excel và bằng chứng OCR; kết quả phải có trạng thái và quyết định xử lý |
| Similarity warning | Cảnh báo ứng viên gần trùng từ ngưỡng cấu hình (baseline 90%); chỉ hỗ trợ review, không tự quyết định |
| Closed with Exceptions | Trạng thái đóng kỳ có dữ liệu thiếu/chờ nhập bù đã được quản lý chấp nhận, không phải kỳ hoàn chỉnh |
| Adapter / mapping chuẩn | Thành phần tích hợp hoặc mapping để đưa dữ liệu công ty vào mô hình dữ liệu chuẩn của hệ thống |
| Guardrail | Mức yêu cầu riêng ngoài MoSCoW: yêu cầu kiểm soát bắt buộc phải có *khi* một capability tùy chọn (OCR, voice, export) được bật, nhưng không thuộc acceptance scope của MVP nếu capability đó chưa triển khai |
| Lưu trữ nóng/lạnh (hot/cold retention) | Chính sách theo thời gian: dữ liệu trong ngưỡng `hotRetentionMonths` giữ ở nơi truy vấn nhanh; dữ liệu cũ hơn chuyển sang lưu trữ chi phí thấp hơn, chấp nhận truy vấn chậm hơn (xem TDD mục 12.4) |
