package com.factory.management.tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Creates a date-range workbook by repeating the business rows of an existing staging workbook. */
public final class StagingExcelDateRangeGenerator {

    private static final List<String> BUSINESS_SHEETS = List.of(
            "SAN_LUONG", "NHAN_SU", "DUNG_MAY", "CHAT_LUONG", "VAT_TU"
    );

    private StagingExcelDateRangeGenerator() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: <input.xlsx> <output.xlsx> <fromDate> <toDate>");
        }
        Path input = Path.of(args[0]).toAbsolutePath();
        Path output = Path.of(args[1]).toAbsolutePath();
        LocalDate fromDate = LocalDate.parse(args[2]);
        LocalDate toDate = LocalDate.parse(args[3]);
        if (toDate.isBefore(fromDate)) throw new IllegalArgumentException("toDate must not be before fromDate");

        try (FileInputStream source = new FileInputStream(input.toFile());
             XSSFWorkbook workbook = new XSSFWorkbook(source)) {
            for (String sheetName : BUSINESS_SHEETS) {
                duplicateForDates(workbook, sheetName, fromDate, toDate);
            }
            Sheet guide = workbook.getSheet("HUONG_DAN");
            if (guide != null) {
                Row title = guide.getRow(0);
                if (title != null) {
                    Cell note = title.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    note.setCellValue("Dữ liệu mẫu từ " + fromDate + " đến " + toDate);
                }
            }
            try (FileOutputStream target = new FileOutputStream(output.toFile())) {
                workbook.write(target);
            }
        }
        System.out.println(output);
    }

    private static void duplicateForDates(XSSFWorkbook workbook, String sheetName,
                                          LocalDate fromDate, LocalDate toDate) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new IllegalArgumentException("Missing sheet: " + sheetName);

        List<List<CellData>> templates = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || row.getLastCellNum() <= 0) continue;
            List<CellData> values = new ArrayList<>();
            for (int column = 0; column < row.getLastCellNum(); column++) {
                Cell cell = row.getCell(column);
                values.add(CellData.from(cell));
            }
            templates.add(values);
        }
        if (templates.isEmpty()) throw new IllegalArgumentException("Sheet has no sample row: " + sheetName);

        for (int rowIndex = sheet.getLastRowNum(); rowIndex >= 1; rowIndex--) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) sheet.removeRow(row);
        }

        int targetRow = 1;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            for (List<CellData> template : templates) {
                Row row = sheet.createRow(targetRow++);
                for (int column = 0; column < template.size(); column++) {
                    CellData value = template.get(column);
                    Cell cell = row.createCell(column);
                    value.write(cell, workbook);
                    if (column == 0) cell.setCellValue(date.toString());
                    if (sheetName.equals("DUNG_MAY") && (column == 5 || column == 6)) {
                        String original = value.textValue();
                        String time = original.length() >= 16 ? original.substring(original.length() - 5) : "08:15";
                        cell.setCellValue(date + " " + time);
                    }
                }
            }
        }
    }

    private record CellData(CellType type, String text, double number, short styleIndex) {
        private static CellData from(Cell cell) {
            if (cell == null) return new CellData(CellType.BLANK, "", 0, (short) 0);
            if (cell.getCellType() == CellType.NUMERIC) {
                return new CellData(CellType.NUMERIC, "", cell.getNumericCellValue(), cell.getCellStyle().getIndex());
            }
            return new CellData(cell.getCellType(), cell.toString(), 0, cell.getCellStyle().getIndex());
        }

        private void write(Cell cell, XSSFWorkbook workbook) {
            if (styleIndex >= 0) {
                CellStyle style = workbook.getCellStyleAt(styleIndex);
                cell.setCellStyle(style);
            }
            if (type == CellType.NUMERIC) cell.setCellValue(number);
            else cell.setCellValue(text);
        }

        private String textValue() {
            return type == CellType.NUMERIC ? String.valueOf(number) : text;
        }
    }
}
