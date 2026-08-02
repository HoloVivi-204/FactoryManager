package com.factory.management.service.ServiceImpl;

import com.factory.management.entity.DailyCloseBatch;
import com.factory.management.entity.DailyCloseBatchItem;
import com.factory.management.entity.EmployeeActualStaging;
import com.factory.management.entity.Factory;
import com.factory.management.entity.MachineDowntimeStaging;
import com.factory.management.entity.MaterialIssueStaging;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.QualityReportStaging;
import com.factory.management.exception.CloseWorkflowException;
import com.factory.management.repository.DailyCloseBatchItemRepository;
import com.factory.management.repository.DailyCloseBatchRepository;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportExcelService {
    public static final String TEMPLATE_VERSION = "1.0";
    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final int MAX_VALIDATION_ERRORS = 500;

    private static final String INFO_SHEET = "ThongTinBaoCao";
    private static final String OUTPUT_SHEET = "SanLuong";
    private static final String DOWNTIME_SHEET = "MayMoc_Downtime";
    private static final String QUALITY_MATERIAL_SHEET = "ChatLuong_VatTu";
    private static final String EMPLOYEE_SHEET = "NhanSuThucTe";

    private final DailyCloseBatchRepository batchRepository;
    private final DailyCloseBatchItemRepository itemRepository;
    private final MachineDowntimeStagingRepository downtimeRepository;
    private final QualityReportStagingRepository qualityRepository;
    private final MaterialIssueStagingRepository materialRepository;
    private final EmployeeActualStagingRepository employeeRepository;

    @Transactional(readOnly = true)
    public WorkbookPayload generate(Long closeBatchId) {
        WorkbookModel model = buildModel(closeBatchId);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.getProperties().getCoreProperties().setCreator("Factory Management");
            workbook.getProperties().getCoreProperties().setTitle("Daily close batch " + closeBatchId);

            CellStyle headerStyle = headerStyle(workbook);
            for (Map.Entry<String, List<List<String>>> entry : model.sheets().entrySet()) {
                Sheet sheet = workbook.createSheet(entry.getKey());
                writeSheet(sheet, entry.getValue(), headerStyle);
            }
            workbook.write(output);
            return new WorkbookPayload(output.toByteArray(), model.dataRowCount());
        } catch (IOException exception) {
            throw CloseWorkflowException.unprocessable(1408, "Không thể tạo file Excel: " + safeMessage(exception));
        }
    }

    @Transactional(readOnly = true)
    public ValidationResult validate(Long closeBatchId, byte[] content) {
        WorkbookModel expected = buildModel(closeBatchId);
        List<ValidationError> errors = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Set<String> requiredNames = expected.sheets().keySet();
            if (workbook.getNumberOfSheets() != requiredNames.size()) {
                errors.add(new ValidationError("WORKBOOK", null, null, "INVALID_SHEET_COUNT",
                        "Số sheet không đúng template", String.valueOf(workbook.getNumberOfSheets())));
            }
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                String actualName = workbook.getSheetName(index);
                if (!requiredNames.contains(actualName)) {
                    errors.add(new ValidationError(actualName, null, null, "UNEXPECTED_SHEET",
                            "File chứa sheet không thuộc template", actualName));
                }
            }
            for (Map.Entry<String, List<List<String>>> entry : expected.sheets().entrySet()) {
                Sheet actual = workbook.getSheet(entry.getKey());
                if (actual == null) {
                    errors.add(new ValidationError(entry.getKey(), null, null, "MISSING_SHEET",
                            "Thiếu sheet bắt buộc", null));
                    continue;
                }
                compareSheet(entry.getKey(), entry.getValue(), actual, errors);
                if (errors.size() >= MAX_VALIDATION_ERRORS) {
                    break;
                }
            }
        } catch (IOException | RuntimeException exception) {
            errors.add(new ValidationError("WORKBOOK", null, null, "INVALID_XLSX_FILE",
                    "File không phải XLSX hợp lệ hoặc đã bị hỏng", safeMessage(exception)));
        }
        if (errors.size() > MAX_VALIDATION_ERRORS) {
            errors = new ArrayList<>(errors.subList(0, MAX_VALIDATION_ERRORS));
        }
        return new ValidationResult(List.copyOf(errors), expected.dataRowCount());
    }

    private WorkbookModel buildModel(Long closeBatchId) {
        DailyCloseBatch batch = batchRepository.findById(closeBatchId)
                .orElseThrow(() -> CloseWorkflowException.notFound(1401, "Lô chốt ngày không tồn tại"));
        List<DailyCloseBatchItem> items = itemRepository.findAllByCloseBatch_IdOrderBySequenceNo(closeBatchId);
        LinkedHashMap<String, List<List<String>>> sheets = new LinkedHashMap<>();
        sheets.put(INFO_SHEET, infoRows(batch));
        sheets.put(OUTPUT_SHEET, outputRows(items));
        sheets.put(DOWNTIME_SHEET, downtimeRows(items));
        sheets.put(QUALITY_MATERIAL_SHEET, qualityMaterialRows(items));
        sheets.put(EMPLOYEE_SHEET, employeeRows(items));
        int rows = sheets.values().stream().mapToInt(values -> Math.max(0, values.size() - 1)).sum();
        return new WorkbookModel(sheets, rows);
    }

    private List<List<String>> infoRows(DailyCloseBatch batch) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(row("FIELD", "VALUE"));
        rows.add(row("TEMPLATE_VERSION", TEMPLATE_VERSION));
        rows.add(row("BATCH_ID", batch.getId()));
        rows.add(row("BATCH_NO", batch.getBatchNo()));
        rows.add(row("REPORT_DATE", batch.getReportDate()));
        rows.add(row("SCOPE_TYPE", batch.getScopeType()));
        rows.add(row("SCOPE_ID", batch.getScopeId()));
        rows.add(row("SCOPE_CODE", batch.getScopeCode()));
        rows.add(row("SHIFT_ID", batch.getShift() == null ? null : batch.getShift().getId()));
        rows.add(row("REPORT_COUNT", batch.getReportCount()));
        return rows;
    }

    private List<List<String>> outputRows(List<DailyCloseBatchItem> items) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(row("STAGING_ID", "REPORT_DATE", "SHIFT_CODE", "FACTORY_CODE", "DEPARTMENT_CODE",
                "PRODUCTION_LINE_CODE", "TEAM_CODE", "LEADER_CODE", "MACHINE_CODE", "PLANNED_QUANTITY",
                "ACTUAL_QUANTITY", "GOOD_QUANTITY", "DEFECT_QUANTITY", "WORKING_MINUTES",
                "DOWNTIME_MINUTES", "NOTE"));
        for (DailyCloseBatchItem item : items) {
            ProductionReportStaging report = item.getStagingReport();
            rows.add(row(report.getId(), report.getReportDate(), report.getShift().getCode(),
                    report.getFactory().getCode(), report.getDepartment().getCode(),
                    report.getProductionLine().getCode(), report.getTeam().getCode(),
                    report.getLeaderEmployee().getCode(), report.getMachine().getCode(),
                    report.getPlannedQuantity(), report.getActualQuantity(), report.getGoodQuantity(),
                    report.getDefectQuantity(), report.getWorkingMinutes(), report.getDowntimeMinutes(),
                    report.getNote()));
        }
        return rows;
    }

    private List<List<String>> downtimeRows(List<DailyCloseBatchItem> items) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(row("STAGING_ID", "DETAIL_ID", "MACHINE_CODE", "REASON_CODE", "START_TIME", "END_TIME",
                "DURATION_MINUTES", "DESCRIPTION"));
        for (DailyCloseBatchItem item : items) {
            Long reportId = item.getStagingReport().getId();
            downtimeRepository.findAllByProductionReportStaging_IdAndActiveTrue(reportId).stream()
                    .sorted(Comparator.comparing(MachineDowntimeStaging::getId))
                    .forEach(value -> rows.add(row(reportId, value.getId(), value.getMachine().getCode(),
                            value.getDowntimeReason().getCode(), value.getStartTime(), value.getEndTime(),
                            value.getDurationMinutes(), value.getDescription())));
        }
        return rows;
    }

    private List<List<String>> qualityMaterialRows(List<DailyCloseBatchItem> items) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(row("RECORD_TYPE", "STAGING_ID", "DETAIL_ID", "REFERENCE_CODE", "ISSUE_TYPE", "QUANTITY",
                "UNIT", "DESCRIPTION"));
        for (DailyCloseBatchItem item : items) {
            Long reportId = item.getStagingReport().getId();
            qualityRepository.findAllByProductionReportStaging_IdAndActiveTrue(reportId).stream()
                    .sorted(Comparator.comparing(QualityReportStaging::getId))
                    .forEach(value -> rows.add(row("QUALITY", reportId, value.getId(),
                            value.getQualityErrorType().getCode(), null, value.getQuantity(), null,
                            value.getDescription())));
            materialRepository.findAllByProductionReportStaging_IdAndActiveTrue(reportId).stream()
                    .sorted(Comparator.comparing(MaterialIssueStaging::getId))
                    .forEach(value -> rows.add(row("MATERIAL", reportId, value.getId(),
                            value.getMaterial().getCode(), value.getIssueType(), value.getQuantity(), value.getUnit(),
                            value.getDescription())));
        }
        return rows;
    }

    private List<List<String>> employeeRows(List<DailyCloseBatchItem> items) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(row("STAGING_ID", "DETAIL_ID", "EMPLOYEE_CODE", "WORKING_MINUTES", "OVERTIME_MINUTES",
                "ATTENDANCE_STATUS", "ASSIGNMENT_TYPE", "DESCRIPTION"));
        for (DailyCloseBatchItem item : items) {
            Long reportId = item.getStagingReport().getId();
            employeeRepository.findAllByProductionReportStaging_IdAndActiveTrue(reportId).stream()
                    .sorted(Comparator.comparing(EmployeeActualStaging::getId))
                    .forEach(value -> rows.add(row(reportId, value.getId(), value.getEmployee().getCode(),
                            value.getWorkingMinutes(), value.getOvertimeMinutes(), value.getAttendanceStatus(),
                            value.getAssignmentType(), value.getDescription())));
        }
        return rows;
    }

    private void writeSheet(Sheet sheet, List<List<String>> rows, CellStyle headerStyle) {
        int maxColumns = 0;
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex);
            List<String> values = rows.get(rowIndex);
            maxColumns = Math.max(maxColumns, values.size());
            for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                Cell cell = row.createCell(columnIndex, CellType.STRING);
                cell.setCellValue(values.get(columnIndex));
                if (rowIndex == 0) cell.setCellStyle(headerStyle);
            }
        }
        sheet.createFreezePane(0, 1);
        if (!rows.isEmpty() && maxColumns > 0) {
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, maxColumns - 1));
        }
        for (int column = 0; column < maxColumns; column++) {
            sheet.autoSizeColumn(column);
            sheet.setColumnWidth(column, Math.min(sheet.getColumnWidth(column) + 768, 60 * 256));
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void compareSheet(
            String sheetName,
            List<List<String>> expected,
            Sheet actual,
            List<ValidationError> errors
    ) {
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        int actualLastRow = lastNonBlankRow(actual, formatter);
        int expectedLastRow = expected.size() - 1;
        if (actualLastRow != expectedLastRow) {
            errors.add(new ValidationError(sheetName, null, null, "ROW_COUNT_MISMATCH",
                    "Số dòng không khớp file được hệ thống tạo",
                    "expected=" + (expectedLastRow + 1) + ", actual=" + (actualLastRow + 1)));
        }
        int rows = Math.max(expected.size(), actualLastRow + 1);
        for (int rowIndex = 0; rowIndex < rows && errors.size() < MAX_VALIDATION_ERRORS; rowIndex++) {
            List<String> expectedRow = rowIndex < expected.size() ? expected.get(rowIndex) : List.of();
            Row actualRow = actual.getRow(rowIndex);
            int actualColumns = lastNonBlankColumn(actualRow, formatter) + 1;
            int columns = Math.max(expectedRow.size(), actualColumns);
            for (int columnIndex = 0; columnIndex < columns && errors.size() < MAX_VALIDATION_ERRORS; columnIndex++) {
                String expectedValue = columnIndex < expectedRow.size() ? expectedRow.get(columnIndex) : "";
                Cell cell = actualRow == null ? null : actualRow.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String columnName = columnName(expected, columnIndex);
                if (cell != null && cell.getCellType() == CellType.FORMULA) {
                    errors.add(new ValidationError(sheetName, rowIndex + 1, columnName, "FORMULA_NOT_ALLOWED",
                            "Không cho phép công thức trong file nạp", cell.getCellFormula()));
                    continue;
                }
                String actualValue = cell == null ? "" : normalize(formatter.formatCellValue(cell));
                if (!normalize(expectedValue).equals(actualValue)) {
                    errors.add(new ValidationError(sheetName, rowIndex + 1, columnName, "CELL_VALUE_MISMATCH",
                            "Giá trị không khớp dữ liệu đã chốt", actualValue));
                }
            }
        }
    }

    private int lastNonBlankRow(Sheet sheet, DataFormatter formatter) {
        for (int index = sheet.getLastRowNum(); index >= 0; index--) {
            if (lastNonBlankColumn(sheet.getRow(index), formatter) >= 0) {
                return index;
            }
        }
        return -1;
    }

    private int lastNonBlankColumn(Row row, DataFormatter formatter) {
        if (row == null || row.getLastCellNum() < 0) {
            return -1;
        }
        for (int index = row.getLastCellNum() - 1; index >= 0; index--) {
            Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !normalize(formatter.formatCellValue(cell)).isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    private String columnName(List<List<String>> expected, int columnIndex) {
        if (!expected.isEmpty() && columnIndex < expected.get(0).size()) {
            return expected.get(0).get(columnIndex);
        }
        return "COLUMN_" + (columnIndex + 1);
    }

    private List<String> row(Object... values) {
        return Arrays.stream(values).map(this::text).toList();
    }

    private String text(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof java.time.LocalDate date) {
            return date.toString();
        }
        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return String.valueOf(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private record WorkbookModel(LinkedHashMap<String, List<List<String>>> sheets, int dataRowCount) {}
    public record WorkbookPayload(byte[] content, int dataRowCount) {}
    public record ValidationResult(List<ValidationError> errors, int dataRowCount) {
        public boolean valid() { return errors.isEmpty(); }
    }
    public record ValidationError(
            String sheetName,
            Integer rowNumber,
            String columnName,
            String errorCode,
            String message,
            String rawValue
    ) {}
}
