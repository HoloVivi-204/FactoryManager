# Hướng dẫn kiểm thử FactoryManager

## 1. Dành cho ai

Tài liệu này dành cho người thử sản phẩm bằng trình duyệt. Không cần đọc source code, nhưng cần biết role
của tài khoản và phạm vi dữ liệu được cấp.

Luồng hiện tại chỉ là demo nền tảng. Approve một báo cáo staging chưa tương đương quy trình close theo
ngày/scope trong PRD. Các test target chưa có màn hình hoặc API được đánh dấu rõ; không ghi Pass cho chúng.

Tên `Team Leader`, `Production Manager` và `Factory Manager` trong nhóm test `CT-*` là role
**Current/legacy** đang tồn tại trong code. Target dùng
`Quản lý điều hành → Quản lý vận hành → Nhân viên vận hành`; Ca trưởng/Tổ trưởng là chức danh có
ngày hiệu lực, không phải security role hoặc tab riêng.

## 2. Chuẩn bị

Người vận hành demo cung cấp:

- URL frontend và backend đang chạy.
- Frontend phải chạy tại `http://localhost:5173`; CORS hiện không cho phép port Vite tự chọn khác.
- Tài khoản riêng cho từng role cần thử.
- Phạm vi factory/line/team của tài khoản.
- Mã shift, team, machine và ngày sản xuất dùng cho test.
- File Excel mẫu đúng version hiện tại nếu thử import.

Không dùng chung mật khẩu, không chụp token và không đưa dữ liệu nhân sự thật vào ảnh lỗi.

## 3. Cách ghi kết quả

Với mỗi test, ghi:

```text
Test ID:
Thời gian:
Role và scope:
Input chính:
Kết quả mong đợi:
Kết quả quan sát:
Pass / Fail / Blocked:
Request ID nếu có:
Ảnh hoặc log đã che dữ liệu nhạy cảm:
```

`Blocked` dùng khi màn hình/API target chưa tồn tại hoặc môi trường không sẵn sàng; không đổi thành Pass.

## 4. Test luồng hiện tại

### CT-01 — Đăng nhập

1. Mở frontend.
2. Nhập credential của tài khoản test.
3. Chọn đăng nhập.

Mong đợi:

- Credential đúng mở workspace của một role được cấp.
- Credential sai hiển thị lỗi an toàn, không lộ stack trace.
- Refresh trang vẫn xác thực lại phiên hoặc đưa về login khi token hết hạn.

### CT-02 — Role navigation

1. Đăng nhập tài khoản có một role.
2. Ghi lại menu hiển thị.
3. Thử sửa URL sang slug role không được cấp.

Mong đợi:

- Frontend đưa về workspace hợp lệ.
- Không thấy dữ liệu của role/scope khác.

Lưu ý: redirect frontend chỉ là UX. Muốn đánh giá bảo mật phải có API test server-side ở mục CT-08.

### CT-03 — Master data

Áp dụng cho tài khoản admin test:

1. Mở một danh mục, ví dụ Factory, Team hoặc Machine.
2. Tạo record với mã test duy nhất.
3. Sửa tên record.
4. Ngừng sử dụng record nếu UI hỗ trợ.

Mong đợi:

- Mã bắt buộc được validate.
- Sau khi lưu, list phản ánh đúng dữ liệu.
- Ngừng sử dụng là soft deactivate, không làm mất dữ liệu lịch sử.
- Tài khoản không có quyền không thực hiện được mutation.

### CT-04 — Tạo báo cáo staging

Áp dụng cho Team Leader trong đúng scope:

1. Mở `Nhập báo cáo ca`.
2. Chọn ngày, ca, tổ và máy thuộc scope.
3. Nhập kế hoạch, thực tế, hàng lỗi, phút làm việc và phút dừng.
4. Lưu Draft.

Mong đợi:

- Trường bắt buộc và quan hệ factory/line/team/machine được kiểm tra.
- Số âm, defect lớn hơn actual hoặc downtime không hợp lệ bị chặn.
- Draft chưa xuất hiện như official report.

### CT-05 — Nhập chi tiết staging

Từ Draft CT-04:

1. Thêm một khoảng downtime với nguyên nhân.
2. Thêm lỗi chất lượng.
3. Thêm sự cố vật tư hoặc nhân sự thực tế nếu cần cho demo.

Mong đợi:

- Detail gắn đúng staging cha.
- Tổng lỗi chất lượng không vượt defect của báo cáo.
- Người ngoài scope không đọc/sửa được detail.

### CT-06 — Submit và request change

1. Team Leader submit Draft.
2. Đăng nhập Production/Factory Manager.
3. Mở hàng đợi duyệt.
4. Request change với nhận xét.

Mong đợi:

- Draft chuyển Submitted.
- Sau request change, người nhập thấy nhận xét và có thể sửa theo state hiện tại.
- State transition sai bị từ chối.

### CT-07 — Approve, dashboard và blocker Reports

1. Submit lại báo cáo hợp lệ.
2. Manager approve.
3. Mở dashboard hiện tại và xác nhận request `/production-reports/dashboard/my-scope` thành công.
4. Không dùng trang Reports trong happy-path demo. Nếu cần xác nhận blocker, mở riêng trang này và ghi lại lỗi.

Mong đợi hiện tại:

- Một production report được tạo từ staging.
- Detail official được copy.
- Approve lại cùng staging không tạo report thứ hai.
- Dashboard cơ bản phản ánh report trong scope.
- `ReportsPage` không thể tải danh sách: frontend gọi `GET /production-reports/search/my-scope`, trong khi
  backend chưa có route này.

Kết quả bắt buộc hiện tại: **không ghi CT-07 là Pass**. Ghi `Blocked` cho hành trình đầy đủ vì phần Reports
chưa có API tương ứng, dù evidence approve/dashboard riêng lẻ có thể được ghi nhận. Nếu request dashboard lỗi
hoặc không có dữ liệu, current UI có thể hiển thị `0`; không coi số đó là kết quả nghiệp vụ.

Không kết luận các FR-CLO đã Pass: luồng này chưa kiểm tra coverage theo ngày/scope, Partial, reopen/reclose
hoặc official version history.

### CT-08 — Data-scope denial

Chuẩn bị hai team A và B cùng hai tài khoản chỉ có quyền riêng:

1. Dùng tài khoản A đọc ID staging của A.
2. Thử gọi API đọc/sửa staging B.
3. Với Draft A, thử update payload chuyển `teamId`/`machineId` sang B.

Mong đợi bảo mật:

- Cả đọc, sửa và chuyển target relation ngoài scope đều phải bị từ chối server-side, không trả `2xx`.
- Contract chưa chốt sẽ dùng `403` hay `404` để tránh lộ resource; ghi chính xác status quan sát được thay
  vì hardcode một mã.
- Không có dữ liệu B trong response/error.

Current có gap đã biết ở bước 3. Nếu request chuyển được record sang B, ghi Fail mức bảo mật cao và giữ
`requestId`; không tiếp tục dùng record đó trong demo.

### CT-09 — Import Excel toàn bộ hợp lệ

1. Tải template từ UI.
2. Điền các row hợp lệ với business key chưa tồn tại.
3. Preview.
4. Import.

Mong đợi hiện tại:

- Preview cho biết file hợp lệ.
- Import tạo/cập nhật Draft theo contract hiện tại.
- Kết quả nêu tổng row và lỗi.

### CT-10 — Import 95/5

1. Chuẩn bị 100 row, trong đó 5 row thiếu machine code.
2. Preview và import.

Mong đợi theo PRD:

- 95 row hợp lệ vào staging.
- 5 row lỗi được giữ ngoài staging và tải được error file.
- Sửa file rồi upload lại không tạo trùng 95 row.

Current expected:

- Service hủy toàn bộ persistence nếu có bất kỳ error.

Vì vậy test này phải ghi `Fail` cho acceptance PRD cho tới khi partial import được triển khai.

## 5. Test target chưa có đầy đủ UI/API

Các hành trình sau thuộc MVP nhưng hiện ghi `Blocked` hoặc `Missing` trong traceability:

- Error/Warning/Information và Warning override có lý do.
- Conflict review từng field, không ghi đè tự động.
- Coverage và close theo production date + scope.
- Partial close và danh sách child scope thiếu.
- Reopen request, approve/reject, correction workspace và reclose.
- Official version Current/Superseded/Under Correction.
- KPI Dictionary/version, completeness, coverage và freshness.
- Dashboard comparison hợp lệ và full drill-down tới source/audit.
- Notification import partial/missing/close blocker.
- Audit đầy đủ cho nhập/import/close/reopen/reclose/override/conflict.

Các test `TT-*` dưới đây là acceptance Target. Với baseline code hiện tại, ghi `Blocked` hoặc `Fail`
theo bằng chứng; chỉ đổi sang `Pass` sau khi màn hình/API và automated test liên quan đã tồn tại.

### TT-01 — Navigation, ba cấp quyền và chức danh

1. Đăng nhập lần lượt bằng tài khoản Quản lý điều hành, Quản lý vận hành và Nhân viên vận hành.
2. Đối chiếu menu với các module chuẩn ở PRD 4.4; xác nhận không có tab Ca trưởng/Tổ trưởng.
3. Gán chức danh Ca trưởng cho Nhân viên vận hành trong một khoảng hiệu lực rồi đăng nhập lại.
4. Gọi trực tiếp một API ngoài action hoặc data scope của từng tài khoản.

Mong đợi:

- Tên module không đổi tùy tiện theo role; module chưa bật hoặc ngoài quyền không xuất hiện như đã sẵn sàng.
- Gán/bỏ chức danh không tạo role, không nâng quyền và không làm đổi chức danh hiển thị của kỳ lịch sử.
- API ngoài quyền bị từ chối server-side dù người dùng sửa URL hoặc payload.

### TT-02 — Phân loại lịch sử và cảnh báo gần trùng

1. Chuẩn bị fixture ẩn danh gồm dữ liệu Current, Historical Backfill, Late Arrival và Correction.
2. Nhập hai record cùng logical identity/cùng giá trị, một cặp cùng identity/khác giá trị và các candidate
   có score `0.8999`, `0.90`, `0.96`, `0.9999`, `1.00`.
3. Mở candidate detail và thử resolve bằng tài khoản không có quyền, sau đó bằng tài khoản có quyền.

Mong đợi:

- Server sinh `businessDataCode`, `dataTimeClass`, source occurred time và received time theo policy version.
- Exact identity chạy trước: bản giao lại cùng giá trị idempotent; cùng identity khác giá trị vào conflict.
- `0.8999` không tạo near-duplicate warning; từ `0.90` đến dưới `1.00` tạo warning có giải thích và
  model/policy version; `1.00` đi theo exact matching, không bị xử lý như near match mơ hồ.
- Không candidate nào tự merge, xóa, ghi đè hoặc chọn nguồn thắng; resolve có actor, lý do và audit.

### TT-03 — Đối soát Excel–web–OCR

1. Nhập dữ liệu web rồi upload Excel chứa một dòng khớp, một dòng lệch giá trị, một dòng chỉ có trong
   Excel và một dòng chỉ có trên web.
2. Chạy đối soát lại cùng idempotency key.
3. Khi capability OCR đã được duyệt/bật, thêm phiếu giấy synthetic có một field confidence thấp và một
   field lệch; lặp lại khi OCR tắt.
4. Thử chốt khi item bất thường chưa resolve, rồi resolve bằng người có quyền với lý do.

Mong đợi:

- Item lần lượt có `Matched`, `Value Mismatch`, `Missing in Excel`, `Missing on Web`; retry không tạo run/item trùng.
- Excel và web đều qua staging; không nguồn nào tự ghi đè nguồn kia.
- OCR luôn mang nhãn nguồn thứ yếu và confidence từng field; OCR tắt không chặn đối soát Excel–web.
- Mismatch/missing/OCR low-confidence chưa xử lý chặn chốt theo rule; resolution có quyền, lý do và audit.

### TT-04 — Catalog biểu đồ cốt lõi

1. Chọn cùng kỳ/scope/filter và mở các view `production-trend`, `plan-vs-actual`,
   `approved-kpi-components`, `machine-state-mix`, `downtime-pareto`, `defect-pareto`,
   `shift-heatmap`, `data-coverage` và `reconciliation-status` khi có dữ liệu.
2. Lặp lại với scope không có dữ liệu, chốt một phần và snapshot stale.
3. Từ mỗi chart có dữ liệu, drill-down tới contributor/official record/source theo quyền và mở table fallback.

Mong đợi:

- Mọi view dùng official snapshot và cùng filter; staging không lọt vào dashboard chính thức.
- Empty, error, partial, stale và unavailable capability khác nhau; không trạng thái nào bị đổi thành số `0`.
- Chart nêu kỳ, scope, đơn vị, completeness/freshness; table fallback chứa cùng tập dữ liệu.
- Không có biểu đồ xếp hạng hiệu suất cá nhân; chart vật tư/kho/bảo trì chỉ xuất hiện khi capability,
  dữ liệu và quyền tương ứng cùng sẵn sàng.

Khi từng slice được triển khai, thêm test case bám acceptance PRD và liên kết test ID vào
`factory-performance-feature-matrix.csv`.

## 6. Khi gặp lỗi

1. Không retry mutation liên tục nếu chưa biết request trước đã commit hay chưa.
2. Ghi URL, role/scope, input đã che secret và `X-Request-Id`.
3. Kiểm tra lỗi là validation, forbidden, conflict, network hay server error.
4. Với lỗi scope, dừng dùng record bị ảnh hưởng.
5. Với import, giữ file test nhưng loại dữ liệu thật; ghi hash/version file nếu cần tái hiện.
6. Không sửa database thủ công để làm test “Pass”.
