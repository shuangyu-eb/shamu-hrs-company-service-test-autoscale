package shamu.company.attendance.service;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
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
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursListDto;
import shamu.company.attendance.dto.PayDetailDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.EmployeeTimeLogMapper;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.common.exception.GeneralException;
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
  private static final String YEAR_TYPE = "Per Year";
  private static final String MONTH_TYPE = "Per Month";
  private static final String WEEK_TYPE = "Per Week";
  private static final String HOUR_TYPE = "Per Hour";
  private final SimpleDateFormat dateFormat =
      new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);
  private final EmployeeTimeEntryRepository employeeTimeEntryRepository;

  private final EmployeeTimeLogRepository employeeTimeLogRepository;

  private final TimeSheetService timeSheetService;

  private final UserCompensationService userCompensationService;

  private final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  private final UserService userService;

  private final EmployeeTimeLogMapper employeeTimeLogMapper;

  public AttendanceMyHoursService(
      final EmployeeTimeEntryRepository employeeTimeEntryRepository,
      final EmployeeTimeLogRepository employeeTimeLogRepository,
      final TimeSheetService timeSheetService,
      final UserCompensationService userCompensationService,
      final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository,
      final UserService userService,
      final EmployeeTimeLogMapper employeeTimeLogMapper) {
    this.employeeTimeEntryRepository = employeeTimeEntryRepository;
    this.employeeTimeLogRepository = employeeTimeLogRepository;
    this.timeSheetService = timeSheetService;
    this.userCompensationService = userCompensationService;
    this.staticEmployeesTaTimeTypeRepository = staticEmployeesTaTimeTypeRepository;
    this.userService = userService;
    this.employeeTimeLogMapper = employeeTimeLogMapper;
  }

  public void saveTimeEntry(final String userId, final TimeEntryDto timeEntryDto) {
    final User user = userService.findById(userId);
    final EmployeeTimeEntry employeeTimeEntry =
        EmployeeTimeEntry.builder().employee(user).comment(timeEntryDto.getComment()).build();
    final EmployeeTimeEntry savedEntry = employeeTimeEntryRepository.save(employeeTimeEntry);
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

      breakTimeLogDtos.sort(
          (o1, o2) -> (int) (o1.getBreakStart().getTime() - o2.getBreakStart().getTime()));
      // break time
      for (final BreakTimeLogDto breakTimeLogDto : breakTimeLogDtos) {
        final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
        employeeTimeLog.setStart(breakTimeLogDto.getBreakStart());
        employeeTimeLog.setTimeType(breakType);
        employeeTimeLog.setDurationMin(breakTimeLogDto.getBreakMin());
        employeeTimeLog.setEntry(savedEntry);
        employeeTimeLogs.add(employeeTimeLog);
      }

      // work time between break
      for (int i = 0; i < breakTimeLogDtos.size() - 1; i++) {
        final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
        final long workStartTimeMs =
            breakTimeLogDtos.get(i).getBreakStart().getTime()
                + breakTimeLogDtos.get(i).getBreakMin() * CONVERT_MIN_TO_MS;
        final Timestamp workStartTime = DateUtil.longToTimestamp(workStartTimeMs);
        final long durationMin =
            (breakTimeLogDtos.get(i + 1).getBreakStart().getTime() - workStartTime.getTime())
                / (CONVERT_MIN_TO_MS);
        employeeTimeLog.setStart(workStartTime);
        employeeTimeLog.setDurationMin((int) durationMin);
        employeeTimeLog.setTimeType(workType);
        employeeTimeLog.setEntry(savedEntry);
        employeeTimeLogs.add(employeeTimeLog);
      }

      // work time outside breaks

      // first work start time to first break time
      final EmployeeTimeLog startTimeLog = new EmployeeTimeLog();
      final long startDurationMin =
          (breakTimeLogDtos.get(0).getBreakStart().getTime()
                  - timeEntryDto.getStartTime().getTime())
              / (CONVERT_MIN_TO_MS);
      startTimeLog.setStart(timeEntryDto.getStartTime());
      startTimeLog.setDurationMin((int) startDurationMin);
      startTimeLog.setTimeType(workType);
      startTimeLog.setEntry(savedEntry);
      employeeTimeLogs.add(startTimeLog);

      // last work start time to end time
      final EmployeeTimeLog endTimeLog = new EmployeeTimeLog();
      final Timestamp lastBreakTime =
          breakTimeLogDtos.get(breakTimeLogDtos.size() - 1).getBreakStart();
      final int lastBreakDurationMin =
          breakTimeLogDtos.get(breakTimeLogDtos.size() - 1).getBreakMin();
      final long lastWorkStartTimeMs =
          lastBreakTime.getTime() + lastBreakDurationMin * CONVERT_MIN_TO_MS;
      final Timestamp lastWorkStartTime = DateUtil.longToTimestamp(lastWorkStartTimeMs);
      final long endDurationMin =
          (timeEntryDto.getEndTime().getTime() - lastWorkStartTime.getTime()) / (CONVERT_MIN_TO_MS);
      endTimeLog.setStart(lastWorkStartTime);
      endTimeLog.setDurationMin((int) endDurationMin);
      endTimeLog.setTimeType(workType);
      endTimeLog.setEntry(savedEntry);
      employeeTimeLogs.add(endTimeLog);
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
        employeeTimeEntryRepository.findAllByTimesheetId(timeSheetId);
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

  private double getWagesPerMin(final String compensationFrequency, final BigInteger wageCents) {
    double wagesPerMin = 0;
    switch (compensationFrequency) {
      case YEAR_TYPE:
        wagesPerMin = (float) wageCents.intValue() / CONVERT_YEAR_TO_MIN;
        break;
      case MONTH_TYPE:
        wagesPerMin = (float) wageCents.intValue() / CONVERT_MONTH_TO_MIN;
        break;
      case WEEK_TYPE:
        wagesPerMin = (float) wageCents.intValue() / CONVERT_WEEK_TO_MIN;
        break;
      case HOUR_TYPE:
        wagesPerMin = (float) wageCents.intValue() / CONVERT_HOUR_TO_MIN;
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

    Date tmp = timeSheet.getStartDate();
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(timeSheet.getStartDate());

    // Get days in one period
    final List<String> periodDates = new ArrayList<>();
    while (tmp.getTime() <= timeSheet.getEndDate().getTime()) {
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
        (o1, o2) -> {
          try {
            return (int)
                (new Timestamp(dateFormat.parse(o1.getDate()).getTime()).getTime()
                    - new Timestamp(dateFormat.parse(o2.getDate()).getTime()).getTime());
          } catch (final ParseException e) {
            throw new GeneralException("Unable to parse date.", e);
          }
        });
  }
}
