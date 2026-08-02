package com.factory.management.ai;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiRecommendationService {
    public List<AiRecommendation> recommend(
            String question,
            List<AiToolResult> results
    ) {
        if (!asksForAdvice(question)) {
            return List.of();
        }

        AiToolResult production = results.stream()
                .filter(result -> "get_production_summary".equals(result.toolName()))
                .findFirst()
                .orElse(null);
        if (production == null) {
            return List.of();
        }

        Map<String, Object> official = map(production.data().get("official"));
        if (longValue(official.get("reportCount")) <= 0) {
            return List.of();
        }
        long planned = longValue(official.get("plannedQuantity"));
        long actual = longValue(official.get("actualQuantity"));
        long good = longValue(official.get("goodQuantity"));
        long defect = longValue(official.get("defectQuantity"));
        long working = longValue(official.get("workingMinutes"));
        long downtime = longValue(official.get("downtimeMinutes"));
        BigDecimal attainment = decimal(official.get("planAttainmentPercent"));
        BigDecimal defectRate = decimal(official.get("defectRatePercent"));
        long gap = Math.max(planned - actual, 0);

        List<AiRecommendation> recommendations = new ArrayList<>();
        if (planned <= 0) {
            recommendations.add(new AiRecommendation(
                    "HIGH",
                    "Bổ sung hoặc kiểm tra lại kế hoạch sản lượng",
                    "Chưa có plannedQuantity hợp lệ nên không thể đánh giá mức hoàn thành hay tính lượng cần bù.",
                    "Kế hoạch hiện tại bằng 0 trong dữ liệu chính thức.",
                    "Có mẫu số kế hoạch hợp lệ trước khi ra quyết định tăng công suất.",
                    false));
        } else if (gap > 0) {
            BigDecimal increaseNeeded = actual <= 0 ? null : percent(gap, actual);
            recommendations.add(new AiRecommendation(
                    "HIGH",
                    "Bù thiếu hụt " + number(gap) + " sản phẩm",
                    increaseNeeded == null
                            ? "Đặt mục tiêu bổ sung " + number(gap)
                            + " sản phẩm và kiểm tra lại năng lực chạy máy trước khi phân bổ kế hoạch."
                            : "Cần nâng sản lượng thêm khoảng " + number(increaseNeeded)
                            + "% so với mức thực tế hiện tại để chạm kế hoạch.",
                    "Thực tế " + number(actual) + "/" + number(planned)
                            + " sản phẩm, đạt " + number(attainment) + "% kế hoạch.",
                    "Đây là mức sản lượng cần bổ sung để đạt 100% kế hoạch.",
                    false));
        } else if (planned > 0) {
            long exceeded = Math.max(actual - planned, 0);
            recommendations.add(new AiRecommendation(
                    "LOW",
                    exceeded > 0 ? "Duy trì phần vượt " + number(exceeded) + " sản phẩm" : "Duy trì mức đạt kế hoạch",
                    "Giữ ổn định nhịp sản xuất hiện tại và theo dõi downtime, hàng lỗi để tránh mất phần dự phòng.",
                    "Thực tế " + number(actual) + "/" + number(planned)
                            + " sản phẩm, đạt " + number(attainment) + "% kế hoạch.",
                    "Bảo vệ mức sản lượng đã đạt thay vì tăng tải máy không cần thiết.",
                    false));
        }

        addWorstLineRecommendation(recommendations, production.data(), gap);
        addDowntimeRecommendation(recommendations, actual, gap, working, downtime);
        addQualityRecommendation(recommendations, planned, actual, good, defect, defectRate);

        if (recommendations.size() == 1 && gap > 0) {
            recommendations.add(new AiRecommendation(
                    "MEDIUM",
                    "Khoanh vùng nguyên nhân trước khi tăng tải",
                    "Dữ liệu tổng hợp chưa chỉ ra downtime hay lỗi đủ rõ. Hãy đối chiếu nhân sự thực tế, "
                            + "vật tư và tốc độ chu kỳ của ca trước khi tăng công suất máy.",
                    "Khoảng thiếu kế hoạch đã được xác nhận nhưng chưa có bằng chứng chi tiết về nguyên nhân.",
                    "Tránh tăng tốc đồng loạt khi chưa biết đúng điểm nghẽn.",
                    false));
        }
        return recommendations.stream().limit(4).toList();
    }

    private void addWorstLineRecommendation(
            List<AiRecommendation> target,
            Map<String, Object> data,
            long overallGap
    ) {
        List<Map<String, Object>> lines = rows(data.get("breakdownByProductionLine"));
        Map<String, Object> worst = lines.stream()
                .filter(line -> longValue(line.get("plannedQuantity")) > longValue(line.get("actualQuantity")))
                .max(Comparator.comparingLong(line ->
                        longValue(line.get("plannedQuantity")) - longValue(line.get("actualQuantity"))))
                .orElse(null);
        if (worst == null) {
            return;
        }

        long planned = longValue(worst.get("plannedQuantity"));
        long actual = longValue(worst.get("actualQuantity"));
        long gap = planned - actual;
        String name = text(worst.get("name"), text(worst.get("code"), "Dây chuyền chưa xác định"));
        target.add(new AiRecommendation(
                overallGap > 0 && gap * 2 >= overallGap ? "HIGH" : "MEDIUM",
                "Ưu tiên " + name,
                "Kiểm tra downtime, tốc độ chu kỳ, nhân sự và vật tư tại dây chuyền này trước; "
                        + "đây là nơi đang hụt sản lượng nhiều nhất trong dữ liệu hiện có.",
                name + " đạt " + number(actual) + "/" + number(planned)
                        + " sản phẩm, còn thiếu " + number(gap) + ".",
                "Tập trung xử lý đúng dây chuyền đang đóng góp khoảng trống lớn nhất.",
                false));
    }

    private void addDowntimeRecommendation(
            List<AiRecommendation> target,
            long actual,
            long gap,
            long working,
            long downtime
    ) {
        long operating = Math.max(working - downtime, 0);
        if (downtime <= 0 || operating <= 0 || actual <= 0) {
            return;
        }

        BigDecimal unitsPerMinute = BigDecimal.valueOf(actual)
                .divide(BigDecimal.valueOf(operating), 4, RoundingMode.HALF_UP);
        long minutesNeeded = gap <= 0 ? Math.max(1, Math.round(downtime * 0.2))
                : BigDecimal.valueOf(gap).divide(unitsPerMinute, 0, RoundingMode.CEILING).longValue();
        long recoverMinutes = Math.min(minutesNeeded, downtime);
        long estimatedUnits = unitsPerMinute.multiply(BigDecimal.valueOf(recoverMinutes))
                .setScale(0, RoundingMode.HALF_UP).longValue();
        String action = gap > 0 && minutesNeeded <= downtime
                ? "Nếu giữ được tốc độ bình quân hiện tại, cần thu hồi khoảng " + number(minutesNeeded)
                + " phút downtime để tạo thêm xấp xỉ " + number(gap) + " sản phẩm. Hãy mở chi tiết máy dừng để ưu tiên sự cố dài nhất."
                : "Giảm trước khoảng " + number(recoverMinutes)
                + " phút downtime và ưu tiên các máy có thời gian dừng cao nhất trong dashboard.";
        target.add(new AiRecommendation(
                gap > 0 ? "HIGH" : "MEDIUM",
                "Giảm thời gian dừng máy",
                action,
                "Downtime " + number(downtime) + "/" + number(working)
                        + " phút; tốc độ bình quân khi vận hành khoảng " + number(unitsPerMinute)
                        + " sản phẩm/phút.",
                "Ước tính giải phóng khoảng " + number(estimatedUnits)
                        + " sản phẩm nếu tốc độ hiện tại được giữ nguyên.",
                true));
    }

    private void addQualityRecommendation(
            List<AiRecommendation> target,
            long planned,
            long actual,
            long good,
            long defect,
            BigDecimal defectRate
    ) {
        if (defect <= 0 || actual <= 0) {
            return;
        }

        BigDecimal retainedPerPoint = BigDecimal.valueOf(actual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal goodPlanRate = planned <= 0 ? null : percent(good, planned);
        target.add(new AiRecommendation(
                "MEDIUM",
                "Giảm hàng lỗi để tăng sản lượng đạt",
                "Khoanh vùng loại lỗi có số lượng cao nhất và công đoạn phát sinh trước khi tăng tốc máy. "
                        + "Giảm lỗi giúp tăng hàng đạt, nhưng không được tính là đã tăng tổng actualQuantity.",
                number(defect) + " sản phẩm lỗi, " + number(good) + " sản phẩm đạt; tỷ lệ lỗi "
                        + number(defectRate) + "%"
                        + (goodPlanRate == null ? "." : ", hàng đạt tương đương "
                        + number(goodPlanRate) + "% kế hoạch."),
                "Mỗi khi giảm 1 điểm phần trăm lỗi có thể giữ lại khoảng "
                        + number(retainedPerPoint) + " sản phẩm đạt.",
                true));
    }

    private boolean asksForAdvice(String question) {
        String value = AiQuestionIntentGuard.normalize(question);
        return containsAny(value,
                "loi khuyen", "khuyen nghi", "de xuat", "cach gi", "cach nao", "lam sao",
                "cai thien", "day san luong", "tang san luong", "nang san luong",
                "nang cao", "tot hon", "toi uu");
    }

    private boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) {
            if (value.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? 0 : new BigDecimal(String.valueOf(value)).longValue();
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private String number(Object value) {
        BigDecimal decimal = decimal(value).stripTrailingZeros();
        return java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN")).format(decimal);
    }

    private String text(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map
                ? new LinkedHashMap<>((Map<String, Object>) map)
                : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rows(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().filter(Map.class::isInstance)
                .<Map<String, Object>>map(item -> new LinkedHashMap<>((Map<String, Object>) item))
                .toList();
    }
}
