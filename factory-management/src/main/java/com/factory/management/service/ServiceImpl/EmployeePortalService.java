package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.CreateLeaveRequest;
import com.factory.management.dto.response.EmployeePortalResponse;
import com.factory.management.entity.Employee;
import com.factory.management.entity.LeaveRequest;
import com.factory.management.entity.LeaveStatus;
import com.factory.management.entity.Notification;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.AttendanceRecordRepository;
import com.factory.management.repository.EmployeeKpiRepository;
import com.factory.management.repository.LeaveRequestRepository;
import com.factory.management.repository.NotificationRepository;
import com.factory.management.repository.OvertimeRequestRepository;
import com.factory.management.repository.WorkScheduleRepository;
import com.factory.management.service.Service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeePortalService {
    private final CurrentUserService current;
    private final WorkScheduleRepository schedules;
    private final AttendanceRecordRepository attendance;
    private final EmployeeKpiRepository kpis;
    private final LeaveRequestRepository leaves;
    private final OvertimeRequestRepository overtimeRequests;
    private final NotificationRepository notifications;

    @Transactional(readOnly = true)
    public EmployeePortalResponse dashboard(LocalDate from, LocalDate to) {
        Employee employee = current.employee();
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now().plusDays(14) : to;
        if (start.isAfter(end)) throw new AppException(ErrorCode.INVALID_PRODUCTION_REPORT_DATE_RANGE);
        var attendanceValues = attendance
                .findAllByEmployee_IdAndWorkDateBetweenOrderByWorkDateDesc(employee.getId(), start, end);

        return EmployeePortalResponse.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getCode())
                .employeeName(employee.getFullName())
                .position(employee.getPosition())
                .teamName(employee.getTeam() == null ? null : employee.getTeam().getName())
                .workingMinutes(attendanceValues.stream().mapToInt(value -> value.getWorkingMinutes()).sum())
                .overtimeMinutes(attendanceValues.stream().mapToInt(value -> value.getOvertimeMinutes()).sum())
                .unreadNotifications(notifications.countByRecipient_IdAndReadFalse(employee.getId()))
                .schedules(schedules
                        .findAllByEmployee_IdAndWorkDateBetweenAndActiveTrueOrderByWorkDate(
                                employee.getId(), start, end)
                        .stream().map(value -> EmployeePortalResponse.ScheduleItem.builder()
                                .id(value.getId()).workDate(value.getWorkDate())
                                .shiftCode(value.getShift().getCode()).shiftName(value.getShift().getName())
                                .startTime(value.getShift().getStartTime()).endTime(value.getShift().getEndTime())
                                .note(value.getNote()).build()).toList())
                .attendance(attendanceValues.stream().map(value -> EmployeePortalResponse.AttendanceItem.builder()
                        .id(value.getId()).workDate(value.getWorkDate()).checkIn(value.getCheckIn())
                        .checkOut(value.getCheckOut()).workingMinutes(value.getWorkingMinutes())
                        .overtimeMinutes(value.getOvertimeMinutes()).status(value.getAttendanceStatus()).build()).toList())
                .kpis(kpis.findAllByEmployee_IdOrderByPeriodEndDesc(employee.getId()).stream().limit(12)
                        .map(value -> EmployeePortalResponse.KpiItem.builder()
                                .id(value.getId()).periodStart(value.getPeriodStart()).periodEnd(value.getPeriodEnd())
                                .score(value.getScore()).productivityScore(value.getProductivityScore())
                                .qualityScore(value.getQualityScore()).attendanceScore(value.getAttendanceScore())
                                .note(value.getNote()).build()).toList())
                .leaves(leaves.findAllByEmployee_IdOrderByCreatedAtDesc(employee.getId()).stream()
                        .map(this::leaveResponse).toList())
                .overtimeRequests(overtimeRequests.findAllByEmployee_IdOrderByCreatedAtDesc(employee.getId()).stream()
                        .map(value -> EmployeePortalResponse.OvertimeItem.builder()
                                .id(value.getId()).workDate(value.getWorkDate())
                                .requestedMinutes(value.getRequestedMinutes()).reason(value.getReason())
                                .status(value.getStatus()).reviewComment(value.getReviewComment())
                                .reviewedAt(value.getReviewedAt()).createdAt(value.getCreatedAt()).build()).toList())
                .notifications(notifications.findAllByRecipient_IdOrderByCreatedAtDesc(employee.getId()).stream()
                        .limit(30).map(value -> EmployeePortalResponse.NotificationItem.builder()
                                .id(value.getId()).title(value.getTitle()).message(value.getMessage())
                                .severity(value.getSeverity()).actionUrl(value.getActionUrl())
                                .read(value.getRead()).readAt(value.getReadAt()).createdAt(value.getCreatedAt()).build())
                        .toList())
                .build();
    }

    @Transactional
    public EmployeePortalResponse.LeaveItem createLeave(CreateLeaveRequest request) {
        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new AppException(ErrorCode.INVALID_LEAVE_DATE_RANGE);
        }
        Employee employee = current.employee();
        boolean pendingOverlap = leaves
                .existsByEmployee_IdAndStatusAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        employee.getId(), LeaveStatus.PENDING, request.getToDate(), request.getFromDate());
        boolean approvedOverlap = leaves
                .existsByEmployee_IdAndStatusAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        employee.getId(), LeaveStatus.APPROVED, request.getToDate(), request.getFromDate());
        if (pendingOverlap || approvedOverlap) throw new AppException(ErrorCode.LEAVE_REQUEST_OVERLAP);
        LeaveRequest value = leaves.save(LeaveRequest.builder()
                .employee(employee)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .leaveType(request.getLeaveType().trim())
                .reason(request.getReason().trim())
                .build());
        return leaveResponse(value);
    }

    @Transactional
    public void markRead(Long id) {
        Employee employee = current.employee();
        Notification notification = notifications.findByIdAndRecipient_Id(id, employee.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
    }

    private EmployeePortalResponse.LeaveItem leaveResponse(LeaveRequest value) {
        return EmployeePortalResponse.LeaveItem.builder()
                .id(value.getId()).fromDate(value.getFromDate()).toDate(value.getToDate())
                .leaveType(value.getLeaveType()).reason(value.getReason()).status(value.getStatus())
                .reviewComment(value.getReviewComment()).reviewedAt(value.getReviewedAt())
                .createdAt(value.getCreatedAt()).build();
    }
}
