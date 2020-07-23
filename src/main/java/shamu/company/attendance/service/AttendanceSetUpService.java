package shamu.company.attendance.service;

import java.util.Optional;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserListDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType.PayFrequencyType;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.scheduler.job.ActivateTimeSheetJob;
import shamu.company.scheduler.job.AddPayPeriodJob;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

@Service
public class AttendanceSetUpService {

  private static final int SUNDAY_DAY_OF_WEEK = 1;
  private static final int SATURDAY_DAY_OF_WEEK = 7;
  private static final int FRIDAY_DAY_OF_WEEK = 6;
  private static final int WEEKS_OF_WEEKLY = 1;
  private static final int WEEKS_OF_BIWEEKLY = 2;
  private static final int MID_DAY_OF_MONTH = 15;
  private static final int PAY_DATE_AFTER_PERIOD_DAYS = 7;
  private static final long MS_OF_ONE_DAY = 24 * 60 * 60 * 1000l;

  private final AttendanceSettingsService attendanceSettingsService;

  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  private final UserRepository userRepository;

  private final JobUserRepository jobUserRepository;

  private final JobUserMapper jobUserMapper;

  private final CompanyRepository companyRepository;

  private final CompensationFrequencyRepository compensationFrequencyRepository;

  private final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  private final UserCompensationService userCompensationService;

  private final UserService userService;

  private final TimePeriodService timePeriodService;

  private final PaidHolidayService paidHolidayService;

  private final TimeSheetService timeSheetService;

  private final QuartzJobScheduler quartzJobScheduler;

  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  private final UserCompensationMapper userCompensationMapper;

  private final PayPeriodFrequencyService payPeriodFrequencyService;

  public AttendanceSetUpService(
      final AttendanceSettingsService attendanceSettingsService,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final UserRepository userRepository,
      final JobUserRepository jobUserRepository,
      final JobUserMapper jobUserMapper,
      final CompanyRepository companyRepository,
      final CompensationFrequencyRepository compensationFrequencyRepository,
      final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository,
      final UserCompensationService userCompensationService,
      final UserService userService,
      final TimePeriodService timePeriodService,
      final PaidHolidayService paidHolidayService,
      final TimeSheetService timeSheetService,
      final QuartzJobScheduler quartzJobScheduler,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository,
      final UserCompensationMapper userCompensationMapper,
      final PayPeriodFrequencyService payPeriodFrequencyService) {
    this.attendanceSettingsService = attendanceSettingsService;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.jobUserMapper = jobUserMapper;
    this.companyRepository = companyRepository;
    this.compensationFrequencyRepository = compensationFrequencyRepository;
    this.compensationOvertimeStatusRepository = compensationOvertimeStatusRepository;
    this.userCompensationService = userCompensationService;
    this.userService = userService;
    this.timePeriodService = timePeriodService;
    this.paidHolidayService = paidHolidayService;
    this.timeSheetService = timeSheetService;
    this.quartzJobScheduler = quartzJobScheduler;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
    this.userCompensationMapper = userCompensationMapper;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
  }

  public Boolean findIsAttendanceSetUp(final String companyId) {
    return attendanceSettingsService.existsByCompanyId(companyId);
  }

  public TimeAndAttendanceRelatedUserListDto getRelatedUsers(final String companyId) {
    // The logic of unSelectedUsers should be modified.
    final List<EmployeesTaSetting> timeAndAttendanceUsers = employeesTaSettingRepository.findAll();
    final List<User> allUsers = userRepository.findAllByCompanyId(companyId);

    final List<String> selectedUsersIds = new ArrayList<>();
    final List<TimeAndAttendanceRelatedUserDto> selectedEmployees =
        timeAndAttendanceUsers.stream()
            .map(
                timeAndAttendanceUser -> {
                  final User user = timeAndAttendanceUser.getEmployee();
                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
                  selectedUsersIds.add(user.getId());
                  final String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, allUsers);
                  return jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
                      user, employeeWithJobInfo, userNameOrUserNameWithEmailAddress);
                })
            .collect(Collectors.toList());

    final List<User> unSelectedUsers =
        selectedUsersIds.isEmpty()
            ? allUsers
            : userRepository.findAllByCompanyIdAndIdNotIn(companyId, selectedUsersIds);
    final List<TimeAndAttendanceRelatedUserDto> unSelectedEmployees =
        unSelectedUsers.stream()
            .map(
                user -> {
                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
                  final String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, allUsers);
                  return jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
                      user, employeeWithJobInfo, userNameOrUserNameWithEmailAddress);
                })
            .collect(Collectors.toList());

    return new TimeAndAttendanceRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  @Transactional
  public void saveAttendanceDetails(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto, final String companyId) {
    saveCompanyTaSetting(timeAndAttendanceDetailsDto, companyId);

    final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList =
        timeAndAttendanceDetailsDto.getOvertimeDetails();

    final List<UserCompensation> userCompensationList =
        saveUserCompensations(overtimeDetailsDtoList);
    saveJobUsers(overtimeDetailsDtoList);

    final Date periodStartDate = getStartOfDay(timeAndAttendanceDetailsDto.getPeriodStartDate());
    final Date periodEndDate = timeAndAttendanceDetailsDto.getPeriodEndDate();

    final TimePeriod firstTimePeriod = new TimePeriod(periodStartDate, periodEndDate);

    final TimeSheetStatus timeSheetStatus;
    if (periodStartDate.after(getEndOfDay(new Date()))) {
      timeSheetStatus = TimeSheetStatus.NOT_YET_START;
      scheduleActivateTimeSheet(companyId, periodStartDate);
    } else {
      timeSheetStatus = TimeSheetStatus.ACTIVE;
    }

    createTimeSheetsAndPeriod(firstTimePeriod, timeSheetStatus, userCompensationList);

    scheduleCreateNextPeriod(companyId, new Date(periodEndDate.getTime()));
  }

  public void scheduleCreateNextPeriod(final String companyId, final Date currentPeriodEndDate) {
    final Date executeDate = getEndOfDay(currentPeriodEndDate);
    final Map<String, Object> jobParameter = assembleCompanyIdParameter(companyId);
    quartzJobScheduler.addOrUpdateJobSchedule(
        AddPayPeriodJob.class,
        "new_period_" + companyId + "_" + executeDate.getTime(),
        jobParameter,
        executeDate);
  }

  private void scheduleActivateTimeSheet(final String companyId, final Date periodStartDate) {
    final Date executeDate = getStartOfDay(periodStartDate);
    final Map<String, Object> jobParameter = assembleCompanyIdParameter(companyId);
    quartzJobScheduler.addOrUpdateJobSchedule(
        ActivateTimeSheetJob.class, "activate_time_sheet" + companyId, jobParameter, executeDate);
  }

  private Map<String, Object> assembleCompanyIdParameter(final String companyId) {
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("companyId", companyId);
    return jobParameter;
  }

  private Date getStartOfDay(final Date date) {
    long time = date.getTime();
    time = time - time % MS_OF_ONE_DAY;
    return new Date(time);
  }

  private Date getEndOfDay(final Date date) {
    long time = date.getTime();
    time = time + MS_OF_ONE_DAY - time % MS_OF_ONE_DAY;
    return new Date(time);
  }

  private void saveCompanyTaSetting(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto, final String companyId) {
    final CompanyTaSetting existCompanyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findByName(timeAndAttendanceDetailsDto.getPayPeriodFrequency());
    final Company company = companyRepository.findCompanyById(companyId);
    final Date payDate = timeAndAttendanceDetailsDto.getPayDate();
    final Timestamp payDay = new Timestamp(payDate.getTime());
    final CompanyTaSetting companyTaSetting =
        new CompanyTaSetting(company, staticCompanyPayFrequencyType, payDay);
    if (null != existCompanyTaSetting) {
      companyTaSetting.setId(existCompanyTaSetting.getId());
    }
    attendanceSettingsService.saveCompanyTaSetting(companyTaSetting);
  }

  private List<UserCompensation> saveUserCompensations(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList) {
    final List<UserCompensation> userCompensations =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  final String userId = employeeOvertimeDetailsDto.getEmployeeId();
                  final BigInteger wageCents =
                      userCompensationMapper.updateCompensationCents(
                          employeeOvertimeDetailsDto.getRegularPay());
                  final CompensationFrequency compensationFrequency =
                      compensationFrequencyRepository
                          .findById(employeeOvertimeDetailsDto.getCompensationUnit())
                          .get();
                  final CompensationOvertimeStatus compensationOvertimeStatus =
                      compensationOvertimeStatusRepository
                          .findById(employeeOvertimeDetailsDto.getOvertimeLaw())
                          .get();
                  if (userCompensationService.existsByUserId(userId)) {
                    final UserCompensation userCompensation =
                        userCompensationService.findByUserId(userId);
                    userCompensation.setCompensationFrequency(compensationFrequency);
                    userCompensation.setOvertimeStatus(compensationOvertimeStatus);
                    userCompensation.setWageCents(wageCents);
                    return userCompensation;
                  } else {
                    return new UserCompensation(
                        userId, wageCents, compensationOvertimeStatus, compensationFrequency);
                  }
                })
            .collect(Collectors.toList());
    return userCompensationService.saveAll(userCompensations);
  }

  private void saveJobUsers(final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList) {
    final List<JobUser> jobUsers =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  JobUser jobUser =
                      jobUserRepository.findByUserId(employeeOvertimeDetailsDto.getEmployeeId());
                  final Timestamp hireDate =
                      new Timestamp(employeeOvertimeDetailsDto.getHireDate().getTime());
                  if (jobUser == null) {
                    jobUser = new JobUser();
                  }
                  jobUser.setStartDate(hireDate);
                  return jobUser;
                })
            .collect(Collectors.toList());
    jobUserRepository.saveAll(jobUsers);
  }

  public void createTimeSheetsAndPeriod(
      final TimePeriod newTimePeriod,
      final TimeSheetStatus timeSheetStatus,
      final List<UserCompensation> userCompensationList) {
    final List<TimeSheet> timeSheets = new ArrayList<>();
    final StaticTimesheetStatus timesheetStatus =
        staticTimesheetStatusRepository.findByName(timeSheetStatus.getValue());
    final TimePeriod timePeriod = timePeriodService.createIfNotExist(newTimePeriod);
    userCompensationList.forEach(
        userCompensation -> {
          final TimeSheet timeSheet = new TimeSheet();
          timeSheet.setTimePeriod(timePeriod);
          timeSheet.setUserCompensation(userCompensation);
          timeSheet.setEmployee(new User(userCompensation.getUserId()));
          timeSheet.setStatus(timesheetStatus);
          timeSheets.add(timeSheet);
        });

    timeSheetService.saveAll(timeSheets);
  }

  public TimePeriod getNextPeriod(
      final TimePeriod currentTimePeriod, final String payPeriodFrequency) {
    final Calendar currentEndDayOfPeriod = Calendar.getInstance();
    currentEndDayOfPeriod.setTimeInMillis(currentTimePeriod.getEndDate().getTime());

    final Calendar currentPayDate = copyCalendar(currentEndDayOfPeriod);
    currentPayDate.add(Calendar.DAY_OF_YEAR, PAY_DATE_AFTER_PERIOD_DAYS);
    final Calendar nextPayDate = getNextPayDate(currentPayDate, payPeriodFrequency);

    currentEndDayOfPeriod.add(Calendar.DAY_OF_YEAR, 1);
    final Timestamp periodStartDate = new Timestamp(currentEndDayOfPeriod.getTimeInMillis());
    nextPayDate.add(Calendar.DAY_OF_YEAR, -1 * PAY_DATE_AFTER_PERIOD_DAYS);
    final Timestamp periodEndDate = new Timestamp(nextPayDate.getTimeInMillis());

    return new TimePeriod(periodStartDate, periodEndDate);
  }

  private Calendar copyCalendar(final Calendar calendar) {
    final Calendar newCalendar = Calendar.getInstance();
    newCalendar.setTimeInMillis(calendar.getTimeInMillis());
    return newCalendar;
  }

  private Calendar getNextPayDate(final Calendar currentPayDate, final String payPeriodFrequency) {
    final PayFrequencyType payFrequencyType = PayFrequencyType.valueOf(payPeriodFrequency);
    Calendar nextPayDate = Calendar.getInstance();
    switch (payFrequencyType) {
      case WEEKLY:
        nextPayDate = getWeeksLaterFriday(currentPayDate, WEEKS_OF_WEEKLY);
        break;
      case BIWEEKLY:
        nextPayDate = getWeeksLaterFriday(currentPayDate, WEEKS_OF_BIWEEKLY);
        break;
      case MONTHLY:
        nextPayDate = getMonthsLaterLastDay(currentPayDate, 1);
        break;
      case BIMONTHLY:
        if (currentPayDate.get(Calendar.DAY_OF_MONTH) > MID_DAY_OF_MONTH) {
          nextPayDate = getMonthsLater15thDay(currentPayDate, 1);
        } else {
          nextPayDate = getMonthsLaterLastDay(currentPayDate, 0);
        }
        break;
      default:
        break;
    }
    return getClosestWeekDay(nextPayDate);
  }

  private Calendar getClosestWeekDay(final Calendar date) {
    while (!isWeekDay(date)) {
      date.add(Calendar.DAY_OF_YEAR, -1);
    }
    return date;
  }

  private Calendar getWeeksLaterFriday(final Calendar calendar, final int weeks) {
    calendar.add(Calendar.WEEK_OF_YEAR, weeks);
    calendar.set(Calendar.DAY_OF_WEEK, FRIDAY_DAY_OF_WEEK);
    return calendar;
  }

  private Calendar getMonthsLaterLastDay(final Calendar calendar, final int months) {
    calendar.add(Calendar.MONTH, months);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    return calendar;
  }

  private Calendar getMonthsLater15thDay(final Calendar calendar, final int months) {
    calendar.add(Calendar.MONTH, months);
    calendar.set(Calendar.DAY_OF_MONTH, MID_DAY_OF_MONTH);
    return calendar;
  }

  private boolean isWeekDay(final Calendar date) {
    final List<PaidHolidayDto> paidHolidayDtoList =
        paidHolidayService.getFederalHolidaysByYear(date.get(Calendar.YEAR));
    final int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
    if (dayOfWeek == SUNDAY_DAY_OF_WEEK || dayOfWeek == SATURDAY_DAY_OF_WEEK) {
      return false;
    }

    for (final PaidHolidayDto paidHolidayDto : paidHolidayDtoList) {
      final Timestamp holiday = paidHolidayDto.getDate();
      final Date holidayDate = new Date(holiday.getTime());
      if (DateUtils.isSameDay(holidayDate, date.getTime())) {
        return false;
      }
    }
    return true;
  }

  public TimePeriod findNextPeriodByUser(final String userId) {
    final TimePeriod timePeriod = timePeriodService.findUserLatestPeriod(userId);
    final User user = userService.findById(userId);
    final Optional<StaticCompanyPayFrequencyType> payFrequencyType =
        payPeriodFrequencyService.findByCompany(user.getCompany().getId());
    return payFrequencyType.map(staticCompanyPayFrequencyType -> getNextPeriod(timePeriod,
        staticCompanyPayFrequencyType.getName())).orElse(null);
  }
}
