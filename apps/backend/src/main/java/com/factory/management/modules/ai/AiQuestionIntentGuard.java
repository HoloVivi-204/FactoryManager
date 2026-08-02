package com.factory.management.modules.ai;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AiQuestionIntentGuard {
    private static final String OPERATIONAL_TOOL = "get_operational_details";
    private static final String PRODUCTION_SUMMARY_TOOL = "get_production_summary";
    private static final String PRODUCTIVITY_TOOL = "analyze_productivity";
    private static final String FINANCIAL_TOOL = "compare_financial_periods";
    private static final String MAINTENANCE_COST_TOOL = "rank_maintenance_cost";
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern DAY_EXPRESSION = Pattern.compile(
            "(?:^|\\s)ngay\\s+(\\d{1,2})(?:\\s*(?:/|-|thang\\s+)(\\d{1,2}))?"
                    + "(?:\\s*(?:/|-|nam\\s+)(\\d{4}))?");
    private static final Set<String> OPERATIONAL_ARGUMENTS = Set.of(
            "detailType", "fromDate", "toDate", "factory", "department", "productionLine",
            "team", "shift", "machine", "keyword", "attendanceStatus", "unplannedOnly",
            "includeTemporary", "limit");
    private static final Set<String> PRODUCTION_SUMMARY_ARGUMENTS = Set.of(
            "fromDate", "toDate", "factory", "department", "productionLine",
            "team", "machine", "includeTemporary");
    private static final Set<String> PRODUCTIVITY_ARGUMENTS = Set.of(
            "currentFrom", "currentTo", "previousFrom", "previousTo", "groupBy",
            "factory", "department", "productionLine", "team", "machine");
    private static final Set<String> FINANCIAL_ARGUMENTS = Set.of(
            "currentFrom", "currentTo", "previousFrom", "previousTo",
            "factory", "department", "productionLine");
    private static final Set<String> MAINTENANCE_COST_ARGUMENTS = Set.of(
            "fromDate", "toDate", "factory", "department", "productionLine", "team", "limit");
    private static final Set<String> GENERIC_SCOPE_TOKENS = Set.of(
            "nha", "may", "factory", "phong", "ban", "department", "day", "chuyen",
            "line", "to", "team", "ca", "shift", "machine", "cua", "toi");
    private static final Set<String> GENERIC_MACHINE_KEYWORDS = Set.of(
            "may", "may loi", "loi may", "loi", "may hong", "hong may", "hong",
            "may dung", "dung may", "dung", "su co may", "su co", "truc trac");

    public Resolution resolve(
            String question,
            String modelToolName,
            Map<String, Object> modelArguments,
            List<String> allowedToolNames,
            LocalDate today
    ) {
        String normalized = normalize(question);
        boolean explicitQuality = containsAny(normalized,
                "loi chat luong", "chat luong san pham", "loi san pham", "san pham loi",
                "hang loi", "hang hu", "phe pham", "khuyet tat", "sai kich thuoc",
                "tray xuoc", "loi han", "moi han loi");
        boolean machineSubject = containsWord(normalized, "may")
                || contains(normalized, "thiet bi");
        boolean machineProblem = containsWord(normalized, "loi")
                || containsWord(normalized, "hong")
                || containsWord(normalized, "dung")
                || containsWord(normalized, "ngung")
                || containsAny(normalized, "su co", "downtime", "truc trac",
                "khong hoat dong", "khong chay");

        String forcedDetailType = null;
        if (explicitQuality) {
            forcedDetailType = "QUALITY_ERRORS";
        } else if (machineSubject && machineProblem) {
            forcedDetailType = "MACHINE_DOWNTIME";
        }
        if (forcedDetailType != null && allowedToolNames.contains(OPERATIONAL_TOOL)) {
            Map<String, Object> arguments = safeCopy(modelArguments);
            copyIfMissing(arguments, "fromDate", "currentFrom");
            copyIfMissing(arguments, "toDate", "currentTo");
            arguments.put("detailType", forcedDetailType);

            if ("MACHINE_DOWNTIME".equals(forcedDetailType)) {
                arguments.remove("attendanceStatus");
                boolean explicitlyUnplanned = containsWord(normalized, "loi")
                        || containsWord(normalized, "hong")
                        || containsAny(normalized,
                        "su co", "truc trac", "dot xuat", "ngoai ke hoach",
                        "khong hoat dong", "khong chay");
                if (explicitlyUnplanned) arguments.put("unplannedOnly", true);
                clearGenericMachineKeyword(arguments);
            } else {
                arguments.remove("attendanceStatus");
                arguments.remove("unplannedOnly");
            }
            sanitizeOperationalArguments(arguments, normalized, today);
            return new Resolution(OPERATIONAL_TOOL, arguments);
        }

        if (isProductionPlanComparison(normalized)
                && allowedToolNames.contains(PRODUCTION_SUMMARY_TOOL)) {
            return new Resolution(PRODUCTION_SUMMARY_TOOL,
                    productionSummaryArguments(modelArguments, normalized, today));
        }

        if (OPERATIONAL_TOOL.equals(modelToolName)
                && allowedToolNames.contains(OPERATIONAL_TOOL)) {
            Map<String, Object> arguments = safeCopy(modelArguments);
            copyIfMissing(arguments, "fromDate", "currentFrom");
            copyIfMissing(arguments, "toDate", "currentTo");
            sanitizeOperationalArguments(arguments, normalized, today);
            return new Resolution(OPERATIONAL_TOOL, arguments);
        }

        if (PRODUCTION_SUMMARY_TOOL.equals(modelToolName)
                && allowedToolNames.contains(PRODUCTION_SUMMARY_TOOL)) {
            return new Resolution(PRODUCTION_SUMMARY_TOOL,
                    productionSummaryArguments(modelArguments, normalized, today));
        }

        if (PRODUCTIVITY_TOOL.equals(modelToolName)
                && allowedToolNames.contains(PRODUCTIVITY_TOOL)) {
            return new Resolution(PRODUCTIVITY_TOOL,
                    productivityArguments(modelArguments, normalized, today));
        }

        if (FINANCIAL_TOOL.equals(modelToolName)
                && allowedToolNames.contains(FINANCIAL_TOOL)) {
            return new Resolution(FINANCIAL_TOOL,
                    financialArguments(modelArguments, normalized, today));
        }

        if (MAINTENANCE_COST_TOOL.equals(modelToolName)
                && allowedToolNames.contains(MAINTENANCE_COST_TOOL)) {
            return new Resolution(MAINTENANCE_COST_TOOL,
                    maintenanceCostArguments(modelArguments, normalized, today));
        }

        return new Resolution(modelToolName, safeCopy(modelArguments));
    }

    private boolean isProductionPlanComparison(String question) {
        boolean production = containsAny(question, "san luong", "output");
        boolean plan = containsAny(question, "ke hoach", "muc tieu", "chi tieu");
        boolean comparison = containsAny(question,
                "thuc te", "so voi", "dat bao nhieu", "ty le dat", "hoan thanh",
                "lam sao", "cai thien", "day san luong", "tang san luong");
        return production && plan && comparison;
    }

    private Map<String, Object> productionSummaryArguments(
            Map<String, Object> modelArguments,
            String normalizedQuestion,
            LocalDate today
    ) {
        Map<String, Object> arguments = safeCopy(modelArguments);
        copyIfMissing(arguments, "fromDate", "currentFrom");
        copyIfMissing(arguments, "toDate", "currentTo");
        arguments.keySet().removeIf(key -> !PRODUCTION_SUMMARY_ARGUMENTS.contains(key));
        arguments.entrySet().removeIf(entry -> isNullish(entry.getValue()));
        sanitizeScopes(arguments, normalizedQuestion);
        sanitizeBoolean(arguments, "includeTemporary");

        applyQuestionOrModelDates(arguments, normalizedQuestion, today, "fromDate", "toDate");
        boolean explicitlyRequestsTemporary = containsAny(normalizedQuestion,
                "du lieu tam", "bao cao tam", "ban nhap", "staging",
                "chua duyet", "chua xac nhan", "chua chot");
        arguments.put("includeTemporary", explicitlyRequestsTemporary || asksToday(normalizedQuestion));
        return arguments;
    }

    private Map<String, Object> productivityArguments(
            Map<String, Object> modelArguments,
            String normalizedQuestion,
            LocalDate today
    ) {
        Map<String, Object> arguments = safeCopy(modelArguments);
        arguments.keySet().removeIf(key -> !PRODUCTIVITY_ARGUMENTS.contains(key));
        arguments.entrySet().removeIf(entry -> isNullish(entry.getValue()));
        sanitizeScopes(arguments, normalizedQuestion);

        List<LocalDate> dates = questionDates(normalizedQuestion, today);
        if (!dates.isEmpty()) {
            LocalDate from = dates.get(0);
            LocalDate to = dates.get(dates.size() - 1);
            if (to.isBefore(from)) {
                LocalDate swap = from;
                from = to;
                to = swap;
            }
            arguments.put("currentFrom", from.toString());
            arguments.put("currentTo", to.toString());
            arguments.put("previousFrom", from.minusYears(1).toString());
            arguments.put("previousTo", to.minusYears(1).toString());
        } else {
            for (String key : List.of("currentFrom", "currentTo", "previousFrom", "previousTo")) {
                sanitizeDate(arguments, key);
            }
            orderDateRange(arguments, "currentFrom", "currentTo");
            orderDateRange(arguments, "previousFrom", "previousTo");
        }
        arguments.put("groupBy", normalizedGroupBy(arguments.get("groupBy")));
        return arguments;
    }

    private Map<String, Object> financialArguments(
            Map<String, Object> modelArguments,
            String normalizedQuestion,
            LocalDate today
    ) {
        Map<String, Object> arguments = safeCopy(modelArguments);
        arguments.keySet().removeIf(key -> !FINANCIAL_ARGUMENTS.contains(key));
        arguments.entrySet().removeIf(entry -> isNullish(entry.getValue()));
        sanitizeScopes(arguments, normalizedQuestion);

        List<LocalDate> dates = questionDates(normalizedQuestion, today);
        if (!dates.isEmpty()) {
            LocalDate from = dates.get(0);
            LocalDate to = dates.get(dates.size() - 1);
            arguments.put("currentFrom", from.toString());
            arguments.put("currentTo", to.toString());
            arguments.put("previousFrom", from.minusMonths(1).toString());
            arguments.put("previousTo", to.minusMonths(1).toString());
        } else {
            for (String key : List.of("currentFrom", "currentTo", "previousFrom", "previousTo")) {
                sanitizeDate(arguments, key);
            }
            orderDateRange(arguments, "currentFrom", "currentTo");
            orderDateRange(arguments, "previousFrom", "previousTo");
        }
        return arguments;
    }

    private Map<String, Object> maintenanceCostArguments(
            Map<String, Object> modelArguments,
            String normalizedQuestion,
            LocalDate today
    ) {
        Map<String, Object> arguments = safeCopy(modelArguments);
        arguments.keySet().removeIf(key -> !MAINTENANCE_COST_ARGUMENTS.contains(key));
        arguments.entrySet().removeIf(entry -> isNullish(entry.getValue()));
        sanitizeScopes(arguments, normalizedQuestion);
        applyQuestionOrModelDates(arguments, normalizedQuestion, today, "fromDate", "toDate");
        sanitizeInteger(arguments, "limit", 1, 20);
        return arguments;
    }

    private String normalizedGroupBy(Object raw) {
        if (raw == null) return "PRODUCTION_LINE";
        String value = normalize(String.valueOf(raw)).replace('-', ' ').replace('_', ' ');
        return switch (value) {
            case "factory", "nha may" -> "FACTORY";
            case "department", "phong ban", "bo phan" -> "DEPARTMENT";
            case "team", "to", "to san xuat" -> "TEAM";
            case "machine", "may" -> "MACHINE";
            case "production line", "line", "day chuyen" -> "PRODUCTION_LINE";
            default -> "PRODUCTION_LINE";
        };
    }

    private void applyQuestionOrModelDates(
            Map<String, Object> arguments,
            String normalizedQuestion,
            LocalDate today,
            String fromKey,
            String toKey
    ) {
        List<LocalDate> dates = questionDates(normalizedQuestion, today);
        if (!dates.isEmpty()) {
            arguments.put(fromKey, dates.get(0).toString());
            arguments.put(toKey, dates.get(dates.size() - 1).toString());
        } else {
            sanitizeDate(arguments, fromKey);
            sanitizeDate(arguments, toKey);
            if (arguments.get(fromKey) != null && arguments.get(toKey) == null) {
                arguments.put(toKey, arguments.get(fromKey));
            }
            if (arguments.get(toKey) != null && arguments.get(fromKey) == null) {
                arguments.put(fromKey, arguments.get(toKey));
            }
        }
        orderDateRange(arguments, fromKey, toKey);
    }

    private void orderDateRange(Map<String, Object> arguments, String fromKey, String toKey) {
        LocalDate from = localDate(arguments.get(fromKey));
        LocalDate to = localDate(arguments.get(toKey));
        if (from != null && to != null && to.isBefore(from)) {
            arguments.put(fromKey, to.toString());
            arguments.put(toKey, from.toString());
        }
    }

    private void sanitizeOperationalArguments(
            Map<String, Object> arguments,
            String normalizedQuestion,
            LocalDate today
    ) {
        arguments.keySet().removeIf(key -> !OPERATIONAL_ARGUMENTS.contains(key));
        arguments.entrySet().removeIf(entry -> isNullish(entry.getValue()));
        sanitizeScopes(arguments, normalizedQuestion);
        sanitizeBoolean(arguments, "includeTemporary");
        sanitizeBoolean(arguments, "unplannedOnly");
        sanitizeLimit(arguments);

        List<LocalDate> dates = questionDates(normalizedQuestion, today);
        if (!dates.isEmpty()) {
            arguments.put("fromDate", dates.get(0).toString());
            arguments.put("toDate", dates.get(dates.size() - 1).toString());
        } else {
            sanitizeDate(arguments, "fromDate");
            sanitizeDate(arguments, "toDate");
            if (arguments.get("fromDate") != null && arguments.get("toDate") == null) {
                arguments.put("toDate", arguments.get("fromDate"));
            }
            if (arguments.get("toDate") != null && arguments.get("fromDate") == null) {
                arguments.put("fromDate", arguments.get("toDate"));
            }
        }

        LocalDate from = localDate(arguments.get("fromDate"));
        LocalDate to = localDate(arguments.get("toDate"));
        if (from != null && to != null && to.isBefore(from)) {
            arguments.put("fromDate", to.toString());
            arguments.put("toDate", from.toString());
            LocalDate swap = from;
            from = to;
            to = swap;
        }

        boolean explicitlyRequestsTemporary = containsAny(normalizedQuestion,
                "du lieu tam", "bao cao tam", "ban nhap", "nhap", "staging",
                "chua duyet", "chua xac nhan", "chua chot");
        if (!explicitlyRequestsTemporary) {
            arguments.put("includeTemporary", false);
        }
    }

    private void sanitizeScopes(Map<String, Object> arguments, String normalizedQuestion) {
        for (String key : List.of(
                "factory", "department", "productionLine", "team", "shift", "machine")) {
            Object raw = arguments.get(key);
            if (raw == null) continue;
            if (!(raw instanceof String) && !(raw instanceof Number)) {
                arguments.remove(key);
                continue;
            }
            String selector = normalize(String.valueOf(raw));
            if (selector.isBlank()) {
                arguments.remove(key);
                continue;
            }
            boolean mentioned = normalizedQuestion.contains(selector);
            if (!mentioned) {
                mentioned = Pattern.compile("[^a-z0-9]+")
                        .splitAsStream(selector)
                        .filter(token -> token.length() >= 2 && !GENERIC_SCOPE_TOKENS.contains(token))
                        .anyMatch(token -> containsWord(normalizedQuestion, token));
            }
            if (!mentioned) arguments.remove(key);
        }
    }

    private boolean isNullish(Object value) {
        if (value == null) return true;
        if (!(value instanceof String text)) return false;
        return text.isBlank() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text);
    }

    private void sanitizeBoolean(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null || value instanceof Boolean) return;
        String normalized = normalize(String.valueOf(value));
        if (Set.of("true", "1", "yes", "co").contains(normalized)) arguments.put(key, true);
        else if (Set.of("false", "0", "no", "khong").contains(normalized)) arguments.put(key, false);
        else arguments.remove(key);
    }

    private void sanitizeLimit(Map<String, Object> arguments) {
        sanitizeInteger(arguments, "limit", 1, 100);
    }

    private void sanitizeInteger(Map<String, Object> arguments, String key, int minimum, int maximum) {
        Object value = arguments.get(key);
        if (value == null) return;
        try {
            int parsed = value instanceof Number number
                    ? number.intValue() : Integer.parseInt(String.valueOf(value));
            if (parsed < minimum || parsed > maximum) arguments.remove(key);
            else arguments.put(key, parsed);
        } catch (NumberFormatException exception) {
            arguments.remove(key);
        }
    }

    private List<LocalDate> questionDates(String question, LocalDate today) {
        java.util.Set<LocalDate> result = new java.util.TreeSet<>();
        var matcher = DAY_EXPRESSION.matcher(question);
        while (matcher.find() && result.size() < 2) {
            int day = Integer.parseInt(matcher.group(1));
            int month = matcher.group(2) == null ? today.getMonthValue() : Integer.parseInt(matcher.group(2));
            int year = matcher.group(3) == null ? today.getYear() : Integer.parseInt(matcher.group(3));
            try {
                result.add(LocalDate.of(year, month, day));
            } catch (DateTimeException ignored) {
                return List.of();
            }
        }
        if (containsAny(question, "hom qua", "yesterday")) result.add(today.minusDays(1));
        if (asksToday(question)) result.add(today);
        if (containsAny(question, "ngay mai", "tomorrow")) result.add(today.plusDays(1));
        return result.stream().limit(2).toList();
    }

    private boolean asksToday(String question) {
        return containsAny(question, "hom nay", "today", "ngay hien tai");
    }

    private void sanitizeDate(Map<String, Object> arguments, String key) {
        Object raw = arguments.get(key);
        if (raw == null) return;
        String value = String.valueOf(raw).trim();
        LocalDate parsed = localDate(value);
        if (parsed == null && value.length() >= 10) parsed = localDate(value.substring(0, 10));
        if (parsed == null) {
            String[] parts = value.split("[/.-]");
            if (parts.length == 3) {
                try {
                    parsed = LocalDate.of(
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                } catch (RuntimeException ignored) {
                    parsed = null;
                }
            }
        }
        if (parsed == null) arguments.remove(key);
        else arguments.put(key, parsed.toString());
    }

    private LocalDate localDate(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void clearGenericMachineKeyword(Map<String, Object> arguments) {
        Object raw = arguments.get("keyword");
        if (raw == null) return;
        String normalized = normalize(String.valueOf(raw));
        if (GENERIC_MACHINE_KEYWORDS.contains(normalized)) {
            arguments.remove("keyword");
            return;
        }
        String specificPart = normalized
                .replaceAll("(^|\\s)(may|nao|bi|co|dang|loi|hong|dung|ngung|su|truc|trac|gi|vay)(?=\\s|$)", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (specificPart.isEmpty()) arguments.remove("keyword");
        else if (!specificPart.equals(normalized)) arguments.put("keyword", specificPart);
    }

    private void copyIfMissing(Map<String, Object> arguments, String target, String source) {
        if (arguments.get(target) == null && arguments.get(source) != null) {
            arguments.put(target, arguments.get(source));
        }
    }

    private Map<String, Object> safeCopy(Map<String, Object> arguments) {
        return arguments == null ? new LinkedHashMap<>() : new LinkedHashMap<>(arguments);
    }

    private boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) if (contains(value, phrase)) return true;
        return false;
    }

    private boolean contains(String value, String phrase) {
        return value.contains(phrase);
    }

    private boolean containsWord(String value, String word) {
        return Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(word) + "([^a-z0-9]|$)")
                .matcher(value).find();
    }

    static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record Resolution(String toolName, Map<String, Object> arguments) {
    }
}
