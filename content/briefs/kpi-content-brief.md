# Content Brief mẫu cho KPI vận hành

## 1. Mục đích

Mẫu này biến một KPI trong PRD thành định nghĩa đủ chi tiết để cấu hình, tính toán, kiểm thử và drill-down.
Không triển khai dashboard chính thức khi các trường bắt buộc chưa được chốt.

OEE hiện chỉ là ví dụ minh họa cấu trúc. Công thức, nguồn và aggregation chính thức vẫn phụ thuộc KPI
Dictionary được phê duyệt; không lấy công thức hardcode hiện tại làm quyết định nghiệp vụ mặc định.

## 2. Metadata

| Trường | Nội dung cần ghi |
|---|---|
| `kpiCode` | Stable ID, ví dụ `KPI-OEE` |
| Tên hiển thị | Tên tiếng Việt, tên rút gọn |
| Mục đích | Quyết định vận hành mà KPI hỗ trợ |
| Owner nghiệp vụ | Nhóm chịu trách nhiệm định nghĩa, không phải quyền code |
| Trạng thái | Draft / Approved / Retired |
| Version | Số version bất biến sau khi approved |
| Effective from/to | Khoảng hiệu lực |
| Scope áp dụng | Company/factory/line/work group/machine type |
| Grain | Shift/day/week/month/year/custom |
| Unit | `%`, phút, sản phẩm, sự kiện... |

## 3. Inputs

Với mỗi input, ghi:

| Field | Source record type | Unit | Required | Missing meaning | Validation |
|---|---|---|---|---|---|
| Planned production time | Machine state/plan | minute | Yes | Chưa đủ dữ liệu | Không âm |
| Downtime | Machine state interval | minute | Yes | Chưa đủ dữ liệu | Không vượt khoảng đo nếu không có lý do |
| Planned quantity | Production plan/output | item | Tùy formula | Chưa có kế hoạch | Không âm |
| Actual quantity | Production output | item | Yes | Chưa nhập sản lượng | Không âm |
| Good quantity | Production output/quality | item | Yes | Chưa đủ dữ liệu chất lượng | `0 ≤ good ≤ actual` |

Tên input trên chỉ là ví dụ. Trước khi approved phải chỉ rõ canonical field và source precedence để tránh
đếm defect hai lần từ production output và quality record.

## 4. Formula type

Chọn một formula type được code sẵn, không lưu biểu thức tự do:

- Ratio.
- Duration ratio.
- Weighted ratio.
- Sum/count.
- Composite KPI từ các component đã định nghĩa.

Đối với KPI composite, ghi:

```text
component list:
  - code
  - version
  - input grain
  - weight/composition rule
```

Không dùng `eval()` hoặc công thức do người dùng nhập tự do.

## 5. Aggregation

Phải trả lời đầy đủ:

1. Kỳ tháng tính lại từ raw/official records hay lấy trung bình KPI ngày?
2. Weight là duration, planned quantity, actual quantity hay giá trị khác?
3. Child scope thiếu dữ liệu ảnh hưởng numerator/denominator thế nào?
4. Có cho so sánh Partial với Complete không?
5. Khi machine chuyển line, ancestor được resolve theo thời điểm nào?
6. Snapshot cấp cha lưu input watermark/version set ra sao?

Ví dụ minh họa không phải quyết định:

```text
Không dùng: average(machineA.oee, machineB.oee)
Khi KPI Dictionary yêu cầu weighted aggregation:
  aggregate component numerators/denominators từ official records
  rồi mới tính KPI ở scope đích.
```

## 6. Missing data và coverage

Phân biệt:

- `MISSING_REQUIRED`: thiếu input bắt buộc.
- `CONFIRMED_NO_PRODUCTION`: đã xác nhận không sản xuất.
- `NOT_APPLICABLE`: KPI không áp dụng cho scope.
- `ZERO_MEASURED`: giá trị đo hợp lệ bằng 0.
- `PARTIAL`: một phần child scope chưa đủ.

Mỗi snapshot ghi:

- `completenessStatus`.
- `coverageRate`.
- Danh sách hoặc count scope thiếu.
- `lastCalculatedAt`.
- `sourceVersion` hoặc deterministic input watermark.

Không chuyển mẫu số 0 thành KPI 0 nếu business rule chưa xác định đó là phép đo hợp lệ.

## 7. Rounding và display

- Precision lưu trữ.
- Precision hiển thị.
- Rounding mode.
- Unit suffix.
- Cách hiển thị missing/partial/stale.
- Ngưỡng màu theo version và scope.
- Text alternative cho chart.

## 8. Threshold

| Level | Điều kiện | Semantic | Hành động gợi ý |
|---|---|---|---|
| Good | Chưa chốt | Success | Theo dõi |
| Attention | Chưa chốt | Warning | Drill-down top loss |
| Critical | Chưa chốt | Danger | Điều tra ca/máy/source |

Không gán số ngưỡng trước khi có decision nghiệp vụ. Threshold cần effective date và có thể khác theo
machine type.

## 9. Worked examples

Mỗi KPI approved cần ít nhất:

1. Trường hợp đầy đủ dữ liệu.
2. Mẫu số bằng 0.
3. Thiếu một input bắt buộc.
4. Scope Partial.
5. Hai child scope có quy mô khác nhau để kiểm tra weighting.
6. Reclose tạo official version mới và snapshot recalculation.
7. Thay đổi KPI version không hồi tố.

Mỗi ví dụ ghi input, phép tính từng bước, output, coverage và display state.

## 10. Drill-down

Xác định đường đi:

```text
KPI snapshot
  → contributor
  → official record/version
  → staging/source record
  → source batch/file metadata hoặc manual provenance
  → audit summary
```

Mỗi bước phải áp dụng data scope server-side.

## 11. Acceptance

- KPI definition/version immutable sau approved.
- Chọn đúng version theo production date/effective date.
- Kết quả khớp worked examples.
- Missing không biến thành zero.
- Aggregation khớp weighting đã chốt.
- Reclose đánh dấu snapshot liên quan stale và tính lại ancestor bottom-up.
- Dashboard hiển thị completeness, coverage và freshness.
- Drill-down tới source/audit trong quyền.
- Unit, integration, E2E và performance test đều tham chiếu `kpiCode` + version.

## 12. Open decisions bắt buộc đóng trước khi dùng OEE chính thức

- Nguồn chuẩn của planned time, run time, ideal cycle time và defect.
- Công thức Availability/Performance/Quality/OEE.
- Cap trên 100% có được phép hay là Warning.
- Aggregation tháng và cấp cha.
- Missing/no-production/zero denominator.
- Threshold theo machine type.
- Retroactive recalculation khi KPI version thay đổi.
