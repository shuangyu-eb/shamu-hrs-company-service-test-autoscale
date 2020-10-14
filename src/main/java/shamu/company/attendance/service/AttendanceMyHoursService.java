package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AllTimeDto;
import shamu.company.attendance.dto.AllTimeEntryDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.dto.UserAttendanceEnrollInfoDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.entity.mapper.EmployeeTimeLogMapper;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  private final OvertimeService overtimeService;

  private final GenericHoursService genericHoursService;

  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  private final EmployeesTaSettingService employeesTaSettingService;

  private final TimePeriodService timePeriodService;

  private final EmailService emailService;

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
      final OvertimeService overtimeService,
      final GenericHoursService genericHoursService,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository,
      final EmployeesTaSettingService employeesTaSettingService,
      final TimePeriodService timePeriodService,
      final EmailService emailService) {
    this.employeeTimeEntryService = employeeTimeEntryService;
    this.employeeTimeLogRepository = employeeTimeLogRepository;
    this.timeSheetService = timeSheetService;
    this.userCompensationService = userCompensationService;
    this.staticEmployeesTaTimeTypeRepository = staticEmployeesTaTimeTypeRepository;
    this.userService = userService;
    this.employeeTimeLogMapper = employeeTimeLogMapper;
    this.attendanceSettingsService = attendanceSettingsService;
    this.timeOffRequestService = timeOffRequestService;
    this.overtimeService = overtimeService;
    this.genericHoursService = genericHoursService;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
    this.employeesTaSettingService = employeesTaSettingService;
    this.timePeriodService = timePeriodService;
    this.emailService = emailService;
  }

  public void saveTimeEntry(
      final String employeeId, final TimeEntryDto timeEntryDto, final String userId) {

    final User employee = userService.findById(employeeId);
    final EmployeeTimeEntry employeeTimeEntry =
        EmployeeTimeEntry.builder().employee(employee).comment(timeEntryDto.getComment()).build();
    List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();
    if (timeEntryDto.getEntryId() != null) {
      employeeTimeLogs = employeeTimeLogRepository.findAllByEntryId(timeEntryDto.getEntryId());
      employeeTimeLogRepository.deleteInBatch(employeeTimeLogs);
    }
    final EmployeeTimeEntry savedEntry = employeeTimeEntryService.saveEntry(employeeTimeEntry);
    final List<EmployeeTimeLog> editedTimeLogs = saveTimeLogs(timeEntryDto, savedEntry);

    final User user = userService.findById(userId);
    sendHoursEditedByManagerEmail(
        user, employee, timeEntryDto.getStartTime(), employeeTimeLogs, editedTimeLogs);
  }

  private void sendHoursEditedByManagerEmail(
      final User fromUser,
      final User toUser,
      final Timestamp date,
      final List<EmployeeTimeLog> originalTimeLogs,
      final List<EmployeeTimeLog> editedTimeLogs) {
    final boolean editSelf = fromUser.getId().equals(toUser.getId());
    if (editSelf || areSameLogs(originalTimeLogs, editedTimeLogs)) {
      return;
    }

    final EmployeesTaSettingDto employeesSetting =
        attendanceSettingsService.findEmployeesSettings(toUser.getId());

    emailService.sendEmail(
        fromUser,
        toUser,
        EmailService.EmailTemplate.MY_HOUR_EDITED,
        emailService.getMyHourEditedEmailParameters(
            fromUser,
            date,
            employeesSetting.getTimeZone().getName(),
            originalTimeLogs,
            editedTimeLogs),
        new Timestamp(new Date().getTime()));
  }

  private boolean areSameLogs(
      final List<EmployeeTimeLog> listA, final List<EmployeeTimeLog> listB) {
    if (listA.size() != listB.size()) {
      return false;
    }

    return listA.stream().allMatch(listB::contains);
  }

  private List<EmployeeTimeLog> saveTimeLogs(
      final TimeEntryDto timeEntryDto, final EmployeeTimeEntry savedEntry) {

    final List<BreakTimeLogDto> breakTimeLogDtos = timeEntryDto.getBreakTimeLogs();

    breakTimeLogDtos.sort(TimeEntryUtils.compareByBreakStart);

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
      if (workMin > 0) {
        final EmployeeTimeLog workTimeLog =
            EmployeeTimeLog.builder()
                .start(currentWorkStart)
                .timeType(workType)
                .durationMin((int) workMin)
                .entry(savedEntry)
                .build();
        employeeTimeLogs.add(workTimeLog);
      }
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

    return employeeTimeLogRepository.saveAll(employeeTimeLogs);
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
    final Timesheet timesheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timesheet.getEmployee();

    final Timestamp timeSheetStart = timesheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timesheet.getTimePeriod().getEndDate();
    final int timeOffHours =
        timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
            user, timeSheetStart.getTime(), timesheetEnd.getTime());

    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    final List<EmployeeTimeLog> workedMinutes =
        findAllRelevantTimelogs(timesheet, companyTaSetting);

    final int workedMin = getTotalNumberOfWorkedMinutes(workedMinutes, timesheet);

    final UserCompensation userCompensation = timesheet.getUserCompensation();
    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();
    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);

    int totalOvertimeMin = 0;
    double overTimePay = 0;
    final Map<Double, Integer> overtimeMinutes =
        overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting);
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

  public int getTotalNumberOfWorkedMinutes(
      final List<EmployeeTimeLog> workedMinutes, final Timesheet timesheet) {
    final long timeSheetStart = timesheet.getTimePeriod().getStartDate().getTime();
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

  public List<EmployeeTimeLog> findAllRelevantTimelogs(
      final Timesheet timesheet, final CompanyTaSetting companyTaSetting) {
    final Timestamp timeSheetStart = timesheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timesheet.getTimePeriod().getEndDate();
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final String userId = timesheet.getEmployee().getId();
    return genericHoursService.findEntriesBetweenDates(
        startOfTimesheetWeek * CONVERT_SECOND_TO_MS, timesheetEnd.getTime(), userId, true);
  }

  public CompensationDto findUserCompensation(final String userId) {
    return userCompensationService.findCompensationByUserId(userId);
  }

  public List<AllTimeEntryDto> findAllHours(final String timesheetId) {
    final Timesheet timesheet = timeSheetService.findTimeSheetById(timesheetId);
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    final List<LocalDateEntryDto> localDateEntries =
        overtimeService.getLocalDateEntries(timesheet, companyTaSetting);
    final List<OvertimeDetailDto> overtimeDetailDtos =
        overtimeService.getOvertimeEntries(localDateEntries, timesheet, companyTaSetting);
    final HashMap<String, OvertimeDetailDto> overtimeHours =
        convertOvertimeHours(overtimeDetailDtos);
    final List<EmployeeTimeLog> timesheetEntries =
        genericHoursService.findEntriesBetweenDates(
            timesheet.getTimePeriod().getStartDate().getTime(),
            timesheet.getTimePeriod().getEndDate().getTime(),
            timesheet.getEmployee().getId(),
            false);
    final LinkedHashMap<String, AllTimeEntryDto> allTimeDtos = new LinkedHashMap<>();
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

  public String findUserTimeZone(final String userId) {
    final User user = userService.findById(userId);
    return user.getTimeZone().getName();
  }

  public TimeEntryDto findMyHourEntry(final String entryId) {
    final EmployeeTimeEntry employeeTimeEntry = employeeTimeEntryService.findById(entryId);
    final List<EmployeeTimeLog> employeeTimeLogs =
        employeeTimeLogRepository.findAllByEntryId(entryId);
    employeeTimeLogs.sort(TimeEntryUtils.compareByLogStartDate);
    final EmployeeTimeLog lastTimeLog = employeeTimeLogs.get(employeeTimeLogs.size() - 1);
    final Timestamp endTime =
        DateUtil.longToTimestamp(
            lastTimeLog.getStart().getTime() + lastTimeLog.getDurationMin() * CONVERT_MIN_TO_MS);
    final Timestamp startTime = employeeTimeLogs.get(0).getStart();
    int totalWorkMin = 0;
    final List<BreakTimeLogDto> breakTimeLogDtos = new ArrayList<>();
    for (final EmployeeTimeLog employeeTimeLog : employeeTimeLogs) {
      if (StaticEmployeesTaTimeType.TimeType.BREAK
          .name()
          .equals(employeeTimeLog.getTimeType().getName())) {
        final BreakTimeLogDto breakTimeLogDto =
            employeeTimeLogMapper.convertToBreakTimeLogDto(employeeTimeLog);
        breakTimeLogDtos.add(breakTimeLogDto);
      }
      if (StaticEmployeesTaTimeType.TimeType.WORK
          .name()
          .equals(employeeTimeLog.getTimeType().getName())) {
        totalWorkMin += employeeTimeLog.getDurationMin();
      }
    }
    return TimeEntryDto.builder()
        .entryId(entryId)
        .startTime(startTime)
        .endTime(endTime)
        .comment(employeeTimeEntry.getComment())
        .hoursWorked(totalWorkMin / 60)
        .minutesWorked(totalWorkMin % 60)
        .breakTimeLogs(breakTimeLogDtos)
        .build();
  }

  public void submitHoursForApproval(final String timesheetId) {
    final StaticTimesheetStatus staticTimesheetStatus =
        staticTimesheetStatusRepository.findByName(
            StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name());
    timeSheetService.updateTimesheetStatus(staticTimesheetStatus.getId(), timesheetId);
  }

  public String findTimesheetStatus(final String timesheetId) {
    return timeSheetService.findTimeSheetById(timesheetId).getStatus().getName();
  }

  public void deleteMyHourEntry(final String entryId, final String userId) {
    final EmployeeTimeEntry employeeTimeEntry = employeeTimeEntryService.findById(entryId);
    final List<EmployeeTimeLog> employeeTimeLogs =
        employeeTimeLogRepository.findAllByEntryId(entryId);
    final User user = userService.findById(userId);

    sendHoursEditedByManagerEmail(
        user,
        employeeTimeEntry.getEmployee(),
        employeeTimeLogs.get(0).getStart(),
        employeeTimeLogs,
        new ArrayList<>());
    employeeTimeEntryService.deleteMyHourEntry(entryId);
  }

  public UserAttendanceEnrollInfoDto findUserAttendanceEnrollInfo(final String userId) {
    final EmployeesTaSetting employeesTaSetting = employeesTaSettingService.findByUserId(userId);
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod();
    final Timesheet timesheet =
        timeSheetService.findActiveByPeriodAndUser(timePeriod.getId(), userId);
    return UserAttendanceEnrollInfoDto.builder()
        .isEnrolled(null != employeesTaSetting)
        .deactivatedAt(timesheet == null ? null : timesheet.getRemovedAt())
        .build();
  }
}
