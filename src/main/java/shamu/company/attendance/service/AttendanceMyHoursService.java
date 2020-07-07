package shamu.company.attendance.service;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursListDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.PayDetailDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
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
  private final SimpleDateFormat dateFormat =
      new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);

  private final EmployeeTimeEntryService employeeTimeEntryService;

  private final EmployeeTimeLogRepository employeeTimeLogRepository;

  private final TimeSheetService timeSheetService;

  private final UserCompensationService userCompensationService;

  private final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  private final UserService userService;

  private final EmployeeTimeLogMapper employeeTimeLogMapper;

  private final AttendanceSettingsService attendanceSettingsService;

  private final TimeOffRequestService timeOffRequestService;

  public AttendanceMyHoursService(
      final EmployeeTimeEntryService employeeTimeEntryService,
      final EmployeeTimeLogRepository employeeTimeLogRepository,
      final TimeSheetService timeSheetService,
      final UserCompensationService userCompensationService,
      final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository,
      final UserService userService,
      final EmployeeTimeLogMapper employeeTimeLogMapper,
      final AttendanceSettingsService attendanceSettingsService,
      final TimeOffRequestService timeOffRequestService) {
    this.employeeTimeEntryService = employeeTimeEntryService;
    this.employeeTimeLogRepository = employeeTimeLogRepository;
    this.timeSheetService = timeSheetService;
    this.userCompensationService = userCompensationService;
    this.staticEmployeesTaTimeTypeRepository = staticEmployeesTaTimeTypeRepository;
    this.userService = userService;
    this.employeeTimeLogMapper = employeeTimeLogMapper;
    this.attendanceSettingsService = attendanceSettingsService;
    this.timeOffRequestService = timeOffRequestService;
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

  // My hour List contains entry list and entry list contains time log list
  public List<MyHoursListDto> findMyHoursLists(final String timeSheetId) {

    final List<EmployeeTimeEntry> employeeTimeEntries =
        employeeTimeEntryService.findEntriesById(timeSheetId);
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timeSheetId);
    final UserCompensation userCompensation =
        userCompensationService.findCompensationById(timeSheet.getUserCompensation().getId());

    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();
    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);

    List<MyHoursListDto> myHoursListDtos = new ArrayList<>();

    if (employeeTimeEntries.isEmpty()) {
      addDaysWithoutWork(myHoursListDtos, timeSheet);
      return myHoursListDtos;
    }

    final List<MyHoursEntryDto> myHoursEntryDtos =
        findTimeEntryLists(employeeTimeEntries, wagesPerMin);
    myHoursListDtos = setMyHoursList(myHoursEntryDtos);
    addDaysWithoutWork(myHoursListDtos, timeSheet);
    return myHoursListDtos;
  }

  private List<MyHoursEntryDto> findTimeEntryLists(
      final List<EmployeeTimeEntry> employeeTimeEntries, final double wagesPerMin) {

    return employeeTimeEntries.stream()
        .map(
            employeeTimeEntry -> {
              final List<EmployeeTimeLog> employeeTimeLogs =
                  employeeTimeLogRepository.findAllByEntryId(employeeTimeEntry.getId());
              employeeTimeLogs.sort(
                  (o1, o2) -> (int) (o1.getStart().getTime() - o2.getStart().getTime()));

              final List<PayDetailDto> payDetailDtos =
                  findTimeLogList(employeeTimeLogs, wagesPerMin);

              return employeeTimeLogMapper.convertToMyHoursEntryDto(
                  employeeTimeLogs.get(0).getStart(),
                  employeeTimeEntry.getComment(),
                  payDetailDtos);
            })
        .collect(Collectors.toList());
  }

  private List<PayDetailDto> findTimeLogList(
      final List<EmployeeTimeLog> employeeTimeLogs, final double wagesPerMin) {
    final SimpleDateFormat timeLogFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
    final List<PayDetailDto> payDetailDtos = new ArrayList<>();

    for (final EmployeeTimeLog employeeTimeLog : employeeTimeLogs) {
      final long endTimeMs =
          employeeTimeLog.getStart().getTime()
              + employeeTimeLog.getDurationMin() * CONVERT_MIN_TO_MS;
      final String endTime = timeLogFormat.format(DateUtil.longToTimestamp(endTimeMs));
      final String startTime = timeLogFormat.format(employeeTimeLog.getStart());
      final String timeRange = startTime + " - " + endTime;
      final int durationMin = employeeTimeLog.getDurationMin();
      final String minutes = durationMin % 60 == 0 ? "00" : String.valueOf((durationMin % 60));
      final String hours = durationMin / 60 + ":" + minutes;
      final DecimalFormat df = new DecimalFormat("0.00");
      df.setRoundingMode(RoundingMode.HALF_UP);
      final StaticEmployeesTaTimeType timeType = employeeTimeLog.getTimeType();
      final String pay;
      if (StaticEmployeesTaTimeType.TimeType.BREAK.name().equals(timeType.getName())) {
        pay = "Break";
      } else {
        pay = "$" + df.format(durationMin * wagesPerMin);
      }
      final PayDetailDto payDetailDto =
          employeeTimeLogMapper.convertToPayDetailDto(
              timeRange.replace("AM", "am").replace("PM", "pm"), hours, pay, timeType.getName());
      payDetailDtos.add(payDetailDto);
    }
    return payDetailDtos;
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

  private List<MyHoursListDto> setMyHoursList(final List<MyHoursEntryDto> myHoursEntryDtos) {
    myHoursEntryDtos.sort((o1, o2) -> (int) (o1.getDate().getTime() - o2.getDate().getTime()));
    final List<MyHoursListDto> myHoursListDtos = new ArrayList<>();
    final List<String> entryDate = new ArrayList<>();

    // One day contains two or more entries
    for (int i = 0; i <= myHoursEntryDtos.size() - 1; i++) {
      if (entryDate.contains(dateFormat.format(myHoursEntryDtos.get(i).getDate()))) {
        final List<MyHoursEntryDto> entryDtos =
            myHoursListDtos.get(myHoursListDtos.size() - 1).getMyHoursEntryDtos();
        entryDtos.add(myHoursEntryDtos.get(i));
        myHoursListDtos.get(myHoursListDtos.size() - 1).setMyHoursEntryDtos(entryDtos);
      } else {
        final MyHoursListDto myHoursListDto = new MyHoursListDto();
        myHoursListDto.setDate(dateFormat.format(myHoursEntryDtos.get(i).getDate()));
        final List<MyHoursEntryDto> myHoursEntryDtos1 = new ArrayList<>();
        myHoursEntryDtos1.add(myHoursEntryDtos.get(i));
        myHoursListDto.setMyHoursEntryDtos(myHoursEntryDtos1);
        myHoursListDtos.add(myHoursListDto);
        entryDate.add(dateFormat.format(myHoursEntryDtos.get(i).getDate()));
      }
    }
    return myHoursListDtos;
  }

  private void addDaysWithoutWork(
      final List<MyHoursListDto> myHoursListDtos, final TimeSheet timeSheet) {

    final List<String> listDates = new ArrayList<>();
    if (!CollectionUtils.isEmpty(myHoursListDtos)) {
      for (final MyHoursListDto myHoursListDto : myHoursListDtos) {
        listDates.add(myHoursListDto.getDate());
      }
    }

    Date tmp = timeSheet.getTimePeriod().getStartDate();
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(timeSheet.getTimePeriod().getStartDate());

    // Get days in one period
    final List<String> periodDates = new ArrayList<>();
    while (tmp.getTime() <= timeSheet.getTimePeriod().getEndDate().getTime()) {
      periodDates.add(dateFormat.format(tmp));
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      tmp = calendar.getTime();
    }

    // Add days without work
    for (final String periodDate : periodDates) {
      if (!listDates.contains(periodDate)) {
        final List<PayDetailDto> payDetailDtos = new ArrayList<>();
        final PayDetailDto payDetailDto =
            employeeTimeLogMapper.convertToPayDetailDto("", "–", "$0.00", "");
        payDetailDtos.add(payDetailDto);
        final List<MyHoursEntryDto> myHoursEntryDtos = new ArrayList<>();
        final MyHoursEntryDto myHoursEntryDto =
            employeeTimeLogMapper.convertToMyHoursEntryDto(null, "", payDetailDtos);
        myHoursEntryDtos.add(myHoursEntryDto);
        final MyHoursListDto myHoursListDto =
            employeeTimeLogMapper.convertToMyHoursListDto(periodDate, myHoursEntryDtos);
        myHoursListDtos.add(myHoursListDto);
      }
    }

    myHoursListDtos.sort(
        (o1, o2) ->
            (int) (DateUtil.stringToLong(o1.getDate()) - DateUtil.stringToLong(o2.getDate())));
  }

  public List<EmployeeTimeLog> findWorkEntriesBetweenDates(
      final long startDate, final long endDate, final String userId) {
    final List<EmployeeTimeLog> employeeTimeLogs =
        employeeTimeLogRepository.findEmployeeTimeLogByTime(startDate, endDate, userId);
    employeeTimeLogs.forEach(
        employeeTimeLog -> {
          final long timeStart = employeeTimeLog.getStart().getTime();
          final long timeEnd = timeStart + employeeTimeLog.getDurationMin() * CONVERT_MIN_TO_MS;
          final int durationMin = employeeTimeLog.getDurationMin();
          final long amountOfTimeBeforeStart =
              Math.max(startDate * CONVERT_SECOND_TO_MS - timeStart, 0);
          final long amountOfTimeAfterEnd = Math.max(timeEnd - endDate * CONVERT_SECOND_TO_MS, 0);
          employeeTimeLog.setStart(new Timestamp(timeStart + amountOfTimeBeforeStart));
          employeeTimeLog.setDurationMin(
              (int)
                  ((durationMin * CONVERT_MIN_TO_MS
                          - amountOfTimeAfterEnd
                          - amountOfTimeBeforeStart)
                      / (CONVERT_MIN_TO_MS)));
        });

    return employeeTimeLogs.stream()
        .filter(
            employeeTimeLog ->
                employeeTimeLog
                    .getTimeType()
                    .getName()
                    .equals(StaticEmployeesTaTimeType.TimeType.WORK.name()))
        .collect(Collectors.toList());
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
    final long endOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timesheetEnd, companyTaSetting.getTimeZone().getName());
    final int timeOffHours =
        timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
            user.getId(), startOfTimesheetWeek, endOfTimesheetWeek);

    final UserCompensation userCompensation = timeSheet.getUserCompensation();
    final boolean isHourly =
        userCompensation.getCompensationFrequency().getName().equals(HOUR_TYPE);
    final List<EmployeeTimeLog> allEmployeeEntries =
        findWorkEntriesBetweenDates(startOfTimesheetWeek, endOfTimesheetWeek, user.getId());
    final List<LocalDateEntryDto> localDateEntries =
        TimeEntryUtils.transformTimeLogsToLocalDate(
            allEmployeeEntries, companyTaSetting.getTimeZone());

    final List<OvertimeDetailDto> overtimeDetailDtos =
        OverTimePayFactory.getOverTimePay(timeSheet.getUserCompensation())
            .getOvertimePay(localDateEntries);
    int totalOvertimeHours = 0;
    for (final OvertimeDetailDto overtimeDetailDto : overtimeDetailDtos) {
      totalOvertimeHours += overtimeDetailDto.getTotalMinutes();
    }

    int paidMin = 0;
    if (isHourly) {
      for (final LocalDateEntryDto localDateEntryDto : localDateEntries) {
        paidMin += localDateEntryDto.getDuration();
      }
    } else {
      paidMin = totalOvertimeHours;
    }

    final BigInteger wageCents = userCompensation.getWageCents();
    final String compensationFrequency = userCompensation.getCompensationFrequency().getName();
    final double wagesPerMin = getWagesPerMin(compensationFrequency, wageCents);

    final DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    final String grossPay = df.format(paidMin * wagesPerMin);

    return AttendanceSummaryDto.builder()
        .overTimeHours(convertMinToTimeToDisplay(totalOvertimeHours))
        .timeOffHours(timeOffHours + ":00")
        .paidHours(convertMinToTimeToDisplay(paidMin))
        .grossPay(grossPay)
        .build();
  }

  String convertMinToTimeToDisplay(final int minutes) {
    final String minutesToDisplay = minutes % 60 == 0 ? "00" : String.valueOf((minutes % 60));
    return minutes / 60 + ":" + minutesToDisplay;
  }

  public CompensationDto findUserCompensation(final String userId) {
    return userCompensationService.findCompensationByUserId(userId);
  }
}
