# Design System FactoryManager

## 1. Phạm vi

Tài liệu này định nghĩa hợp đồng UI cho phần lõi hiệu suất nhà máy. Nó bao gồm token, component, trạng thái
dữ liệu, responsive và accessibility. Nhân sự/vật tư chỉ thuộc lõi ở phạm vi vận hành; các màn hình
chấm công/lương/KPI cá nhân, tài chính, kho đầy đủ, bảo trì đầy đủ và chatbot đang có trong code không tự mở rộng phạm vi Must của PRD.

Có hai lớp:

- **Current**: quy ước quan sát được trong CSS/component hiện tại.
- **Target**: quy ước cần dùng khi xây các màn hình PRD còn thiếu.

## 2. Nguyên tắc sản phẩm

1. Trạng thái dữ liệu quan trọng hơn trang trí: luôn cho biết Draft, Needs Review, Partial, Closed,
   Reopened, Adjusted hoặc Stale.
2. Dữ liệu thiếu khác dữ liệu bằng 0. UI dùng `Chưa có dữ liệu` hoặc lý do thiếu; không hiển thị `0` thay thế.
3. KPI phải đi cùng kỳ, scope, freshness, coverage và trạng thái completeness.
4. Warning phải hiển thị nguyên nhân, người có quyền override và ô nhập lý do.
5. Drill-down giữ nguyên filter và breadcrumb từ nhà máy tới source/audit.
6. Không dùng màu làm tín hiệu duy nhất; mọi màu trạng thái có nhãn hoặc biểu tượng kèm theo.
7. Không hiển thị chức năng mà API/permission hiện tại không hỗ trợ như thể đã dùng được.
8. AI/OCR chỉ tạo cảnh báo có score/confidence và giải thích; CTA của con người mới resolve dữ liệu.
9. Nhiều biểu đồ phải tạo thêm góc nhìn phân tích, không lặp cùng một số dưới nhiều hình thức trang trí.

## 3. Token hiện tại

CSS hiện dùng literal value thay vì CSS custom properties. Bảng dưới đặt tên semantic cho các giá trị đang
được dùng để component mới không tạo thêm biến thể.

### 3.1. Màu

| Token đề xuất | Giá trị hiện tại | Mục đích |
|---|---:|---|
| `--color-bg-page` | `#f4f7fb` | Nền trang |
| `--color-bg-surface` | `#ffffff` | Panel, card, modal |
| `--color-bg-sidebar` | `#101a31` | Sidebar |
| `--color-text-primary` | `#172033` | Nội dung chính |
| `--color-text-secondary` | `#788397` | Mô tả, metadata |
| `--color-border` | `#e4e9f1` | Viền panel |
| `--color-primary` | `#356af4` | CTA, tab active, KPI accent |
| `--color-success` | `#18a875` | Thành công/complete |
| `--color-warning` | `#f29b4b` | Warning/partial |
| `--color-danger` | `#e95458` | Error/destructive |
| `--color-info` | `#7c59d9` | Information/secondary KPI |

Target cần chuyển các giá trị này thành custom properties tại `:root`. Không thêm màu nghiệp vụ trực tiếp
trong component.

### 3.2. Typography

- Font hiện tại: `Inter`, fallback `Arial, sans-serif`.
- Body/table: khoảng 12–13 px hiện tại; target tối thiểu 14 px cho nội dung tương tác chính.
- Page title: 24 px.
- KPI value: 25 px.
- Label in hoa hiện tại: 10 px; không dùng cho nội dung cần đọc liên tục.
- Font weight: 400, 500, 600, 700, 800.

Google Fonts hiện được import từ internet. Deployment kín mạng cần self-host font hoặc chấp nhận fallback.

### 3.3. Shape và khoảng cách

| Token | Giá trị quan sát được |
|---|---:|
| Radius control | 7–9 px |
| Radius panel/card | 11–12 px |
| Radius modal | 14–18 px |
| Content padding desktop | 28 px |
| Panel padding | 17–22 px |
| Grid gap | 12–18 px |
| Sidebar width | 245 px |
| Topbar height | 78 px |

Target dùng thang spacing `4, 8, 12, 16, 20, 24, 32` px.

## 4. Component contract

### 4.1. Application shell

- Desktop: sidebar cố định + topbar + content tối đa 1600 px.
- Tablet dưới 1000 px: sidebar thu gọn, card KPI còn hai cột.
- Mobile dưới 650 px: sidebar ẩn, content một cột.
- Target phải có nút mở navigation trên mobile; việc chỉ ẩn sidebar là chưa đủ.
- Role switcher chỉ hiển thị role server trả về; client-side navigation không thay server authorization.
- Ba role vận hành hiển thị là `Quản lý điều hành`, `Quản lý vận hành`, `Nhân viên vận hành`.
- Không có workspace/tab `Ca trưởng/Tổ trưởng`; chức danh này nằm trong hồ sơ/phân công của `Quản lý nhân sự`.
- Module key và label chuẩn theo thứ tự: Tổng quan điều hành, Quản lý sản xuất, Quản lý vận hành,
  Quản lý nhân sự, Quản lý vật tư, Quản lý kho, Bảo trì & lập lịch, Dữ liệu & đối soát.
- Tab capability chưa bật hoặc ngoài quyền không render. Việc không render vẫn không thay kiểm tra quyền API.

### 4.2. Page header

Gồm:

- Tiêu đề duy nhất `h1` hoặc `h2` theo hierarchy.
- Mô tả ngắn về scope/kỳ.
- Primary action nếu người dùng có quyền.
- Optional status badge cho Draft, Partial, Adjusted hoặc Stale.

### 4.3. KPI card

Mỗi card KPI chính thức phải có:

- Tên KPI và đơn vị.
- Giá trị hoặc trạng thái `Chưa đủ dữ liệu`.
- Kỳ và scope.
- Coverage/completeness.
- So sánh kỳ trước chỉ khi hai kỳ đủ điều kiện.
- Freshness hoặc `Đang cập nhật`.
- Link drill-down nếu người dùng có quyền.

Không hiển thị màu tốt/xấu trước khi threshold config của KPI được xác định.

### 4.4. Filter bar

Target filter chuẩn:

- Kỳ: ngày, tuần, tháng, năm hoặc tùy chỉnh.
- Factory, line, work group, machine, shift.
- Product chỉ khi danh mục sản phẩm đáng tin cậy.
- Data status/completeness khi màn hình hỗ trợ điều tra.

Filter phải:

- Giữ trạng thái khi đổi tab/biểu đồ.
- Có hành động `Đặt lại`.
- Hiển thị filter đang áp dụng trong export hoặc drill-down.
- Không gửi company scope từ client như một nguồn đáng tin; server resolve từ auth/deployment context.

### 4.5. Table

- Header rõ, hỗ trợ overflow ngang trên màn hình nhỏ.
- Có loading skeleton, empty state, error state và retry.
- Row có stable key; không dùng index nếu dữ liệu thay đổi.
- Pagination ở server cho tập dữ liệu lớn.
- Cột action có nhãn accessible; icon đơn lẻ cần `aria-label`.
- Audit/source table không lộ dữ liệu ngoài data scope.

### 4.6. Badge trạng thái

| Nhóm | Màu semantic | Nhãn bắt buộc |
|---|---|---|
| Complete/Approved/Active | Success | Có |
| Warning/Partial/Pending Review | Warning | Có |
| Error/Rejected/Failed | Danger | Có |
| Draft/Information/Processing | Info hoặc neutral | Có |
| Reopened/Adjusted/Stale | Purple hoặc warning | Có |

Các trạng thái khác nghĩa không dùng chung một nhãn chỉ vì cùng màu.

### 4.7. Form

- Label luôn liên kết input bằng `for`/`id`.
- Trường bắt buộc có ký hiệu và mô tả lỗi tại trường.
- Submit disable trong lúc gửi nhưng vẫn cho đọc giá trị.
- Error server map về field khi có `fieldErrors`; lỗi tổng quát hiển thị ở đầu form.
- Form staging phân biệt production date với calendar date.
- Warning override bắt buộc textarea lý do trước khi gửi.
- Destructive action cần xác nhận với tên entity/scope cụ thể.

### 4.8. Modal và dialog

- Focus chuyển vào dialog và quay lại trigger khi đóng.
- Đóng được bằng Escape trừ bước đang commit không thể hủy.
- Có title/description accessible.
- Action order: secondary bên trái, primary/destructive bên phải.
- Dialog review conflict phải cho so sánh current/proposed/source theo từng field.

### 4.9. Toast

- Dùng cho kết quả thao tác ngắn, không thay error state lâu dài.
- Không chứa secret hoặc dữ liệu cá nhân.
- Lỗi import theo dòng phải nằm trong màn hình/bảng tải lỗi, không chỉ trong toast.

### 4.10. Reconciliation và similarity review

- Mỗi item hiển thị `businessDataCode`, `dataTimeClass`, nguồn web/Excel/OCR và trạng thái.
- So sánh theo từng field; không chỉ dùng một phần trăm tổng hợp.
- Warning từ 90% đến dưới 100% hiển thị score, trường đóng góp, policy/model version và nhãn “AI hỗ trợ”.
- OCR hiển thị confidence từng field và ảnh crop/bounding evidence khi có quyền; OCR luôn có nhãn nguồn thứ yếu.
- CTA tách rõ `Liên kết bản ghi`, `Giữ riêng`, `Tạo xung đột`, `Sửa ứng viên`; mọi action yêu cầu lý do khi làm thay đổi kết luận.
- Không có nút “Tự động gộp tất cả”. Item mismatch/missing/low-confidence chưa resolve phải có trạng thái chặn chốt rõ.

## 5. Trạng thái màn hình bắt buộc

Mỗi screen trong [`docs/ui/SCREEN_MAP.md`](docs/ui/SCREEN_MAP.md) phải mô tả:

1. `Loading`: giữ layout ổn định, không hiển thị số 0 tạm.
2. `Empty`: nói rõ chưa có dữ liệu, thiếu quyền hay filter không khớp.
3. `Error`: mã/thông điệp an toàn và retry phù hợp.
4. `Unauthorized`: không render dữ liệu cũ trước khi redirect.
5. `Partial`: coverage và danh sách scope thiếu.
6. `Stale/Recalculating`: giữ snapshot gần nhất nhưng gắn nhãn freshness.
7. `Offline/network failure`: không giả thao tác đã thành công.

## 6. Biểu đồ và dữ liệu

- Luôn có title, unit, period và legend.
- Tooltip hiển thị scope, timestamp/kỳ và trạng thái dữ liệu.
- Không so sánh Partial với Complete nếu có thể gây hiểu nhầm.
- Màu series ổn định giữa các màn hình.
- Có bảng dữ liệu hoặc text summary tương đương cho accessibility.
- Top loss reason phải liên kết tới record hoặc nhóm contributor liên quan.
- KPI chart không dùng trung bình KPI con nếu KPI Dictionary yêu cầu weighted/raw aggregation.

Catalog cốt lõi và dạng mặc định:

| Chart code | Dạng | Mục đích |
|---|---|---|
| `production-trend` | Line/area | Xu hướng sản lượng theo kỳ |
| `plan-vs-actual` | Grouped bar | Chênh lệch kế hoạch–thực tế |
| `approved-kpi-components` | Multi-series line/bar | Thành phần KPI đã duyệt |
| `machine-state-mix` | Stacked bar | Tỷ trọng running/downtime/no-plan |
| `downtime-pareto` | Bar + cumulative line | Lý do gây mất thời gian lớn nhất |
| `defect-pareto` | Bar + cumulative line | Loại lỗi chất lượng chính |
| `shift-heatmap` | Heatmap | Điểm nóng theo ngày/ca |
| `data-coverage` | Stacked bar | Complete/partial/missing/stale |
| `reconciliation-status` | Stacked bar/donut | Match/mismatch/missing/OCR confidence |

Chart vật tư, kho và bảo trì là conditional. Mỗi chart phải dùng cùng filter bar, hiển thị
`ready/empty/partial/stale/updating/unavailable capability`, có drill-down hoặc bảng fallback và
không có xếp hạng hiệu suất cá nhân.

## 7. Responsive

| Breakpoint hiện tại | Hành vi |
|---:|---|
| `> 1000px` | Sidebar đầy đủ, KPI 4 cột, chart 2:1 |
| `651–1000px` | Sidebar thu gọn, KPI 2 cột, chart 1 cột |
| `≤ 650px` | Sidebar ẩn, content/KPI/filter 1 cột |

Target cần bổ sung:

- Mobile navigation có thể mở và thao tác bằng bàn phím.
- Table ưu tiên cột chính hoặc card layout thay vì chỉ cuộn ngang vô hạn.
- Touch target tối thiểu 44×44 px cho action chính.
- Modal không vượt viewport và body không scroll phía sau.

### 7.1. Viewport acceptance

Breakpoint CSS và viewport nghiệm thu là hai khái niệm khác nhau. Tối thiểu phải
kiểm chứng:

- `390px`: form nhập liệu dùng được trên mobile; navigation, validation và
  primary action không bị che hoặc buộc cuộn ngang toàn trang.
- `768px`: form và màn hình review dùng được trên tablet; filter, table/card và
  dialog giữ đúng thứ tự đọc và thao tác.
- `1366px`: dashboard desktop hiển thị đầy đủ KPI, filter, chart và drill-down
  mà không chồng lấn hoặc cắt nội dung chính.

Mỗi màn hình mới ghi screenshot hoặc E2E/visual evidence ở viewport áp dụng.
Browser tối thiểu vẫn phải được chốt trước UAT; ba viewport trên không thay thế
browser compatibility matrix.

## 8. Accessibility gate

- Điều hướng và form dùng được chỉ bằng bàn phím.
- Focus visible có độ tương phản rõ.
- Semantic heading không nhảy cấp.
- Contrast theo WCAG 2.1 AA cho text và control.
- Icon, chart và status có text alternative.
- Validation gắn `aria-invalid` và `aria-describedby`.
- Toast quan trọng dùng live region phù hợp.
- Không tự động chuyển focus hoặc refresh dashboard làm mất ngữ cảnh người dùng.

Các gate này là target contract; repository hiện chưa có automated accessibility test.

## 9. Debt hiện tại cần tránh nhân rộng

- CSS chứa nhiều block `.admin-actions` và modal bị lặp.
- Token đang hardcode ở nhiều selector.
- Một số module dùng component placeholder dù navigation trông như chức năng hoàn chỉnh.
- Mobile ẩn sidebar mà chưa có navigation thay thế.
- `src/utils/format.ts` đang format `undefined` thành `0`/`0.00%`; khi request
  lỗi hoặc dữ liệu thiếu, overview có thể trình bày số không như một phép đo hợp lệ.
- `StandardRoleOverview` trong `RoleOverviewPage.tsx` chỉ xử lý loading, bỏ qua
  `summary.error` rồi render các formatter fallback; lỗi tải có thể bị che thành
  KPI bằng 0 hoặc phạm vi rỗng.
- Chưa có visual regression hoặc accessibility test.

Khi sửa UI, gom token và component dùng chung trước khi thêm biến thể mới; không mở rộng các block CSS lặp.
