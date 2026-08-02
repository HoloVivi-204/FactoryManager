package com.factory.management.modules.production.service;

import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.common.security.AuthorizationScope;
import com.factory.management.modules.audit.service.AuditService;
import com.factory.management.modules.hr.entity.AttendanceStatus;
import com.factory.management.modules.masterdata.entity.AssignmentType;
import com.factory.management.modules.masterdata.entity.Department;
import com.factory.management.modules.masterdata.entity.DowntimeReason;
import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.masterdata.entity.Factory;
import com.factory.management.modules.masterdata.entity.Machine;
import com.factory.management.modules.masterdata.entity.Material;
import com.factory.management.modules.masterdata.entity.ProductionLine;
import com.factory.management.modules.masterdata.entity.QualityErrorType;
import com.factory.management.modules.masterdata.entity.Shift;
import com.factory.management.modules.masterdata.entity.Team;
import com.factory.management.modules.masterdata.repository.DepartmentRepository;
import com.factory.management.modules.masterdata.repository.DowntimeReasonRepository;
import com.factory.management.modules.masterdata.repository.EmployeeRepository;
import com.factory.management.modules.masterdata.repository.FactoryRepository;
import com.factory.management.modules.masterdata.repository.MachineRepository;
import com.factory.management.modules.masterdata.repository.MaterialRepository;
import com.factory.management.modules.masterdata.repository.ProductionLineRepository;
import com.factory.management.modules.masterdata.repository.QualityErrorTypeRepository;
import com.factory.management.modules.masterdata.repository.ShiftRepository;
import com.factory.management.modules.masterdata.repository.TeamRepository;
import com.factory.management.modules.production.dto.response.StagingExcelImportErrorResponse;
import com.factory.management.modules.production.dto.response.StagingExcelImportResponse;
import com.factory.management.modules.production.entity.EmployeeActualStaging;
import com.factory.management.modules.production.entity.MachineDowntimeStaging;
import com.factory.management.modules.production.entity.MaterialIssueStaging;
import com.factory.management.modules.production.entity.MaterialIssueType;
import com.factory.management.modules.production.entity.ProductionReportStaging;
import com.factory.management.modules.production.entity.ProductionReportStatus;
import com.factory.management.modules.production.entity.QualityReportStaging;
import com.factory.management.modules.production.repository.EmployeeActualStagingRepository;
import com.factory.management.modules.production.repository.MachineDowntimeStagingRepository;
import com.factory.management.modules.production.repository.MaterialIssueStagingRepository;
import com.factory.management.modules.production.repository.ProductionReportStagingRepository;
import com.factory.management.modules.production.repository.QualityReportStagingRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StagingReportExcelService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_DATA_ROWS = 10_000;

    private static final String SAN_LUONG = "SAN_LUONG";
    private static final String NHAN_SU = "NHAN_SU";
    private static final String DUNG_MAY = "DUNG_MAY";
    private static final String CHAT_LUONG = "CHAT_LUONG";
    private static final String VAT_TU = "VAT_TU";

    private static final String[] REPORT_HEADERS = {
            "reportDate", "shiftCode", "factoryCode", "departmentCode", "productionLineCode",
            "teamCode", "leaderEmployeeCode", "machineCode", "plannedQuantity", "actualQuantity",
            "defectQuantity", "workingMinutes", "downtimeMinutes", "note"
    };
    private static final String[] EMPLOYEE_HEADERS = {
            "reportDate", "shiftCode", "teamCode", "machineCode", "employeeCode", "workingMinutes",
            "overtimeMinutes", "attendanceStatus", "assignmentType", "description"
    };
    private static final String[] DOWNTIME_HEADERS = {
            "reportDate", "shiftCode", "teamCode", "machineCode", "downtimeReasonCode",
            "startTime", "endTime", "description"
    };
    private static final String[] QUALITY_HEADERS = {
            "reportDate", "shiftCode", "teamCode", "machineCode", "qualityErrorTypeCode",
            "quantity", "description"
    };
    private static final String[] MATERIAL_HEADERS = {
            "reportDate", "shiftCode", "teamCode", "machineCode", "materialCode", "issueType",
            "quantity", "unit", "description"
    };

    private final ShiftRepository shiftRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final ProductionLineRepository productionLineRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final MachineRepository machineRepository;
    private final DowntimeReasonRepository downtimeReasonRepository;
    private final QualityErrorTypeRepository qualityErrorTypeRepository;
    private final MaterialRepository materialRepository;
    private final ProductionReportStagingRepository reportRepository;
    private final EmployeeActualStagingRepository employeeActualRepository;
    private final MachineDowntimeStagingRepository downtimeRepository;
    private final QualityReportStagingRepository qualityRepository;
    private final MaterialIssueStagingRepository materialIssueRepository;
    private final AuthorizationScope authorizationScope;
    private final AuditService auditService;

    public record ExcelFile(byte[] content, String fileName) {}

    @Transactional(readOnly = true)
    public ExcelFile template() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle headerStyle = headerStyle(workbook);
            createGuide(workbook, headerStyle);
            createDataSheet(workbook, SAN_LUONG, REPORT_HEADERS, headerStyle);
            createDataSheet(workbook, NHAN_SU, EMPLOYEE_HEADERS, headerStyle);
            createDataSheet(workbook, DUNG_MAY, DOWNTIME_HEADERS, headerStyle);
            createDataSheet(workbook, CHAT_LUONG, QUALITY_HEADERS, headerStyle);
            createDataSheet(workbook, VAT_TU, MATERIAL_HEADERS, headerStyle);
            createCatalogSheets(workbook, headerStyle);
            workbook.setActiveSheet(0);
            workbook.write(output);
            return new ExcelFile(output.toByteArray(), "mau-nhap-bao-cao-ca.xlsx");
        } catch (IOException ex) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional(readOnly = true)
    public StagingExcelImportResponse preview(MultipartFile file) {
        Inspection inspection = inspect(file);
        return response(file, inspection, false, new Counters());
    }

    @Transactional
    public StagingExcelImportResponse importWorkbook(MultipartFile file) {
        Inspection inspection = inspect(file);
        if (!inspection.errors.isEmpty()) return response(file, inspection, false, new Counters());

        Counters counters = persist(inspection);
        auditService.record("IMPORT_STAGING_EXCEL", "ProductionReportStaging", null,
                "{\"fileName\":\"" + json(file.getOriginalFilename()) + "\",\"totalRows\":"
                        + inspection.parsed.totalRows() + ",\"createdReports\":" + counters.createdReports
                        + ",\"updatedReports\":" + counters.updatedReports + "}");
        return response(file, inspection, true, counters);
    }

    private Inspection inspect(MultipartFile file) {
        validateFile(file);
        ParsedWorkbook parsed = parse(file);
        Inspection inspection = new Inspection(parsed);
        inspection.errors.addAll(parsed.errors);
        if (parsed.totalRows() == 0) {
            inspection.errors.add(error("WORKBOOK", 0, "", "NO_DATA",
                    "File chưa có dòng dữ liệu nào trong 5 sheet nghiệp vụ", ""));
        }
        if (parsed.totalRows() > MAX_DATA_ROWS) {
            inspection.errors.add(error("WORKBOOK", 0, "", "TOO_MANY_ROWS",
                    "Mỗi lần chỉ được nhập tối đa " + MAX_DATA_ROWS + " dòng", String.valueOf(parsed.totalRows())));
        }
        if (!inspection.errors.isEmpty()) return inspection;
        validateBusiness(inspection);
        return inspection;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.STAGING_EXCEL_EMPTY_FILE);
        if (file.getSize() > MAX_FILE_SIZE) throw new AppException(ErrorCode.STAGING_EXCEL_FILE_TOO_LARGE);
    }

    private ParsedWorkbook parse(MultipartFile file) {
        ParsedWorkbook parsed = new ParsedWorkbook();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter(Locale.US);
            parseReports(workbook, formatter, parsed);
            parseEmployees(workbook, formatter, parsed);
            parseDowntime(workbook, formatter, parsed);
            parseQuality(workbook, formatter, parsed);
            parseMaterials(workbook, formatter, parsed);
            return parsed;
        } catch (IOException | EncryptedDocumentException | IllegalArgumentException ex) {
            throw new AppException(ErrorCode.STAGING_EXCEL_INVALID_FORMAT);
        }
    }

    private void parseReports(Workbook workbook, DataFormatter formatter, ParsedWorkbook parsed) {
        Sheet sheet = validSheet(workbook, SAN_LUONG, REPORT_HEADERS, formatter, parsed.errors);
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (blank(row, REPORT_HEADERS.length, formatter)) continue;
            try {
                parsed.reports.add(new ReportRow(i + 1,
                        date(row, 0, formatter, "reportDate"), required(row, 1, formatter, "shiftCode"),
                        required(row, 2, formatter, "factoryCode"), required(row, 3, formatter, "departmentCode"),
                        required(row, 4, formatter, "productionLineCode"), required(row, 5, formatter, "teamCode"),
                        optional(row, 6, formatter), required(row, 7, formatter, "machineCode"),
                        longNumber(row, 8, formatter, "plannedQuantity"), longNumber(row, 9, formatter, "actualQuantity"),
                        longNumber(row, 10, formatter, "defectQuantity"), integer(row, 11, formatter, "workingMinutes"),
                        integer(row, 12, formatter, "downtimeMinutes"), optional(row, 13, formatter)));
            } catch (CellException ex) {
                parsed.errors.add(error(SAN_LUONG, i + 1, ex.column, "INVALID_VALUE", ex.getMessage(), ex.value));
            }
        }
    }

    private void parseEmployees(Workbook workbook, DataFormatter formatter, ParsedWorkbook parsed) {
        Sheet sheet = validSheet(workbook, NHAN_SU, EMPLOYEE_HEADERS, formatter, parsed.errors);
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (blank(row, EMPLOYEE_HEADERS.length, formatter)) continue;
            try {
                parsed.employees.add(new EmployeeRow(i + 1, base(row, formatter),
                        required(row, 4, formatter, "employeeCode"), integer(row, 5, formatter, "workingMinutes"),
                        integer(row, 6, formatter, "overtimeMinutes"), required(row, 7, formatter, "attendanceStatus"),
                        required(row, 8, formatter, "assignmentType"), optional(row, 9, formatter)));
            } catch (CellException ex) {
                parsed.errors.add(error(NHAN_SU, i + 1, ex.column, "INVALID_VALUE", ex.getMessage(), ex.value));
            }
        }
    }

    private void parseDowntime(Workbook workbook, DataFormatter formatter, ParsedWorkbook parsed) {
        Sheet sheet = validSheet(workbook, DUNG_MAY, DOWNTIME_HEADERS, formatter, parsed.errors);
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (blank(row, DOWNTIME_HEADERS.length, formatter)) continue;
            try {
                parsed.downtime.add(new DowntimeRow(i + 1, base(row, formatter),
                        required(row, 4, formatter, "downtimeReasonCode"),
                        dateTime(row, 5, formatter, "startTime"), dateTime(row, 6, formatter, "endTime"),
                        optional(row, 7, formatter)));
            } catch (CellException ex) {
                parsed.errors.add(error(DUNG_MAY, i + 1, ex.column, "INVALID_VALUE", ex.getMessage(), ex.value));
            }
        }
    }

    private void parseQuality(Workbook workbook, DataFormatter formatter, ParsedWorkbook parsed) {
        Sheet sheet = validSheet(workbook, CHAT_LUONG, QUALITY_HEADERS, formatter, parsed.errors);
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (blank(row, QUALITY_HEADERS.length, formatter)) continue;
            try {
                parsed.quality.add(new QualityRow(i + 1, base(row, formatter),
                        required(row, 4, formatter, "qualityErrorTypeCode"),
                        longNumber(row, 5, formatter, "quantity"), optional(row, 6, formatter)));
            } catch (CellException ex) {
                parsed.errors.add(error(CHAT_LUONG, i + 1, ex.column, "INVALID_VALUE", ex.getMessage(), ex.value));
            }
        }
    }

    private void parseMaterials(Workbook workbook, DataFormatter formatter, ParsedWorkbook parsed) {
        Sheet sheet = validSheet(workbook, VAT_TU, MATERIAL_HEADERS, formatter, parsed.errors);
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (blank(row, MATERIAL_HEADERS.length, formatter)) continue;
            try {
                parsed.materials.add(new MaterialRow(i + 1, base(row, formatter),
                        required(row, 4, formatter, "materialCode"), required(row, 5, formatter, "issueType"),
                        decimal(row, 6, formatter, "quantity"), optional(row, 7, formatter),
                        optional(row, 8, formatter)));
            } catch (CellException ex) {
                parsed.errors.add(error(VAT_TU, i + 1, ex.column, "INVALID_VALUE", ex.getMessage(), ex.value));
            }
        }
    }

    private BaseRow base(Row row, DataFormatter formatter) {
        return new BaseRow(date(row, 0, formatter, "reportDate"),
                required(row, 1, formatter, "shiftCode"), required(row, 2, formatter, "teamCode"),
                required(row, 3, formatter, "machineCode"));
    }

    private Sheet validSheet(Workbook workbook, String name, String[] headers, DataFormatter formatter,
                             List<StagingExcelImportErrorResponse> errors) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) return null;
        Row header = sheet.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            String actual = header == null ? "" : formatter.formatCellValue(header.getCell(i)).trim();
            if (!headers[i].equals(actual)) {
                errors.add(error(name, 1, headers[i], "INVALID_HEADER",
                        "Tên cột phải là '" + headers[i] + "'", actual));
            }
        }
        return errors.stream().anyMatch(value -> value.getSheet().equals(name) && value.getRow() == 1) ? null : sheet;
    }

    private void validateBusiness(Inspection inspection) {
        Context context = context();
        Set<ReportKey> reportDuplicates = new HashSet<>();

        for (ReportRow row : inspection.parsed.reports) {
            if (row.reportDate.isAfter(LocalDate.now())) add(inspection, SAN_LUONG, row.row, "reportDate",
                    "FUTURE_DATE", "Không được nhập báo cáo cho ngày tương lai", row.reportDate.toString());
            validateHeaderNumbers(row, inspection);

            Shift shift = lookup(
                    context.shifts,
                    row.shiftCode,
                    inspection,
                    SAN_LUONG,
                    row.row,
                    "shiftCode",
                    "Không tìm thấy ca đang hoạt động"
            );
            Factory factory = lookup(
                    context.factories,
                    row.factoryCode,
                    inspection,
                    SAN_LUONG,
                    row.row,
                    "factoryCode",
                    "Không tìm thấy nhà máy đang hoạt động"
            );
            Department department = factory == null ? null : lookup(context.departments,
                    path(row.factoryCode, row.departmentCode), inspection, SAN_LUONG, row.row, "departmentCode",
                    "Không tìm thấy phòng ban trong nhà máy đã chọn");
            ProductionLine line = department == null ? null : lookup(context.lines,
                    path(row.factoryCode, row.departmentCode, row.lineCode), inspection, SAN_LUONG, row.row,
                    "productionLineCode", "Không tìm thấy dây chuyền trong phòng ban đã chọn");
            Team team = line == null ? null : lookup(context.teams,
                    path(row.factoryCode, row.departmentCode, row.lineCode, row.teamCode), inspection, SAN_LUONG,
                    row.row, "teamCode", "Không tìm thấy tổ trong dây chuyền đã chọn");
            Machine machine = lookup(context.machines, row.machineCode, inspection, SAN_LUONG, row.row,
                    "machineCode", "Không tìm thấy máy đang hoạt động");
            Employee leader = row.leaderCode == null || row.leaderCode.isBlank()
                    ? team == null ? null : team.getLeader()
                    : lookup(context.employees, row.leaderCode, inspection, SAN_LUONG, row.row,
                    "leaderEmployeeCode", "Không tìm thấy nhân viên tổ trưởng đang hoạt động");

            if (team != null && !context.accessibleTeamIds.contains(team.getId())) {
                add(inspection, SAN_LUONG, row.row, "teamCode", "OUT_OF_SCOPE",
                        "Bạn không có quyền nhập dữ liệu cho tổ này", row.teamCode);
            }
            if (team != null && machine != null && !machine.getTeam().getId().equals(team.getId())) {
                add(inspection, SAN_LUONG, row.row, "machineCode", "MACHINE_TEAM_MISMATCH",
                        "Máy không thuộc tổ trong cùng dòng", row.machineCode);
            }
            if (team != null && leader == null) {
                add(inspection, SAN_LUONG, row.row, "leaderEmployeeCode", "LEADER_REQUIRED",
                        "Tổ chưa có tổ trưởng; hãy nhập mã nhân viên tổ trưởng", "");
            } else if (team != null && (leader.getTeam() == null || !leader.getTeam().getId().equals(team.getId()))) {
                add(inspection, SAN_LUONG, row.row, "leaderEmployeeCode", "LEADER_TEAM_MISMATCH",
                        "Tổ trưởng không thuộc tổ trong cùng dòng", row.leaderCode);
            }

            if (shift == null || factory == null || department == null || line == null || team == null
                    || machine == null || leader == null || hasRowError(inspection, SAN_LUONG, row.row)) continue;
            ReportKey key = new ReportKey(row.reportDate, shift.getId(), team.getId(), machine.getId());
            if (!reportDuplicates.add(key)) {
                add(inspection, SAN_LUONG, row.row, "reportDate", "DUPLICATE_REPORT",
                        "Báo cáo ngày/ca/tổ/máy bị lặp trong file", row.reportDate.toString());
                continue;
            }
            ProductionReportStaging existing = reportRepository
                    .findByReportDateAndShift_IdAndTeam_IdAndMachine_Id(row.reportDate, shift.getId(), team.getId(), machine.getId())
                    .orElse(null);
            if (existing != null && existing.getStatus() != ProductionReportStatus.DRAFT) {
                add(inspection, SAN_LUONG, row.row, "reportDate", "REPORT_NOT_DRAFT",
                        "Báo cáo đã gửi duyệt hoặc đã chốt nên không thể nhập đè", existing.getStatus().name());
                continue;
            }
            ResolvedReport resolved = new ResolvedReport(row, key, shift, factory, department, line, team, leader, machine, existing);
            inspection.reports.add(resolved);
            inspection.reportByKey.put(key, resolved);
        }

        validateEmployees(inspection, context);
        validateDowntime(inspection, context);
        validateQuality(inspection, context);
        validateMaterials(inspection, context);
    }

    private void validateHeaderNumbers(ReportRow row, Inspection inspection) {
        if (row.planned < 0) numberError(inspection, SAN_LUONG, row.row, "plannedQuantity", row.planned);
        if (row.actual < 0) numberError(inspection, SAN_LUONG, row.row, "actualQuantity", row.actual);
        if (row.defect < 0) numberError(inspection, SAN_LUONG, row.row, "defectQuantity", row.defect);
        if (row.working < 0) numberError(inspection, SAN_LUONG, row.row, "workingMinutes", row.working);
        if (row.downtime < 0) numberError(inspection, SAN_LUONG, row.row, "downtimeMinutes", row.downtime);
        if (row.defect > row.actual) add(inspection, SAN_LUONG, row.row, "defectQuantity", "DEFECT_EXCEEDS_ACTUAL",
                "Số lượng lỗi không được vượt sản lượng thực tế", String.valueOf(row.defect));
        if (row.downtime > row.working) add(inspection, SAN_LUONG, row.row, "downtimeMinutes", "DOWNTIME_EXCEEDS_WORKING",
                "Phút dừng không được vượt phút làm việc", String.valueOf(row.downtime));
    }

    private void validateEmployees(Inspection inspection, Context context) {
        Set<String> duplicates = new HashSet<>();
        for (EmployeeRow row : inspection.parsed.employees) {
            ResolvedBase base = resolveBase(row.base, NHAN_SU, row.row, inspection, context);
            Employee employee = lookup(context.employees, row.employeeCode, inspection, NHAN_SU, row.row,
                    "employeeCode", "Không tìm thấy nhân viên đang hoạt động");
            AttendanceStatus attendance = enumValue(AttendanceStatus.class, row.attendanceStatus, inspection,
                    NHAN_SU, row.row, "attendanceStatus");
            AssignmentType assignment = enumValue(AssignmentType.class, row.assignmentType, inspection,
                    NHAN_SU, row.row, "assignmentType");
            if (row.working < 0) numberError(inspection, NHAN_SU, row.row, "workingMinutes", row.working);
            if (row.overtime < 0) numberError(inspection, NHAN_SU, row.row, "overtimeMinutes", row.overtime);
            if (attendance == AttendanceStatus.ABSENT && row.working != 0) add(inspection, NHAN_SU, row.row,
                    "workingMinutes", "ABSENT_WORKING_MINUTES", "Nhân viên vắng mặt phải có workingMinutes = 0",
                    String.valueOf(row.working));
            if (base != null && employee != null && assignment == AssignmentType.NORMAL
                    && (employee.getTeam() == null || !employee.getTeam().getId().equals(base.team.getId()))) {
                add(inspection, NHAN_SU, row.row, "employeeCode", "EMPLOYEE_TEAM_MISMATCH",
                        "Nhân sự NORMAL phải thuộc đúng tổ; nhân sự tổ khác dùng SUPPORT hoặc TRANSFERRED", row.employeeCode);
            }
            if (base == null || employee == null || attendance == null || assignment == null
                    || hasRowError(inspection, NHAN_SU, row.row)) continue;
            String key = base.key + "|" + employee.getId();
            if (!duplicates.add(key)) {
                add(inspection, NHAN_SU, row.row, "employeeCode", "DUPLICATE_EMPLOYEE",
                        "Một nhân viên chỉ được xuất hiện một lần trong cùng báo cáo", row.employeeCode);
                continue;
            }
            inspection.employees.add(new ResolvedEmployee(row, base, employee, attendance, assignment));
        }
    }

    private void validateDowntime(Inspection inspection, Context context) {
        Set<String> duplicates = new HashSet<>();
        for (DowntimeRow row : inspection.parsed.downtime) {
            ResolvedBase base = resolveBase(row.base, DUNG_MAY, row.row, inspection, context);
            DowntimeReason reason = lookup(context.reasons, row.reasonCode, inspection, DUNG_MAY, row.row,
                    "downtimeReasonCode", "Không tìm thấy lý do dừng máy đang hoạt động");
            long minutes = ChronoUnit.MINUTES.between(row.startTime, row.endTime);
            if (minutes <= 0) add(inspection, DUNG_MAY, row.row, "endTime", "INVALID_TIME_RANGE",
                    "endTime phải lớn hơn startTime", row.endTime.toString());
            if (base != null && !row.startTime.toLocalDate().equals(base.key.reportDate)) {
                add(inspection, DUNG_MAY, row.row, "startTime", "DATE_MISMATCH",
                        "startTime phải thuộc ngày báo cáo", row.startTime.toString());
            }
            if (base != null && row.endTime.toLocalDate().isAfter(base.key.reportDate.plusDays(1))) {
                add(inspection, DUNG_MAY, row.row, "endTime", "TIME_RANGE_TOO_LONG",
                        "Một lần dừng máy không được kéo dài quá ngày kế tiếp", row.endTime.toString());
            }
            if (base == null || reason == null || minutes <= 0 || minutes > Integer.MAX_VALUE
                    || hasRowError(inspection, DUNG_MAY, row.row)) continue;
            String key = base.key + "|" + reason.getId() + "|" + row.startTime + "|" + row.endTime;
            if (!duplicates.add(key)) {
                add(inspection, DUNG_MAY, row.row, "startTime", "DUPLICATE_DOWNTIME",
                        "Lần dừng máy bị lặp trong file", row.startTime.toString());
                continue;
            }
            inspection.downtime.add(new ResolvedDowntime(row, base, reason, (int) minutes));
        }
    }

    private void validateQuality(Inspection inspection, Context context) {
        Set<String> duplicates = new HashSet<>();
        Map<ReportKey, List<ResolvedQuality>> byReport = new HashMap<>();
        for (QualityRow row : inspection.parsed.quality) {
            ResolvedBase base = resolveBase(row.base, CHAT_LUONG, row.row, inspection, context);
            QualityErrorType type = lookup(context.errorTypes, row.errorTypeCode, inspection, CHAT_LUONG, row.row,
                    "qualityErrorTypeCode", "Không tìm thấy loại lỗi chất lượng đang hoạt động");
            if (row.quantity <= 0) add(inspection, CHAT_LUONG, row.row, "quantity", "QUANTITY_NOT_POSITIVE",
                    "quantity phải lớn hơn 0", String.valueOf(row.quantity));
            if (base == null || type == null || row.quantity <= 0 || hasRowError(inspection, CHAT_LUONG, row.row)) continue;
            String key = base.key + "|" + type.getId();
            if (!duplicates.add(key)) {
                add(inspection, CHAT_LUONG, row.row, "qualityErrorTypeCode", "DUPLICATE_ERROR_TYPE",
                        "Một loại lỗi chỉ được xuất hiện một lần trong cùng báo cáo", row.errorTypeCode);
                continue;
            }
            ResolvedQuality resolved = new ResolvedQuality(row, base, type);
            inspection.quality.add(resolved);
            byReport.computeIfAbsent(base.key, ignored -> new ArrayList<>()).add(resolved);
        }
        byReport.forEach((key, rows) -> {
            ResolvedBase base = rows.get(0).base;
            long total = base.report.getId() == null ? 0 : qualityRepository.sumActiveQuantityByReportId(base.report.getId());
            for (ResolvedQuality row : rows) {
                if (base.report.getId() != null) {
                    total -= qualityRepository.findByProductionReportStaging_IdAndQualityErrorType_Id(
                                    base.report.getId(), row.type.getId())
                            .filter(item -> Boolean.TRUE.equals(item.getActive())).map(QualityReportStaging::getQuantity).orElse(0L);
                }
                total += row.row.quantity;
            }
            long defect = Optional.ofNullable(inspection.reportByKey.get(key))
                    .map(value -> value.row.defect).orElse(base.report.getDefectQuantity());
            if (total > defect) {
                for (ResolvedQuality row : rows) add(inspection, CHAT_LUONG, row.row.row, "quantity",
                        "QUALITY_TOTAL_EXCEEDS_DEFECT", "Tổng lỗi chi tiết sau khi nhập (" + total
                                + ") vượt defectQuantity của báo cáo (" + defect + ")", String.valueOf(row.row.quantity));
            }
        });
    }

    private void validateMaterials(Inspection inspection, Context context) {
        Set<String> duplicates = new HashSet<>();
        for (MaterialRow row : inspection.parsed.materials) {
            ResolvedBase base = resolveBase(row.base, VAT_TU, row.row, inspection, context);
            Material material = lookup(context.materials, row.materialCode, inspection, VAT_TU, row.row,
                    "materialCode", "Không tìm thấy vật tư đang hoạt động");
            MaterialIssueType type = enumValue(MaterialIssueType.class, row.issueType, inspection,
                    VAT_TU, row.row, "issueType");
            if (row.quantity.compareTo(BigDecimal.ZERO) <= 0) add(inspection, VAT_TU, row.row, "quantity",
                    "QUANTITY_NOT_POSITIVE", "quantity phải lớn hơn 0", row.quantity.toPlainString());
            if (base == null || material == null || type == null || row.quantity.compareTo(BigDecimal.ZERO) <= 0
                    || hasRowError(inspection, VAT_TU, row.row)) continue;
            String key = base.key + "|" + material.getId() + "|" + type;
            if (!duplicates.add(key)) {
                add(inspection, VAT_TU, row.row, "materialCode", "DUPLICATE_MATERIAL_ISSUE",
                        "Cùng vật tư và loại sự cố chỉ nên có một dòng trong một báo cáo", row.materialCode);
                continue;
            }
            inspection.materials.add(new ResolvedMaterial(row, base, material, type));
        }
    }

    private ResolvedBase resolveBase(BaseRow row, String sheet, int rowNumber, Inspection inspection, Context context) {
        if (row.reportDate.isAfter(LocalDate.now())) add(inspection, sheet, rowNumber, "reportDate", "FUTURE_DATE",
                "Không được nhập dữ liệu cho ngày tương lai", row.reportDate.toString());
        Shift shift = lookup(context.shifts, row.shiftCode, inspection, sheet, rowNumber,
                "shiftCode", "Không tìm thấy ca đang hoạt động");
        Machine machine = lookup(context.machines, row.machineCode, inspection, sheet, rowNumber,
                "machineCode", "Không tìm thấy máy đang hoạt động");
        if (shift == null || machine == null) return null;
        Team team = machine.getTeam();
        if (!team.getCode().equalsIgnoreCase(row.teamCode)) add(inspection, sheet, rowNumber, "teamCode",
                "MACHINE_TEAM_MISMATCH", "Mã tổ không khớp với tổ quản lý máy", row.teamCode);
        if (!context.accessibleTeamIds.contains(team.getId())) add(inspection, sheet, rowNumber, "teamCode",
                "OUT_OF_SCOPE", "Bạn không có quyền nhập dữ liệu cho tổ này", row.teamCode);
        ReportKey key = new ReportKey(row.reportDate, shift.getId(), team.getId(), machine.getId());
        ResolvedReport inFile = inspection.reportByKey.get(key);
        ProductionReportStaging report = inFile == null
                ? reportRepository.findByReportDateAndShift_IdAndTeam_IdAndMachine_Id(
                row.reportDate, shift.getId(), team.getId(), machine.getId()).orElse(null)
                : inFile.existing == null ? placeholder(inFile) : inFile.existing;
        if (report == null) add(inspection, sheet, rowNumber, "reportDate", "PARENT_REPORT_NOT_FOUND",
                "Không tìm thấy báo cáo Sản lượng tương ứng; hãy nhập sheet SAN_LUONG trước hoặc cùng file", row.reportDate.toString());
        else if (report.getId() != null && report.getStatus() != ProductionReportStatus.DRAFT)
            add(inspection, sheet, rowNumber, "reportDate", "REPORT_NOT_DRAFT",
                    "Chỉ được nhập chi tiết vào báo cáo DRAFT", report.getStatus().name());
        if (report == null || hasRowError(inspection, sheet, rowNumber)) return null;
        return new ResolvedBase(key, report, team, machine);
    }

    private ProductionReportStaging placeholder(ResolvedReport value) {
        return ProductionReportStaging.builder()
                .reportDate(value.row.reportDate).shift(value.shift).factory(value.factory).department(value.department)
                .productionLine(value.line).team(value.team).leaderEmployee(value.leader).machine(value.machine)
                .plannedQuantity(value.row.planned).actualQuantity(value.row.actual)
                .defectQuantity(value.row.defect).goodQuantity(value.row.actual - value.row.defect)
                .workingMinutes(value.row.working).downtimeMinutes(value.row.downtime)
                .note(trim(value.row.note)).status(ProductionReportStatus.DRAFT).build();
    }

    private Counters persist(Inspection inspection) {
        Counters counters = new Counters();
        Map<ReportKey, ProductionReportStaging> reports = new HashMap<>();
        for (ResolvedReport value : inspection.reports) {
            ProductionReportStaging report = value.existing == null ? new ProductionReportStaging() : value.existing;
            report.setReportDate(value.row.reportDate);
            report.setShift(value.shift);
            report.setFactory(value.factory);
            report.setDepartment(value.department);
            report.setProductionLine(value.line);
            report.setTeam(value.team);
            report.setLeaderEmployee(value.leader);
            report.setMachine(value.machine);
            report.setPlannedQuantity(value.row.planned);
            report.setActualQuantity(value.row.actual);
            report.setDefectQuantity(value.row.defect);
            report.setGoodQuantity(value.row.actual - value.row.defect);
            report.setWorkingMinutes(value.row.working);
            report.setDowntimeMinutes(value.row.downtime);
            report.setNote(trim(value.row.note));
            report.setStatus(ProductionReportStatus.DRAFT);
            report = reportRepository.save(report);
            reports.put(value.key, report);
            if (value.existing == null) counters.createdReports++; else counters.updatedReports++;
        }

        for (ResolvedEmployee value : inspection.employees) {
            ProductionReportStaging report = persistedReport(value.base, reports);
            EmployeeActualStaging entity = employeeActualRepository
                    .findByProductionReportStaging_IdAndEmployee_Id(report.getId(), value.employee.getId()).orElse(null);
            boolean created = entity == null;
            if (created) entity = new EmployeeActualStaging();
            entity.setProductionReportStaging(report);
            entity.setEmployee(value.employee);
            entity.setWorkingMinutes(value.row.working);
            entity.setOvertimeMinutes(value.row.overtime);
            entity.setAttendanceStatus(value.attendance);
            entity.setAssignmentType(value.assignment);
            entity.setDescription(trim(value.row.description));
            entity.setActive(true);
            employeeActualRepository.save(entity);
            counters.detail(created);
        }
        for (ResolvedDowntime value : inspection.downtime) {
            ProductionReportStaging report = persistedReport(value.base, reports);
            MachineDowntimeStaging entity = downtimeRepository
                    .findByProductionReportStaging_IdAndMachine_IdAndDowntimeReason_IdAndStartTimeAndEndTime(
                            report.getId(), value.base.machine.getId(), value.reason.getId(),
                            value.row.startTime, value.row.endTime).orElse(null);
            boolean created = entity == null;
            if (created) entity = new MachineDowntimeStaging();
            entity.setProductionReportStaging(report);
            entity.setMachine(value.base.machine);
            entity.setDowntimeReason(value.reason);
            entity.setStartTime(value.row.startTime);
            entity.setEndTime(value.row.endTime);
            entity.setDurationMinutes(value.minutes);
            entity.setDescription(trim(value.row.description));
            entity.setActive(true);
            downtimeRepository.save(entity);
            counters.detail(created);
        }
        for (ResolvedQuality value : inspection.quality) {
            ProductionReportStaging report = persistedReport(value.base, reports);
            QualityReportStaging entity = qualityRepository
                    .findByProductionReportStaging_IdAndQualityErrorType_Id(report.getId(), value.type.getId()).orElse(null);
            boolean created = entity == null;
            if (created) entity = new QualityReportStaging();
            entity.setProductionReportStaging(report);
            entity.setQualityErrorType(value.type);
            entity.setQuantity(value.row.quantity);
            entity.setDescription(trim(value.row.description));
            entity.setActive(true);
            qualityRepository.save(entity);
            counters.detail(created);
        }
        for (ResolvedMaterial value : inspection.materials) {
            ProductionReportStaging report = persistedReport(value.base, reports);
            MaterialIssueStaging entity = materialIssueRepository
                    .findFirstByProductionReportStaging_IdAndMaterial_IdAndIssueTypeOrderByIdAsc(
                            report.getId(), value.material.getId(), value.type).orElse(null);
            boolean created = entity == null;
            if (created) entity = new MaterialIssueStaging();
            entity.setProductionReportStaging(report);
            entity.setMaterial(value.material);
            entity.setIssueType(value.type);
            entity.setQuantity(value.row.quantity.setScale(3, RoundingMode.HALF_UP));
            entity.setUnit(value.row.unit == null || value.row.unit.isBlank() ? value.material.getUnit() : value.row.unit.trim());
            entity.setDescription(trim(value.row.description));
            entity.setActive(true);
            materialIssueRepository.save(entity);
            counters.detail(created);
        }
        reportRepository.flush();
        return counters;
    }

    private ProductionReportStaging persistedReport(ResolvedBase base, Map<ReportKey, ProductionReportStaging> imported) {
        return Optional.ofNullable(imported.get(base.key)).orElse(base.report);
    }

    private Context context() {
        Set<Long> accessible = authorizationScope.accessibleTeamIds();
        Map<String, Shift> shifts = activeMap(shiftRepository.findAll(), Shift::getCode, Shift::getActive);
        Map<String, Factory> factories = activeMap(factoryRepository.findAll(), Factory::getCode, Factory::getActive);
        Map<String, Department> departments = departmentRepository.findAll().stream()
                .filter(value -> Boolean.TRUE.equals(value.getActive()) && Boolean.TRUE.equals(value.getFactory().getActive()))
                .collect(Collectors.toMap(value -> path(value.getFactory().getCode(), value.getCode()), Function.identity(), (a, b) -> a));
        Map<String, ProductionLine> lines = productionLineRepository.findAll().stream()
                .filter(value -> Boolean.TRUE.equals(value.getActive()) && Boolean.TRUE.equals(value.getDepartment().getActive())
                        && Boolean.TRUE.equals(value.getDepartment().getFactory().getActive()))
                .collect(Collectors.toMap(value -> path(value.getDepartment().getFactory().getCode(),
                        value.getDepartment().getCode(), value.getCode()), Function.identity(), (a, b) -> a));
        Map<String, Team> teams = teamRepository.findAll().stream()
                .filter(value -> Boolean.TRUE.equals(value.getActive()) && Boolean.TRUE.equals(value.getProductionLine().getActive())
                        && Boolean.TRUE.equals(value.getProductionLine().getDepartment().getActive())
                        && Boolean.TRUE.equals(value.getProductionLine().getDepartment().getFactory().getActive()))
                .collect(Collectors.toMap(value -> path(value.getProductionLine().getDepartment().getFactory().getCode(),
                        value.getProductionLine().getDepartment().getCode(), value.getProductionLine().getCode(), value.getCode()),
                        Function.identity(), (a, b) -> a));
        return new Context(accessible, shifts, factories, departments, lines, teams,
                activeMap(employeeRepository.findAll(), Employee::getCode, Employee::getActive),
                activeMap(machineRepository.findAll(), Machine::getCode, Machine::getActive),
                activeMap(downtimeReasonRepository.findAll(), DowntimeReason::getCode, DowntimeReason::getActive),
                activeMap(qualityErrorTypeRepository.findAll(), QualityErrorType::getCode, QualityErrorType::getActive),
                activeMap(materialRepository.findAll(), Material::getCode, Material::getActive));
    }

    private <T> Map<String, T> activeMap(List<T> values, Function<T, String> code, Function<T, Boolean> active) {
        return values.stream().filter(value -> Boolean.TRUE.equals(active.apply(value)))
                .collect(Collectors.toMap(value -> norm(code.apply(value)), Function.identity(), (a, b) -> a));
    }

    private <T> T lookup(Map<String, T> values, String code, Inspection inspection, String sheet, int row,
                         String column, String message) {
        T value = values.get(norm(code));
        if (value == null) add(inspection, sheet, row, column, "MASTER_DATA_NOT_FOUND", message, code);
        return value;
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String raw, Inspection inspection,
                                             String sheet, int row, String column) {
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            add(inspection, sheet, row, column, "INVALID_ENUM",
                    "Giá trị hợp lệ: " + Arrays.toString(type.getEnumConstants()), raw);
            return null;
        }
    }

    private StagingExcelImportResponse response(MultipartFile file, Inspection inspection, boolean imported,
                                                Counters counters) {
        ParsedWorkbook parsed = inspection.parsed;
        return StagingExcelImportResponse.builder()
                .fileName(file.getOriginalFilename()).valid(inspection.errors.isEmpty()).imported(imported)
                .totalRows(parsed.totalRows()).productionRows(parsed.reports.size()).employeeRows(parsed.employees.size())
                .downtimeRows(parsed.downtime.size()).qualityRows(parsed.quality.size()).materialRows(parsed.materials.size())
                .createdReports(counters.createdReports).updatedReports(counters.updatedReports)
                .createdDetails(counters.createdDetails).updatedDetails(counters.updatedDetails)
                .errors(List.copyOf(inspection.errors)).build();
    }

    private void createGuide(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("HUONG_DAN");
        String[][] rows = {
                {"NHẬP DỮ LIỆU BÁO CÁO CA TỪ EXCEL"},
                {"1", "Không đổi tên sheet hoặc tên cột. Có thể chỉ điền sheet cần nhập."},
                {"2", "Dữ liệu chỉ được tạo/cập nhật báo cáo DRAFT, không tự gửi duyệt."},
                {"3", "Chi tiết Nhân sự/Dừng máy/Chất lượng/Vật tư phải có báo cáo SAN_LUONG tương ứng trong file hoặc đã tồn tại."},
                {"4", "Mã danh mục phải lấy từ các sheet DM_* trong file mẫu."},
                {"5", "Ngày: yyyy-MM-dd. Thời gian dừng: yyyy-MM-dd HH:mm."},
                {"6", "goodQuantity và durationMinutes do backend tự tính, không nhập vào Excel."},
                {"7", "Hãy bấm Kiểm tra file trên web trước khi bấm Nhập dữ liệu."},
                {"Ví dụ khóa báo cáo", "2026-07-12 | CA-SANG | TEAM-A | MCH001"},
                {"attendanceStatus", "PRESENT, ABSENT, LATE, LEAVE_EARLY, ON_LEAVE"},
                {"assignmentType", "NORMAL, TRANSFERRED, SUPPORT, OVERTIME"},
                {"issueType", "SHORTAGE, LATE_DELIVERY, WRONG_SPECIFICATION, DAMAGED, QUALITY_FAILED, OTHER"}
        };
        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < rows[i].length; j++) row.createCell(j).setCellValue(rows[i][j]);
        }
        sheet.getRow(0).getCell(0).setCellStyle(headerStyle);
        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 95 * 256);
    }

    private void createDataSheet(XSSFWorkbook workbook, String name, String[] headers, CellStyle style) {
        Sheet sheet = workbook.createSheet(name);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, Math.min(Math.max(headers[i].length() + 4, 16), 28) * 256);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
    }

    private void createCatalogSheets(XSSFWorkbook workbook, CellStyle style) {
        Set<Long> teamIds = authorizationScope.accessibleTeamIds();
        List<Team> teams = teamRepository.findAllActiveInActiveHierarchy()
                .stream().filter(team -> teamIds.contains(team.getId())).toList();
        Sheet scope = catalog(workbook, "DM_TO_MAY", style,
                "factoryCode", "departmentCode", "productionLineCode", "teamCode", "leaderEmployeeCode", "machineCode");
        int rowNumber = 1;
        for (Team team : teams) {
            List<Machine> machines = machineRepository.findAll().stream()
                    .filter(machine -> Boolean.TRUE.equals(machine.getActive()) && machine.getTeam().getId().equals(team.getId())).toList();
            if (machines.isEmpty()) write(scope, rowNumber++, team.getProductionLine().getDepartment().getFactory().getCode(),
                    team.getProductionLine().getDepartment().getCode(), team.getProductionLine().getCode(), team.getCode(),
                    team.getLeader() == null ? "" : team.getLeader().getCode(), "");
            for (Machine machine : machines) write(scope, rowNumber++,
                    team.getProductionLine().getDepartment().getFactory().getCode(),
                    team.getProductionLine().getDepartment().getCode(), team.getProductionLine().getCode(), team.getCode(),
                    team.getLeader() == null ? "" : team.getLeader().getCode(), machine.getCode());
        }
        Sheet shifts = catalog(workbook, "DM_CA", style, "shiftCode", "name", "startTime", "endTime");
        int i = 1;
        for (Shift value : shiftRepository.findAll()) if (Boolean.TRUE.equals(value.getActive()))
            write(shifts, i++, value.getCode(), value.getName(), value.getStartTime().toString(), value.getEndTime().toString());
        Sheet employees = catalog(workbook, "DM_NHAN_SU", style, "employeeCode", "fullName", "teamCode");
        i = 1;
        for (Employee value : employeeRepository.findAll()) if (Boolean.TRUE.equals(value.getActive())
                && value.getTeam() != null && teamIds.contains(value.getTeam().getId()))
            write(employees, i++, value.getCode(), value.getFullName(), value.getTeam().getCode());
        Sheet reasons = catalog(workbook, "DM_NGUYEN_NHAN", style, "downtimeReasonCode", "name", "reasonType");
        i = 1;
        for (DowntimeReason value : downtimeReasonRepository.findAll()) if (Boolean.TRUE.equals(value.getActive()))
            write(reasons, i++, value.getCode(), value.getName(), value.getReasonType().name());
        Sheet errors = catalog(workbook, "DM_LOAI_LOI", style, "qualityErrorTypeCode", "name", "severity");
        i = 1;
        for (QualityErrorType value : qualityErrorTypeRepository.findAll()) if (Boolean.TRUE.equals(value.getActive()))
            write(errors, i++, value.getCode(), value.getName(), value.getSeverity().name());
        Sheet materials = catalog(workbook, "DM_VAT_TU", style, "materialCode", "name", "unit");
        i = 1;
        for (Material value : materialRepository.findAll()) if (Boolean.TRUE.equals(value.getActive()))
            write(materials, i++, value.getCode(), value.getName(), value.getUnit());
    }

    private Sheet catalog(XSSFWorkbook workbook, String name, CellStyle style, String... headers) {
        Sheet sheet = workbook.createSheet(name);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 24 * 256);
        }
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    private void write(Sheet sheet, int rowNumber, String... values) {
        Row row = sheet.createRow(rowNumber);
        for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private boolean blank(Row row, int count, DataFormatter formatter) {
        if (row == null) return true;
        for (int i = 0; i < count; i++) if (!optional(row, i, formatter).isBlank()) return false;
        return true;
    }

    private String required(Row row, int index, DataFormatter formatter, String column) {
        String value = optional(row, index, formatter);
        if (value.isBlank()) throw new CellException(column, "Không được để trống", value);
        return value;
    }

    private String optional(Row row, int index, DataFormatter formatter) {
        Cell cell = row == null ? null : row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private Long longNumber(Row row, int index, DataFormatter formatter, String column) {
        BigDecimal value = number(row, index, formatter, column);
        try { return value.longValueExact(); }
        catch (ArithmeticException ex) { throw new CellException(column, "Phải là số nguyên", value.toPlainString()); }
    }

    private Integer integer(Row row, int index, DataFormatter formatter, String column) {
        BigDecimal value = number(row, index, formatter, column);
        try { return value.intValueExact(); }
        catch (ArithmeticException ex) { throw new CellException(column, "Phải là số nguyên trong giới hạn", value.toPlainString()); }
    }

    private BigDecimal decimal(Row row, int index, DataFormatter formatter, String column) {
        return number(row, index, formatter, column);
    }

    private BigDecimal number(Row row, int index, DataFormatter formatter, String column) {
        String raw = required(row, index, formatter, column).replace(",", "");
        try { return new BigDecimal(raw); }
        catch (NumberFormatException ex) { throw new CellException(column, "Không phải dữ liệu số hợp lệ", raw); }
    }

    private LocalDate date(Row row, int index, DataFormatter formatter, String column) {
        Cell cell = row.getCell(index);
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            return cell.getLocalDateTimeCellValue().toLocalDate();
        String raw = required(row, index, formatter, column);
        for (DateTimeFormatter format : List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try { return LocalDate.parse(raw, format); } catch (DateTimeParseException ignored) {}
        }
        throw new CellException(column, "Ngày phải theo yyyy-MM-dd", raw);
    }

    private LocalDateTime dateTime(Row row, int index, DataFormatter formatter, String column) {
        Cell cell = row.getCell(index);
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            return cell.getLocalDateTimeCellValue();
        String raw = required(row, index, formatter, column);
        for (DateTimeFormatter format : List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))) {
            try { return LocalDateTime.parse(raw, format); } catch (DateTimeParseException ignored) {}
        }
        throw new CellException(column, "Thời gian phải theo yyyy-MM-dd HH:mm", raw);
    }

    private void numberError(Inspection inspection, String sheet, int row, String column, Number value) {
        add(inspection, sheet, row, column, "NEGATIVE_NUMBER", "Giá trị không được âm", String.valueOf(value));
    }

    private void add(Inspection inspection, String sheet, int row, String column, String code,
                     String message, String value) {
        inspection.errors.add(error(sheet, row, column, code, message, value));
    }

    private StagingExcelImportErrorResponse error(String sheet, int row, String column, String code,
                                                   String message, String value) {
        return StagingExcelImportErrorResponse.builder().sheet(sheet).row(row).column(column).code(code)
                .message(message).value(value == null ? "" : value).build();
    }

    private boolean hasRowError(Inspection inspection, String sheet, int row) {
        return inspection.errors.stream().anyMatch(value -> value.getSheet().equals(sheet) && value.getRow() == row);
    }

    private String norm(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    private String path(String... values) { return Arrays.stream(values).map(this::norm).collect(Collectors.joining("|")); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String json(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static final class CellException extends RuntimeException {
        private final String column;
        private final String value;
        private CellException(String column, String message, String value) { super(message); this.column = column; this.value = value; }
    }

    private static final class ParsedWorkbook {
        private final List<ReportRow> reports = new ArrayList<>();
        private final List<EmployeeRow> employees = new ArrayList<>();
        private final List<DowntimeRow> downtime = new ArrayList<>();
        private final List<QualityRow> quality = new ArrayList<>();
        private final List<MaterialRow> materials = new ArrayList<>();
        private final List<StagingExcelImportErrorResponse> errors = new ArrayList<>();
        private int totalRows() { return reports.size() + employees.size() + downtime.size() + quality.size() + materials.size(); }
    }

    private static final class Inspection {
        private final ParsedWorkbook parsed;
        private final List<StagingExcelImportErrorResponse> errors = new ArrayList<>();
        private final List<ResolvedReport> reports = new ArrayList<>();
        private final List<ResolvedEmployee> employees = new ArrayList<>();
        private final List<ResolvedDowntime> downtime = new ArrayList<>();
        private final List<ResolvedQuality> quality = new ArrayList<>();
        private final List<ResolvedMaterial> materials = new ArrayList<>();
        private final Map<ReportKey, ResolvedReport> reportByKey = new HashMap<>();
        private Inspection(ParsedWorkbook parsed) { this.parsed = parsed; }
    }

    private static final class Counters {
        private int createdReports;
        private int updatedReports;
        private int createdDetails;
        private int updatedDetails;
        private void detail(boolean created) { if (created) createdDetails++; else updatedDetails++; }
    }

    private record BaseRow(LocalDate reportDate, String shiftCode, String teamCode, String machineCode) {}
    private record ReportRow(int row, LocalDate reportDate, String shiftCode, String factoryCode,
                             String departmentCode, String lineCode, String teamCode, String leaderCode,
                             String machineCode, long planned, long actual, long defect, int working,
                             int downtime, String note) {}
    private record EmployeeRow(int row, BaseRow base, String employeeCode, int working, int overtime,
                               String attendanceStatus, String assignmentType, String description) {}
    private record DowntimeRow(int row, BaseRow base, String reasonCode, LocalDateTime startTime,
                               LocalDateTime endTime, String description) {}
    private record QualityRow(int row, BaseRow base, String errorTypeCode, long quantity, String description) {}
    private record MaterialRow(int row, BaseRow base, String materialCode, String issueType,
                               BigDecimal quantity, String unit, String description) {}
    private record ReportKey(LocalDate reportDate, Long shiftId, Long teamId, Long machineId) {}

    private record ResolvedReport(ReportRow row, ReportKey key, Shift shift, Factory factory,
                                  Department department, ProductionLine line, Team team, Employee leader,
                                  Machine machine, ProductionReportStaging existing) {}
    private record ResolvedBase(ReportKey key, ProductionReportStaging report, Team team, Machine machine) {}
    private record ResolvedEmployee(EmployeeRow row, ResolvedBase base, Employee employee,
                                    AttendanceStatus attendance, AssignmentType assignment) {}
    private record ResolvedDowntime(DowntimeRow row, ResolvedBase base, DowntimeReason reason, int minutes) {}
    private record ResolvedQuality(QualityRow row, ResolvedBase base, QualityErrorType type) {}
    private record ResolvedMaterial(MaterialRow row, ResolvedBase base, Material material, MaterialIssueType type) {}

    private record Context(Set<Long> accessibleTeamIds, Map<String, Shift> shifts, Map<String, Factory> factories,
                           Map<String, Department> departments, Map<String, ProductionLine> lines,
                           Map<String, Team> teams, Map<String, Employee> employees, Map<String, Machine> machines,
                           Map<String, DowntimeReason> reasons, Map<String, QualityErrorType> errorTypes,
                           Map<String, Material> materials) {}
}
