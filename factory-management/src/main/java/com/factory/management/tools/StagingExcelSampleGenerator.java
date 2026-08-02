package com.factory.management.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates a ready-to-upload sample workbook for the staging report Excel API.
 * This is intentionally independent from Spring so it can be run as a small build utility.
 */
public final class StagingExcelSampleGenerator {

    private StagingExcelSampleGenerator() {}

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "MAU_NHAP_BAO_CAO_CA.xlsx" : args[0]).toAbsolutePath();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream stream = new FileOutputStream(output.toFile())) {
            CellStyle header = headerStyle(workbook);
            guide(workbook, header);

            Sheet production = dataSheet(workbook, "SAN_LUONG", header,
                    "reportDate", "shiftCode", "factoryCode", "departmentCode", "productionLineCode",
                    "teamCode", "leaderEmployeeCode", "machineCode", "plannedQuantity", "actualQuantity",
                    "defectQuantity", "workingMinutes", "downtimeMinutes", "note");
            row(production, 1, "2026-07-12", "CA-SANG", "BD", "BD-SX", "LINE-A", "TEAM-A1",
                    "TL-A1-001", "BD-CNC-01", 1100, 1000, 20, 480, 30,
                    "Dữ liệu mẫu ca sáng ngày 12/07/2026");

            Sheet employees = dataSheet(workbook, "NHAN_SU", header,
                    "reportDate", "shiftCode", "teamCode", "machineCode", "employeeCode", "workingMinutes",
                    "overtimeMinutes", "attendanceStatus", "assignmentType", "description");
            row(employees, 1, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "TL-A1-001",
                    480, 0, "PRESENT", "NORMAL", "Tổ trưởng làm đủ ca");
            row(employees, 2, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "EMP-A1-001",
                    480, 60, "PRESENT", "OVERTIME", "Tăng ca 60 phút");
            row(employees, 3, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "EMP-A1-002",
                    0, 0, "ABSENT", "NORMAL", "Vắng đột xuất");

            Sheet downtime = dataSheet(workbook, "DUNG_MAY", header,
                    "reportDate", "shiftCode", "teamCode", "machineCode", "downtimeReasonCode",
                    "startTime", "endTime", "description");
            row(downtime, 1, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "MACHINE_FAILURE",
                    "2026-07-12 08:15", "2026-07-12 08:45", "Máy dừng do lỗi trục chính");

            Sheet quality = dataSheet(workbook, "CHAT_LUONG", header,
                    "reportDate", "shiftCode", "teamCode", "machineCode", "qualityErrorTypeCode",
                    "quantity", "description");
            row(quality, 1, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "DIMENSION_ERROR",
                    12, "Sai kích thước sau gia công");
            row(quality, 2, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "SURFACE_SCRATCH",
                    8, "Trầy xước bề mặt khi chuyển công đoạn");

            Sheet materials = dataSheet(workbook, "VAT_TU", header,
                    "reportDate", "shiftCode", "teamCode", "machineCode", "materialCode", "issueType",
                    "quantity", "unit", "description");
            row(materials, 1, "2026-07-12", "CA-SANG", "TEAM-A1", "BD-CNC-01", "STEEL-304",
                    "SHORTAGE", 120.5, "KG", "Thiếu thép cho công đoạn gia công buổi sáng");

            catalogs(workbook, header);
            workbook.setActiveSheet(0);
            workbook.write(stream);
        }
        System.out.println(output);
    }

    private static void guide(XSSFWorkbook workbook, CellStyle header) {
        Sheet sheet = workbook.createSheet("HUONG_DAN");
        String[][] values = {
                {"FILE MẪU NHẬP BÁO CÁO CA", "Có sẵn dữ liệu thử ngày 2026-07-12 cho TEAM-A1"},
                {"Cách dùng", "Thay các mã bằng dữ liệu thực tế hoặc dùng nguyên mẫu sau khi đã tạo bộ dữ liệu trong README_TEST_DATA.md."},
                {"Bước 1", "Không đổi tên sheet và tên cột."},
                {"Bước 2", "Có thể xóa dữ liệu ở những sheet không muốn nhập."},
                {"Bước 3", "Trên web bấm Kiểm tra file trước, chỉ bấm Nhập khi file hợp lệ."},
                {"Ngày", "reportDate dùng yyyy-MM-dd; startTime/endTime dùng yyyy-MM-dd HH:mm."},
                {"Tự tính", "Không nhập goodQuantity và durationMinutes vì backend tự tính."},
                {"Trạng thái", "Dữ liệu chỉ được tạo/cập nhật ở trạng thái DRAFT, không tự duyệt."}
        };
        for (int i = 0; i < values.length; i++) row(sheet, i, (Object[]) values[i]);
        sheet.getRow(0).getCell(0).setCellStyle(header);
        sheet.getRow(0).getCell(1).setCellStyle(header);
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 100 * 256);
    }

    private static void catalogs(XSSFWorkbook workbook, CellStyle header) {
        Sheet scope = dataSheet(workbook, "DM_TO_MAY", header, "factoryCode", "departmentCode",
                "productionLineCode", "teamCode", "leaderEmployeeCode", "machineCode");
        row(scope, 1, "BD", "BD-SX", "LINE-A", "TEAM-A1", "TL-A1-001", "BD-CNC-01");
        row(scope, 2, "BD", "BD-SX", "LINE-A", "TEAM-A1", "TL-A1-001", "BD-CNC-02");
        row(scope, 3, "BD", "BD-SX", "LINE-A", "TEAM-A2", "TL-A2-001", "BD-WELD-01");
        row(scope, 4, "BD", "BD-SX", "LINE-B", "TEAM-B1", "TL-B1-001", "BD-PRESS-01");
        row(scope, 5, "DN", "DN-SX", "LINE-A", "TEAM-DN-A1", "TL-DN-A1-001", "DN-CNC-01");

        Sheet shifts = dataSheet(workbook, "DM_CA", header, "shiftCode", "name", "startTime", "endTime");
        row(shifts, 1, "CA-SANG", "Ca sáng", "06:00", "14:00");
        row(shifts, 2, "CA-CHIEU", "Ca chiều", "14:00", "22:00");
        row(shifts, 3, "CA-DEM", "Ca đêm", "22:00", "06:00");

        Sheet staff = dataSheet(workbook, "DM_NHAN_SU", header, "employeeCode", "fullName", "teamCode");
        row(staff, 1, "TL-A1-001", "Võ Thành Công", "TEAM-A1");
        row(staff, 2, "EMP-A1-001", "Nguyễn Văn An", "TEAM-A1");
        row(staff, 3, "EMP-A1-002", "Trần Văn Bình", "TEAM-A1");
        row(staff, 4, "TL-A2-001", "Lê Thị Thu Hà", "TEAM-A2");

        Sheet reasons = dataSheet(workbook, "DM_NGUYEN_NHAN", header,
                "downtimeReasonCode", "name", "reasonType");
        row(reasons, 1, "MACHINE_FAILURE", "Hỏng máy", "UNPLANNED");
        row(reasons, 2, "POWER_OUTAGE", "Mất điện", "UNPLANNED");
        row(reasons, 3, "MATERIAL_SHORTAGE", "Thiếu nguyên vật liệu", "UNPLANNED");
        row(reasons, 4, "PLANNED_MAINTENANCE", "Bảo trì định kỳ", "PLANNED");

        Sheet errors = dataSheet(workbook, "DM_LOAI_LOI", header,
                "qualityErrorTypeCode", "name", "severity");
        row(errors, 1, "DIMENSION_ERROR", "Sai kích thước", "HIGH");
        row(errors, 2, "SURFACE_SCRATCH", "Trầy xước bề mặt", "MEDIUM");
        row(errors, 3, "WELD_ERROR", "Lỗi mối hàn", "HIGH");
        row(errors, 4, "MATERIAL_DEFECT", "Lỗi nguyên vật liệu", "MEDIUM");

        Sheet materials = dataSheet(workbook, "DM_VAT_TU", header, "materialCode", "name", "unit");
        row(materials, 1, "STEEL-304", "Thép không gỉ 304", "KG");
        row(materials, 2, "CUTTING-OIL", "Dầu cắt gọt", "LIT");
        row(materials, 3, "PAINT-WHITE", "Sơn công nghiệp màu trắng", "LIT");
    }

    private static Sheet dataSheet(XSSFWorkbook workbook, String name, CellStyle header, String... columns) {
        Sheet sheet = workbook.createSheet(name);
        row(sheet, 0, (Object[]) columns);
        for (int i = 0; i < columns.length; i++) {
            sheet.getRow(0).getCell(i).setCellStyle(header);
            sheet.setColumnWidth(i, Math.min(Math.max(columns[i].length() + 5, 16), 34) * 256);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columns.length - 1));
        return sheet;
    }

    private static void row(Sheet sheet, int index, Object... values) {
        Row row = sheet.createRow(index);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number number) cell.setCellValue(number.doubleValue());
            else cell.setCellValue(value == null ? "" : String.valueOf(value));
        }
    }

    private static CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }
}
