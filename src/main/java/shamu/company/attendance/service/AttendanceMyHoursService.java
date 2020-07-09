package shamu.company.attendance.service;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursTimeLogDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
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
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OverTimePayFactory;
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
      final EmployeesTaSettingService employeesTaSettingService) {
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

  public List<EmployeeTimeLog> findEntriesBetweenDates(
      final long startDate, final long endDate, final String userId, final boolean isWorkEntries) {
    final List<EmployeeTimeLog> employeeTimeLogs =
        employeeTimeLogRepository.findEmployeeTimeLogByTime(
            startDate * CONVERT_SECOND_TO_MS, endDate, userId);
    employeeTimeLogs.forEach(
        employeeTimeLog -> {
          final long timeStart = employeeTimeLog.getStart().getTime();
          final long timeEnd = timeStart + employeeTimeLog.getDurationMin() * CONVERT_MIN_TO_MS;
          final int durationMin = employeeTimeLog.getDurationMin();
          final long amountOfTimeBeforeStart =
              Math.max(startDate * CONVERT_SECOND_TO_MS - timeStart, 0);
          final long amountOfTimeAfterEnd = Math.max(timeEnd - endDate, 0);
          employeeTimeLog.setStart(new Timestamp(timeStart + amountOfTimeBeforeStart));
          employeeTimeLog.setDurationMin(
              (int)
                  ((durationMin * CONVERT_MIN_TO_MS
                          - amountOfTimeAfterEnd
                          - amountOfTimeBeforeStart)
                      / (CONVERT_MIN_TO_MS)));
        });

    employeeTimeLogs.sort(TimeEntryUtils.compareByLogStartDate);

    if (isWorkEntries) {
      return employeeTimeLogs.stream()
          .filter(
              employeeTimeLog ->
                  employeeTimeLog
                      .getTimeType()
                      .getName()
                      .equals(StaticEmployeesTaTimeType.TimeType.WORK.name()))
          .collect(Collectors.toList());
    }

    return employeeTimeLogs;
  }

  public AttendanceSummaryDto findAttendanceSummary(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timeSheet.getEmployee();

    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(user.getCompany().getId());
    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final int timeOffHours =
        timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
            user.getId(), timeSheetStart.getTime(), timesheetEnd.getTime());

    final UserCompensation userCompensation = timeSheet.getUserCompensation();
    final boolean isHourly =
        userCompensation.getCompensationFrequency().getName().equals(HOUR_TYPE);
    final List<EmployeeTimeLog> allEmployeeEntries =
        findEntriesBetweenDates(startOfTimesheetWeek, timesheetEnd.getTime(), user.getId(), true);
    final List<LocalDateEntryDto> localDateEntries =
        TimeEntryUtils.transformTimeLogsToLocalDate(
            allEmployeeEntries, companyTaSetting.getTimeZone());

    final List<OvertimeDetailDto> overtimeDetailDtos =
        OverTimePayFactory.getOverTimePay(timeSheet.getUserCompensation())
            .getOvertimePay(localDateEntries);

    int totalOvertimeMin = 0;
    double grossPay = 0;
    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();
    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);

    for (final OvertimeDetailDto overtimeDetailDto : overtimeDetailDtos) {
      final ArrayList<OverTimeMinutesDto> overTimeMinutesDtos =
          overtimeDetailDto.getOverTimeMinutesDtos();
      for (final OverTimeMinutesDto overTimeMinutesDto : overTimeMinutesDtos) {
        final Timestamp timeLogStartTime = Timestamp.valueOf(overTimeMinutesDto.getStartTime());
        if (timeLogStartTime.getTime() <= timeSheetStart.getTime()) {
          final int overTimeMin =
              (int)
                      Math.max(
                          timeLogStartTime.getTime()
                              + overTimeMinutesDto.getMinutes() * CONVERT_MIN_TO_MS
                              - timeSheetStart.getTime(),
                          0)
                  / CONVERT_MIN_TO_MS;
          totalOvertimeMin += overTimeMin;
          grossPay += totalOvertimeMin * wagesPerMin * overTimeMinutesDto.getRate();
        } else {
          totalOvertimeMin += overTimeMinutesDto.getMinutes();
          grossPay += overTimeMinutesDto.getMinutes() * wagesPerMin * overTimeMinutesDto.getRate();
        }
      }
    }

    int paidMin = 0;
    if (isHourly) {
      for (final LocalDateEntryDto localDateEntryDto : localDateEntries) {
        final Timestamp timeLogStartTime = Timestamp.valueOf(localDateEntryDto.getStartTime());
        if (timeLogStartTime.getTime() <= timeSheetStart.getTime()) {
          final int overTimeMin =
              (int)
                      Math.max(
                          Timestamp.valueOf(localDateEntryDto.getEndTime()).getTime()
                              - timeSheetStart.getTime(),
                          0)
                  / CONVERT_MIN_TO_MS;
          paidMin += overTimeMin;
        } else {
          paidMin += localDateEntryDto.getDuration();
        }
      }
    } else {
      paidMin = totalOvertimeMin;
    }

    final DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    return AttendanceSummaryDto.builder()
        .overTimeHours(convertMinToTimeToDisplay(totalOvertimeMin))
        .timeOffHours(timeOffHours + ":00")
        .paidHours(convertMinToTimeToDisplay(paidMin))
        .grossPay(df.format(grossPay))
        .build();
  }

  String convertMinToTimeToDisplay(final int minutes) {
    final String minutesToDisplay = minutes % 60 == 0 ? "00" : String.valueOf((minutes % 60));
    return minutes / 60 + ":" + minutesToDisplay;
  }

  public CompensationDto findUserCompensation(final String userId) {
    return userCompensationService.findCompensationByUserId(userId);
  }

  public List<MyHoursEntryDto> findMyHoursEntries(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timeSheet.getEmployee();

    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final List<EmployeeTimeLog> allEmployeeTimeLogs =
        employeeTimeLogRepository.findEmployeeTimeLogByTime(
            timeSheetStart.getTime(), timesheetEnd.getTime(), user.getId());
    allEmployeeTimeLogs.sort(TimeEntryUtils.compareByLogStartDate);
    final UserCompensation userCompensation = timeSheet.getUserCompensation();
    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();

    final DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);
    final LinkedHashMap<String, MyHoursEntryDto> timeLogToEntries = new LinkedHashMap<>();
    for (final EmployeeTimeLog employeeTimeLog : allEmployeeTimeLogs) {
      final String entryId = employeeTimeLog.getEntry().getId();
      final String basePay = df.format(employeeTimeLog.getDurationMin() * wagesPerMin);
      final MyHoursTimeLogDto myHoursTimeLogDto =
          employeeTimeLogMapper.convertToMyHoursTimeLogDto(employeeTimeLog, basePay);
      if (timeLogToEntries.containsKey(entryId)) {
        timeLogToEntries.get(entryId).getMyHoursTimeLogDtos().add(myHoursTimeLogDto);
      } else {
        final MyHoursEntryDto myHoursEntryDto =
            employeeTimeLogMapper.convertToMyHoursEntryDto(
                employeeTimeLog, Collections.singletonList(myHoursTimeLogDto));
        timeLogToEntries.put(entryId, myHoursEntryDto);
      }
    }

    final List<MyHoursEntryDto> myHoursEntryDtos = new ArrayList<>();
    for (final Map.Entry<String, MyHoursEntryDto> myHoursEntryDtoEntry :
        timeLogToEntries.entrySet()) {
      myHoursEntryDtos.add(myHoursEntryDtoEntry.getValue());
    }

    return myHoursEntryDtos;
  }

  public String findUserTimeZone(final String timesheetId) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final User user = timeSheet.getEmployee();
    final EmployeesTaSetting employeesTaSetting =
        employeesTaSettingService.findByUserId(user.getId());
    return employeesTaSetting.getTimeZone().getName();
  }
}
