package shamu.company.attendance.service;

import static java.time.DayOfWeek.SATURDAY;
import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserListDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmailNotificationStatus;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType.PayFrequencyType;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.entity.mapper.EmployeesTaSettingsMapper;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyService;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.scheduler.job.ActivateTimeSheetJob;
import shamu.company.scheduler.job.AddPayPeriodAndAutoSubmitHourJob;
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

@Service
public class AttendanceSetUpService {

  private static final int SUNDAY_DAY_OF_WEEK = 1;
  private static final int SATURDAY_DAY_OF_WEEK = 7;
  private static final int FRIDAY_DAY_OF_WEEK = 6;
  private static final int WEEKS_OF_WEEKLY = 1;
  private static final int WEEKS_OF_BIWEEKLY = 2;
  private static final int MID_DAY_OF_MONTH = 15;
  private static final int PAY_DATE_AFTER_PERIOD_DAYS = 7;
  private static final int DEFAULT_APPROVAL_DAYS_BEFORE_PAYROLL = 2;
  private static final long MS_OF_ONE_DAY = 24 * 60 * 60 * 1000l;
  private static final String COMPANY_POSTAL_CODE = "companyPostalCode";

  private final AttendanceSettingsService attendanceSettingsService;

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

  private final StaticTimeZoneRepository staticTimeZoneRepository;

  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  private final UserCompensationMapper userCompensationMapper;

  private final PayPeriodFrequencyService payPeriodFrequencyService;

  private final CompanyService companyService;

  private final GoogleMapsHelper googleMapsHelper;

  private final EmployeesTaSettingsMapper employeesTaSettingsMapper;

  private final CompanyTaSettingsMapper companyTaSettingsMapper;

  public AttendanceSetUpService(
      final AttendanceSettingsService attendanceSettingsService,
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
      final StaticTimeZoneRepository staticTimeZoneRepository,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final UserCompensationMapper userCompensationMapper,
      final PayPeriodFrequencyService payPeriodFrequencyService,
      final CompanyService companyService,
      final GoogleMapsHelper googleMapsHelper,
      final EmployeesTaSettingsMapper employeesTaSettingsMapper,
      final CompanyTaSettingsMapper companyTaSettingsMapper) {
    this.attendanceSettingsService = attendanceSettingsService;
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
    this.staticTimeZoneRepository = staticTimeZoneRepository;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.userCompensationMapper = userCompensationMapper;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
    this.companyService = companyService;
    this.googleMapsHelper = googleMapsHelper;
    this.employeesTaSettingsMapper = employeesTaSettingsMapper;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
  }

  public Boolean findIsAttendanceSetUp(final String companyId) {
    return attendanceSettingsService.existsByCompanyId(companyId);
  }

  public TimeAndAttendanceRelatedUserListDto getRelatedUsers(final String companyId) {
    final List<User> selectedUsers = userService.listCompanyAttendanceEnrolledUsers(companyId);
    final List<User> allUsers = userRepository.findAllByCompanyId(companyId);

    final List<String> selectedUsersIds = new ArrayList<>();
    final List<TimeAndAttendanceRelatedUserDto> selectedEmployees =
        selectedUsers.stream()
            .map(
                user -> {
                  selectedUsersIds.add(user.getId());
                  return assembleRelatedUsers(user, allUsers);
                })
            .collect(Collectors.toList());

    final List<User> unSelectedUsers =
        selectedUsersIds.isEmpty()
            ? allUsers
            : userRepository.findAllByCompanyIdAndIdNotIn(companyId, selectedUsersIds);
    final List<TimeAndAttendanceRelatedUserDto> unSelectedEmployees =
        unSelectedUsers.stream()
            .map(user -> assembleRelatedUsers(user, allUsers))
            .collect(Collectors.toList());

    return new TimeAndAttendanceRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  private TimeAndAttendanceRelatedUserDto assembleRelatedUsers(
      final User user, final List<User> allUsers) {
    final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
    final String userNameOrUserNameWithEmailAddress =
        userService.getUserNameInUsers(user, allUsers);
    return jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
        user, employeeWithJobInfo, userNameOrUserNameWithEmailAddress);
  }

  @Transactional
  public void saveAttendanceDetails(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final String companyId,
      final String employeeId) {
    saveAttendanceSettings(timeAndAttendanceDetailsDto, companyId, employeeId);

    final Company company = companyService.findById(companyId);

    final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList =
        timeAndAttendanceDetailsDto.getOvertimeDetails();

    final List<UserCompensation> userCompensationList =
        saveUserCompensations(overtimeDetailsDtoList);
    saveJobUsers(overtimeDetailsDtoList);

    final Date periodStartDate = getStartOfDay(timeAndAttendanceDetailsDto.getPeriodStartDate());
    final Date periodEndDate = timeAndAttendanceDetailsDto.getPeriodEndDate();

    final TimePeriod firstTimePeriod = new TimePeriod(periodStartDate, periodEndDate, company);

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
        AddPayPeriodAndAutoSubmitHourJob.class,
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

  public void saveAttendanceSettings(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final String companyId,
      final String employeeId) {
    final String adminOfficePostalCode =
        jobUserRepository
            .findByUserId(employeeId)
            .getOffice()
            .getOfficeAddress()
            .getPostalCode();
    final Set<String> allPostalCodes = findAllPostalCodes(timeAndAttendanceDetailsDto, adminOfficePostalCode);
    final Map<String, String> companyPostalCodes = googleMapsHelper.findTimezoneByPostalCode(allPostalCodes);
    final Map<String, StaticTimezone> allTimezones =
        findTimezonesByPostalCode(companyPostalCodes, adminOfficePostalCode);
    saveCompanyTaSetting(timeAndAttendanceDetailsDto, companyId, allTimezones);
    saveEmployeeTaSettings(timeAndAttendanceDetailsDto, allTimezones);
  }

  private Map<String, StaticTimezone> findTimezonesByPostalCode(
      final Map<String, String> staticTimezones, final String companyPostalCode) {
    final Map<String, StaticTimezone> allTimezones = new HashMap<>();
    staticTimezones.keySet().stream()
        .forEach(
            postalCode ->
                allTimezones.put(
                    postalCode,
                    staticTimeZoneRepository.findByName(staticTimezones.get(postalCode))));
    allTimezones.put(COMPANY_POSTAL_CODE, allTimezones.get(companyPostalCode));
    return allTimezones;
  }

  private Set<String> findAllPostalCodes(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final String adminPostalCode) {

    final Set<String> allPostalCodes =
        timeAndAttendanceDetailsDto.getOvertimeDetails().stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  final Office office =
                      jobUserRepository
                          .findByUserId(employeeOvertimeDetailsDto.getEmployeeId())
                          .getOffice();
                  if (office != null && office.getOfficeAddress() != null) {
                    return office.getOfficeAddress().getPostalCode();
                  }
                  return adminPostalCode;
                })
            .collect(Collectors.toSet());
    allPostalCodes.add(adminPostalCode);
    return allPostalCodes;
  }

  private void saveCompanyTaSetting(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final String companyId,
      final Map<String, StaticTimezone> allTimezones) {
    final CompanyTaSetting existCompanyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findByName(timeAndAttendanceDetailsDto.getPayPeriodFrequency());
    final Company company = companyRepository.findCompanyById(companyId);
    final Date payDate = timeAndAttendanceDetailsDto.getPayDate();
    final Timestamp payDay = new Timestamp(payDate.getTime());
    final CompanyTaSetting companyTaSetting =
        new CompanyTaSetting(company, staticCompanyPayFrequencyType, payDay);
    companyTaSettingsMapper.updateFromCompanyTaSettings(
        companyTaSetting,
        allTimezones.get(COMPANY_POSTAL_CODE),
        EmailNotificationStatus.ON.getValue(),
        DEFAULT_APPROVAL_DAYS_BEFORE_PAYROLL,
        SATURDAY.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    if (null != existCompanyTaSetting) {
      companyTaSetting.setId(existCompanyTaSetting.getId());
    }
    attendanceSettingsService.saveCompanyTaSetting(companyTaSetting);
  }

  private void saveEmployeeTaSettings(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final Map<String, StaticTimezone> allTimezones) {
    final List<EmployeesTaSetting> employeesTaSettings =
        timeAndAttendanceDetailsDto.getOvertimeDetails().stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  final String employeeId = employeeOvertimeDetailsDto.getEmployeeId();
                  final int defaultMessagingOn = EmailNotificationStatus.ON.getValue();
                  final Office office =
                      jobUserRepository
                          .findByUserId(employeeOvertimeDetailsDto.getEmployeeId())
                          .getOffice();
                  if (office != null && office.getOfficeAddress() != null) {
                    return employeesTaSettingsMapper.convertToEmployeeTaSettings(
                        allTimezones.get(office.getOfficeAddress().getPostalCode()),
                        employeeId,
                        defaultMessagingOn);
                  } else {
                    return employeesTaSettingsMapper.convertToEmployeeTaSettings(
                        allTimezones.get(COMPANY_POSTAL_CODE), employeeId, defaultMessagingOn);
                  }
                })
            .collect(Collectors.toList());
    employeesTaSettingRepository.saveAll(employeesTaSettings);
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
    userCompensationList.forEach(
        userCompensation -> {
          final TimeSheet timeSheet = new TimeSheet();
          timeSheet.setTimePeriod(newTimePeriod);
          timeSheet.setUserCompensation(userCompensation);
          timeSheet.setEmployee(new User(userCompensation.getUserId()));
          timeSheet.setStatus(timesheetStatus);
          timeSheets.add(timeSheet);
        });

    timeSheetService.saveAll(timeSheets);
  }

  public TimePeriod getNextPeriod(
      final TimePeriod currentTimePeriod, final String payPeriodFrequency, final Company company) {
    final Calendar currentEndDayOfPeriod = Calendar.getInstance();
    currentEndDayOfPeriod.setTimeInMillis(currentTimePeriod.getEndDate().getTime());

    final Calendar currentPayDate = copyCalendar(currentEndDayOfPeriod);
    currentPayDate.add(Calendar.DAY_OF_YEAR, PAY_DATE_AFTER_PERIOD_DAYS);
    final Calendar nextPayDate = getNextPayDate(currentPayDate, payPeriodFrequency);

    currentEndDayOfPeriod.add(Calendar.DAY_OF_YEAR, 1);
    final Timestamp periodStartDate = new Timestamp(currentEndDayOfPeriod.getTimeInMillis());
    nextPayDate.add(Calendar.DAY_OF_YEAR, -1 * PAY_DATE_AFTER_PERIOD_DAYS);
    final Timestamp periodEndDate = new Timestamp(nextPayDate.getTimeInMillis());

    return new TimePeriod(periodStartDate, periodEndDate, company);
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
    final Optional<TimePeriod> timePeriod = timePeriodService.findUserLatestPeriod(userId);
    if (!timePeriod.isPresent()) {
      return null;
    }
    final User user = userService.findById(userId);
    final Optional<StaticCompanyPayFrequencyType> payFrequencyType =
        payPeriodFrequencyService.findByCompany(user.getCompany().getId());
    return payFrequencyType
        .map(
            staticCompanyPayFrequencyType ->
                getNextPeriod(
                    timePeriod.get(), staticCompanyPayFrequencyType.getName(), user.getCompany()))
        .orElse(null);
  }
}
