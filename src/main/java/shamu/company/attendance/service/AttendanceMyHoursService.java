package shamu.company.attendance.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AllTimeDto;
import shamu.company.attendance.dto.AllTimeEntryDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.EmployeeTimeLogMapper;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@Service
@Transactional
public class AttendanceMyHoursService {
  private static final int CONVERT_MIN_TO_MS = 60 * 1000;
  private static final int CONVERT_HOUR_TO_MIN = 60;
  private static final int CONVERT_WEEK_TO_MIN = 60 * 40;
  private static final int CONVERT_MONTH_TO_MIN = 60 * 40 * 52 / 12;
  private static final int CONVERT_YEAR_TO_MIN = 60 * 40 * 52;
  private static final int CONVERT_SECOND_TO_MS = 1000;
  private static final String YEAR_TYPE = "Per Year";
  private static final String MONTH_TYPE = "Per Month";
  private static final String WEEK_TYPE = "Per Week";
  private static final String HOUR_TYPE = "Per Hour";

  private final EmployeeTimeEntryService employeeTimeEntryService;

  private final EmployeeTimeLogRepository employeeTimeLogRepository;

  private final TimeSheetService timeSheetService;

  private final UserCompensationService userCompensationService;

  private final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  private final UserService userService;

  private final EmployeeTimeLogMapper employeeTimeLogMapper;

  private final AttendanceSettingsService attendanceSettingsService;

  private final TimeOffRequestService timeOffRequestService;

  private final EmployeesTaSettingService employeesTaSettingService;

  private final OvertimeService overtimeService;

  private final GenericHoursService genericHoursService;

  public AttendanceMyHoursService(
      final EmployeeTimeEntryService employeeTimeEntryService,
      final EmployeeTimeLogRepository employeeTimeLogRepository,
      final TimeSheetService timeSheetService,
      final UserCompensationService userCompensationService,
      final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository,
      final UserService userService,
      final EmployeeTimeLogMapper employeeTimeLogMapper,
      final AttendanceSettingsService attendanceSettingsService,
      final TimeOffRequestService timeOffRequestService,
      final EmployeesTaSettingService employeesTaSettingService,
      final OvertimeService overtimeService,
      final GenericHoursService genericHoursService) {
    this.employeeTimeEntryService = employeeTimeEntryService;
    this.employeeTimeLogRepository = employeeTimeLogRepository;
    this.timeSheetService = timeSheetService;
    this.userCompensationService = userCompensationService;
    this.staticEmployeesTaTimeTypeRepository = staticEmployeesTaTimeTypeRepository;
    this.userService = userService;
    this.employeeTimeLogMapper = employeeTimeLogMapper;
    this.attendanceSettingsService = attendanceSettingsService;
    this.timeOffRequestService = timeOffRequestService;
    this.employeesTaSettingService = employeesTaSettingService;
    this.overtimeService = overtimeService;
    this.genericHoursService = genericHoursService;
  }

  public void saveTimeEntry(final String userId, final TimeEntryDto timeEntryDto) {
    final User user = userService.findById(userId);
    final EmployeeTimeEntry employeeTimeEntry =
        EmployeeTimeEntry.builder().employee(user).comment(timeEntryDto.getComment()).build();
    final EmployeeTimeEntry savedEntry = employeeTimeEntryService.saveEntry(employeeTimeEntry);
    saveTimeLogs(timeEntryDto, savedEntry);
  }

  private void saveTimeLogs(final TimeEntryDto timeEntryDto, final EmployeeTimeEntry savedEntry) {

    final List<BreakTimeLogDto> breakTimeLogDtos = timeEntryDto.getBreakTimeLogs();

    final StaticEmployeesTaTimeType breakType =
        staticEmployeesTaTimeTypeRepository.findByName(
            StaticEmployeesTaTimeType.TimeType.BREAK.name());
    final StaticEmployeesTaTimeType workType =
        staticEmployeesTaTimeTypeRepository.findByName(
            StaticEmployeesTaTimeType.TimeType.WORK.name());

    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();

    if (!breakTimeLogDtos.isEmpty()) {

      Timestamp currentWorkStart = timeEntryDto.getStartTime();
      for (final BreakTimeLogDto breakTimeLogDto : breakTimeLogDtos) {
        final Timestamp breakStart = breakTimeLogDto.getBreakStart();
        final long workMin =
            (breakStart.getTime() - currentWorkStart.getTime()) / (CONVERT_MIN_TO_MS);
        final long breakMin = breakTimeLogDto.getBreakMin();
        final EmployeeTimeLog breakTimeLog =
            EmployeeTimeLog.builder()
                .start(breakStart)
                .timeType(breakType)
                .durationMin((int) breakMin)
                .entry(savedEntry)
                .build();
        final EmployeeTimeLog workTimeLog =
            EmployeeTimeLog.builder()
                .start(currentWorkStart)
                .timeType(workType)
                .durationMin((int) workMin)
                .entry(savedEntry)
                .build();
        employeeTimeLogs.add(breakTimeLog);
        employeeTimeLogs.add(workTimeLog);
        currentWorkStart =
            DateUtil.longToTimestamp(
                breakStart.getTime() + breakTimeLogDto.getBreakMin() * CONVERT_MIN_TO_MS);
      }
      final Timestamp workEnd = timeEntryDto.getEndTime();
      final long workMin = (workEnd.getTime() - currentWorkStart.getTime()) / (CONVERT_MIN_TO_MS);
      final EmployeeTimeLog workTimeLog =
          EmployeeTimeLog.builder()
              .start(currentWorkStart)
              .timeType(workType)
              .durationMin((int) workMin)
              .entry(savedEntry)
              .build();
      employeeTimeLogs.add(workTimeLog);
    }

    // do not have break
    if (breakTimeLogDtos.isEmpty()) {
      final EmployeeTimeLog timeLog = new EmployeeTimeLog();
      final int durationMin = timeEntryDto.getHoursWorked() * 60 + timeEntryDto.getMinutesWorked();
      timeLog.setStart(timeEntryDto.getStartTime());
      timeLog.setDurationMin(durationMin);
      timeLog.setEntry(savedEntry);
      timeLog.setTimeType(workType);
      employeeTimeLogs.add(timeLog);
    }

    employeeTimeLogRepository.saveAll(employeeTimeLogs);
  }

  public double getWagesPerMin(final String compensationFrequency, final BigInteger wageCents) {
    double wagesPerMin = 0;
    switch (compensationFrequency) {
      case YEAR_TYPE:
        wagesPerMin = (float) wageCents.intValue() / (CONVERT_YEAR_TO_MIN * 100);
        break;
      case MONTH_TYPE:
        wagesPerMin = (float) wageCents.intValue() / (CONVERT_MONTH_TO_MIN * 100);
        break;
      case WEEK_TYPE:
        wagesPerMin = (float) wageCents.intValue() / (CONVERT_WEEK_TO_MIN * 100);
        break;
      case HOUR_TYPE:
        wagesPerMin = (float) wageCents.intValue() / (CONVERT_HOUR_TO_MIN * 100);
        break;
      default:
        break;
    }
    return wagesPerMin;
  }

  public AttendanceSummaryDto findAttendanceSummary(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timeSheet.getEmployee();

    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final int timeOffHours =
        timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
            user.getId(), timeSheetStart.getTime(), timesheetEnd.getTime());

    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(user.getCompany().getId());
    final List<EmployeeTimeLog> workedMinutes =
        findAllRelevantTimelogs(timeSheet, companyTaSetting);

    final int workedMin = getTotalNumberOfWorkedMinutes(workedMinutes, timeSheet);

    final UserCompensation userCompensation = timeSheet.getUserCompensation();
    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();
    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);

    int totalOvertimeMin = 0;
    double overTimePay = 0;
    final Map<Double, Integer> overtimeMinutes =
        overtimeService.findAllOvertimeHours(workedMinutes, timeSheet, companyTaSetting);
    for (final Map.Entry<Double, Integer> overtimeMinute : overtimeMinutes.entrySet()) {
      totalOvertimeMin += overtimeMinute.getValue();
      overTimePay += overtimeMinute.getKey() * overtimeMinute.getValue() * wagesPerMin;
    }

    double ptoPay = 0;
    double regHourlyPay = 0;
    if (compensationFrequency.equals(HOUR_TYPE)) {
      ptoPay = timeOffHours * CONVERT_HOUR_TO_MIN * wagesPerMin;
      regHourlyPay = (workedMin - totalOvertimeMin) * wagesPerMin;
    }

    return AttendanceSummaryDto.builder()
        .overTimeMinutes(totalOvertimeMin)
        .workedMinutes(workedMin)
        .totalPtoMinutes(timeOffHours * CONVERT_HOUR_TO_MIN)
        .ptoPay(ptoPay)
        .regHourlyPay(regHourlyPay)
        .overTimePay(overTimePay)
        .build();
  }

  int getTotalNumberOfWorkedMinutes(
      final List<EmployeeTimeLog> workedMinutes, final TimeSheet timeSheet) {
    final long timeSheetStart = timeSheet.getTimePeriod().getStartDate().getTime();
    int workedMin = 0;
    for (final EmployeeTimeLog employeeTimeLog : workedMinutes) {
      final long timeLogStart = employeeTimeLog.getStart().getTime();
      final long timeLogEnd = timeLogStart + employeeTimeLog.getDurationMin() * CONVERT_MIN_TO_MS;

      if (timeLogStart <= timeSheetStart) {
        final int overTimeMin =
            (int) (Math.max(timeLogEnd - timeSheetStart, 0) / CONVERT_MIN_TO_MS);
        workedMin += overTimeMin;
      } else {
        workedMin += employeeTimeLog.getDurationMin();
      }
    }
    return workedMin;
  }

  final List<EmployeeTimeLog> findAllRelevantTimelogs(
      final TimeSheet timeSheet, final CompanyTaSetting companyTaSetting) {
    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final String userId = timeSheet.getEmployee().getId();
    return genericHoursService.findEntriesBetweenDates(
        startOfTimesheetWeek * CONVERT_SECOND_TO_MS, timesheetEnd.getTime(), userId, true);
  }

  public CompensationDto findUserCompensation(final String userId) {
    return userCompensationService.findCompensationByUserId(userId);
  }

  public List<AllTimeEntryDto> findAllHours(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(timeSheet.getEmployee().getCompany().getId());
    final List<LocalDateEntryDto> localDateEntries =
        overtimeService.getLocalDateEntries(timeSheet, companyTaSetting);
    final List<OvertimeDetailDto> overtimeDetailDtos =
        overtimeService.getOvertimeEntries(localDateEntries, timeSheet, companyTaSetting);
    final HashMap<String, OvertimeDetailDto> overtimeHours =
        convertOvertimeHours(overtimeDetailDtos);
    final List<EmployeeTimeLog> timesheetEntries =
        genericHoursService.findEntriesBetweenDates(
            timeSheet.getTimePeriod().getStartDate().getTime(),
            timeSheet.getTimePeriod().getEndDate().getTime(),
            timeSheet.getEmployee().getId(),
            false);
    final HashMap<String, AllTimeEntryDto> allTimeDtos = new HashMap<>();
    for (final EmployeeTimeLog employeeTimeLog : timesheetEntries) {
      final String entry = employeeTimeLog.getEntry().getId();
      final AllTimeDto allTimeDto =
          employeeTimeLogMapper.convertToTimeLogDto(
              employeeTimeLog, overtimeHours.getOrDefault(employeeTimeLog.getId(), null));
      if (allTimeDtos.containsKey(entry)) {
        allTimeDtos.get(entry).addAllTimeLogs(allTimeDto);
      } else {
        allTimeDtos.put(
            entry,
            new AllTimeEntryDto(
                entry,
                employeeTimeLog.getEntry().getComment(),
                new ArrayList<>(Collections.singletonList(allTimeDto))));
      }
    }
    return new ArrayList<>(allTimeDtos.values());
  }

  private HashMap<String, OvertimeDetailDto> convertOvertimeHours(
      final List<OvertimeDetailDto> overtimeDetailDtos) {
    final HashMap<String, OvertimeDetailDto> idToTimeLogDto = new HashMap<>();
    for (final OvertimeDetailDto overtimeDetailDto : overtimeDetailDtos) {
      if (idToTimeLogDto.containsKey(overtimeDetailDto.getTimeLogId())) {
        final OvertimeDetailDto currentTimeLog =
            idToTimeLogDto.get(overtimeDetailDto.getTimeLogId());
        currentTimeLog.addMinuteDtos(overtimeDetailDto.getOverTimeMinutesDtos());
        currentTimeLog.setTotalMinutes(
            currentTimeLog.getTotalMinutes() + overtimeDetailDto.getTotalMinutes());
      } else {
        idToTimeLogDto.putIfAbsent(overtimeDetailDto.getTimeLogId(), overtimeDetailDto);
      }
    }
    return idToTimeLogDto;
  }

  public String findUserTimeZone(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timeSheet.getEmployee();
    final EmployeesTaSetting employeesTaSetting =
        employeesTaSettingService.findByUserId(user.getId());
    return employeesTaSetting.getTimeZone().getName();
  }

  public String findCompensationFrequency(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    return timeSheet.getUserCompensation().getCompensationFrequency().getName();
  }
}
