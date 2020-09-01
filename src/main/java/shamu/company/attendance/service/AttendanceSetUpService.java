package shamu.company.attendance.service;

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
import shamu.company.attendance.exception.ParseDateException;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyService;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.email.service.EmailService.EmailNotification;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.scheduler.job.AddPayPeriodJob;
import shamu.company.scheduler.job.AttendanceEmailNotificationJob;
import shamu.company.scheduler.job.ChangeTimeSheetsStatusJob;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

@Service
public class AttendanceSetUpService {

  private static final int SUNDAY_DAY_OF_WEEK = 1;
  private static final int SATURDAY_DAY_OF_WEEK = 7;
  private static final int FRIDAY_DAY_OF_WEEK = 6;
  private static final int WEEKS_OF_WEEKLY = 1;
  private static final int WEEKS_OF_BIWEEKLY = 2;
  private static final int MID_DAY_OF_MONTH = 15;
  private static final int DAYS_PAY_DATE_AFTER_PERIOD = 6;
  private static final int HOURS_EMAIL_NOTIFICATION_BEFORE_SUBMIT = 8;
  private static final int DEFAULT_APPROVAL_DAYS_BEFORE_PAYROLL = 2;
  private static final long MS_OF_ONE_HOUR = 60 * 60 * 1000L;
  private static final long MS_OF_ONE_DAY = 24 * MS_OF_ONE_HOUR;
  private static final String COMPANY_POSTAL_CODE = "companyPostalCode";
  private static final String DATE_FORMAT = "MM/dd/yyyy";

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

  private final EmailService emailService;

  private final PayrollDetailService payrollDetailService;

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
      final CompanyTaSettingsMapper companyTaSettingsMapper,
      final EmailService emailService,
      final PayrollDetailService payrollDetailService) {
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
    this.emailService = emailService;
    this.payrollDetailService = payrollDetailService;
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
    final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList =
        timeAndAttendanceDetailsDto.getOvertimeDetails();

    final Boolean isAddOrRemoveEmployee = timeAndAttendanceDetailsDto.getIsAddOrRemove();

    final Map<String, StaticTimezone> allTimezones =
        findTimezonesByPostalCode(
            overtimeDetailsDtoList, employeeId, timeAndAttendanceDetailsDto.getFrontendTimezone());
    final StaticTimezone companyTimezone = allTimezones.get(COMPANY_POSTAL_CODE);

    final Date periodStartDate =
        parseDateWithZone(
                timeAndAttendanceDetailsDto.getPeriodStartDate(), companyTimezone.getName())
            .get();
    final Date periodEndDate =
        addOneDayTime(
            parseDateWithZone(
                    timeAndAttendanceDetailsDto.getPeriodEndDate(), companyTimezone.getName())
                .get());

    saveEmployeeTaSettings(timeAndAttendanceDetailsDto, allTimezones);

    final List<UserCompensation> userCompensationList =
        saveUserCompensations(overtimeDetailsDtoList, periodStartDate);
    saveJobUsers(overtimeDetailsDtoList);

    final Company company = companyService.findById(companyId);
    final TimePeriod firstTimePeriod =
        timePeriodService.save(new TimePeriod(periodStartDate, periodEndDate, company));
    final TimeSheetStatus timeSheetStatus;
    if (periodStartDate.after(new Date())) {
      timeSheetStatus = TimeSheetStatus.NOT_YET_START;
      if (Boolean.FALSE.equals(isAddOrRemoveEmployee)) {
        scheduleChangeTimeSheetsStatus(
            firstTimePeriod,
            TimeSheetStatus.NOT_YET_START,
            TimeSheetStatus.ACTIVE,
            periodStartDate);
      }
    } else {
      timeSheetStatus = TimeSheetStatus.ACTIVE;
    }

    if (Boolean.TRUE.equals(isAddOrRemoveEmployee)) {
      final TimePeriod currentPeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
      createTimeSheets(currentPeriod, timeSheetStatus, userCompensationList);
    } else {
      createTimeSheets(firstTimePeriod, timeSheetStatus, userCompensationList);
      saveCompanyTaSetting(
          timeAndAttendanceDetailsDto.getPayPeriodFrequency(),
          timeAndAttendanceDetailsDto.getPayDate(),
          companyId,
          companyTimezone);
      scheduleTasks(companyId, firstTimePeriod, companyTimezone.getName());
    }
  }

  private Optional<Date> parseDateWithZone(final String date, final String timezone) {
    final SimpleDateFormat isoFormat = new SimpleDateFormat(DATE_FORMAT);
    isoFormat.setTimeZone(TimeZone.getTimeZone(timezone));
    try {
      return Optional.ofNullable(isoFormat.parse(date));
    } catch (final ParseException e) {
      throw new ParseDateException("Unable to parse date.", e);
    }
  }

  private void scheduleChangeTimeSheetsStatus(
      final TimePeriod timePeriod,
      final TimeSheetStatus fromStatus,
      final TimeSheetStatus toStatus,
      final Date executeDate) {
    final Map<String, Object> jobParameter = new HashMap<>();
    final String timePeriodId = timePeriod.getId();
    jobParameter.put("timePeriodId", timePeriodId);
    jobParameter.put("fromStatus", fromStatus.name());
    jobParameter.put("toStatus", toStatus.name());

    quartzJobScheduler.addOrUpdateJobSchedule(
        ChangeTimeSheetsStatusJob.class,
        timePeriodId,
        String.format("change_timeSheets_status_from_%s_to_%s", fromStatus, toStatus),
        jobParameter,
        executeDate);
  }

  public void scheduleTasks(
      final String companyId, final TimePeriod currentPeriod, final String companyTimeZone) {
    final Map<String, Object> jobParameter = assembleCompanyIdParameter(companyId);
    final Date currentPeriodEndDate = currentPeriod.getEndDate();

    quartzJobScheduler.addOrUpdateJobSchedule(
        AddPayPeriodJob.class, companyId, "newPeriod", jobParameter, currentPeriodEndDate);

    final Date autoSubmitDate = addOneDayTime(currentPeriodEndDate);
    scheduleChangeTimeSheetsStatus(
        currentPeriod, TimeSheetStatus.ACTIVE, TimeSheetStatus.SUBMITTED, autoSubmitDate);

    final String currentPeriodId = currentPeriod.getId();
    scheduleEmailNotification(
        currentPeriodId,
        EmailNotification.SUBMIT_TIME_SHEET,
        getPreNotificationDate(autoSubmitDate));

    final Date runPayrollDdl = getRunPayrollDdl(companyId, currentPeriodEndDate, companyTimeZone);
    scheduleEmailNotification(
        currentPeriodId, EmailNotification.RUN_PAYROLL, getPreNotificationDate(runPayrollDdl));
    scheduleEmailNotification(
        currentPeriodId, EmailNotification.RUN_PAYROLL_TIME_OUT, runPayrollDdl);
  }

  private void scheduleEmailNotification(
      final String periodId, final EmailNotification emailNotification, final Date sendDate) {
    final Map<String, Object> parameter =
        assembleEmailParameter(periodId, emailNotification, sendDate);
    quartzJobScheduler.addOrUpdateJobSchedule(
        AttendanceEmailNotificationJob.class,
        periodId,
        emailNotification.name() + "_emails",
        parameter,
        sendDate);
  }

  private Date getPreNotificationDate(final Date ddl) {
    return new Date(ddl.getTime() - HOURS_EMAIL_NOTIFICATION_BEFORE_SUBMIT * MS_OF_ONE_HOUR);
  }

  public void sendEmailNotification(
      final String periodId, final EmailNotification emailNotification, final Date sendDate) {
    final List<Email> emails =
        emailService.getAttendanceNotificationEmails(
            periodId, emailNotification, new Timestamp(sendDate.getTime()));
    emailService.saveAndScheduleEmails(emails);
  }

  private Map<String, Object> assembleEmailParameter(
      final String periodId, final EmailNotification emailNotification, final Date sendDate) {
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("periodId", periodId);
    jobParameter.put("emailNotification", emailNotification);
    jobParameter.put("sendDate", sendDate);
    return jobParameter;
  }

  private Map<String, Object> assembleCompanyIdParameter(final String companyId) {
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("companyId", companyId);
    return jobParameter;
  }

  private Date addOneDayTime(final Date date) {
    long time = date.getTime();
    time = time + MS_OF_ONE_DAY;
    return new Date(time);
  }

  // return a map containing company's timezone and all employees' timezones
  private Map<String, StaticTimezone> findTimezonesByPostalCode(
      final List<EmployeeOvertimeDetailsDto> overtimeDetails,
      final String adminUserId,
      final String frontendTimezone) {
    final String companyPostalCode =
        jobUserRepository.findByUserId(adminUserId).getOffice().getOfficeAddress().getPostalCode();
    final Set<String> allPostalCodes = findAllPostalCodes(overtimeDetails, companyPostalCode);
    final Map<String, String> postalCodeToTimezone =
        googleMapsHelper.findTimezoneByPostalCode(allPostalCodes);

    final Map<String, StaticTimezone> postalCodeToStaticTimezone = new HashMap<>();
    postalCodeToTimezone
        .keySet()
        .forEach(
            postalCode ->
                postalCodeToStaticTimezone.put(
                    postalCode,
                    staticTimeZoneRepository.findByName(postalCodeToTimezone.get(postalCode))));
    if (postalCodeToStaticTimezone.containsKey(COMPANY_POSTAL_CODE)) {
      postalCodeToStaticTimezone.put(
          COMPANY_POSTAL_CODE, postalCodeToStaticTimezone.get(companyPostalCode));
    } else {
      postalCodeToStaticTimezone.put(
          COMPANY_POSTAL_CODE, staticTimeZoneRepository.findByName(frontendTimezone));
    }
    return postalCodeToStaticTimezone;
  }

  private Set<String> findAllPostalCodes(
      final List<EmployeeOvertimeDetailsDto> overtimeDetails, final String adminPostalCode) {

    final Set<String> allPostalCodes =
        overtimeDetails.stream()
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
      final String periodFrequency,
      final Date payDate,
      final String companyId,
      final StaticTimezone companyTimezone) {
    final CompanyTaSetting existCompanyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findByName(periodFrequency);
    final Company company = companyRepository.findCompanyById(companyId);
    final PayrollDetail payrollDetail =
        new PayrollDetail(company, staticCompanyPayFrequencyType, payDate);
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    companyTaSetting.setCompany(company);
    companyTaSettingsMapper.updateFromCompanyTaSettings(
        companyTaSetting,
        companyTimezone,
        EmailNotificationStatus.ON.getValue(),
        DEFAULT_APPROVAL_DAYS_BEFORE_PAYROLL,
        SATURDAY.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    if (null != existCompanyTaSetting) {
      companyTaSetting.setId(existCompanyTaSetting.getId());
    }
    payrollDetailService.savePayrollDetail(payrollDetail);
    attendanceSettingsService.saveCompanyTaSetting(companyTaSetting);
  }

  public Date getRunPayrollDdl(
      final String companyId, final Date currentPeriodEndDate, final String companyTimeZone) {
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final int approvalDaysBeforePayroll =
        companyTaSetting == null
            ? DEFAULT_APPROVAL_DAYS_BEFORE_PAYROLL
            : companyTaSetting.getApprovalDaysBeforePayroll();

    final Calendar autoApproveDate =
        getCalendarInstance(
            currentPeriodEndDate.getTime() + approvalDaysBeforePayroll * MS_OF_ONE_DAY,
            companyTimeZone);
    getClosestWeekDay(autoApproveDate, 1);
    return new Date(autoApproveDate.getTimeInMillis());
  }

  private void saveEmployeeTaSettings(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto,
      final Map<String, StaticTimezone> allTimezones) {
    final List<EmployeesTaSetting> employeesTaSettings = new ArrayList<>();
    timeAndAttendanceDetailsDto
        .getOvertimeDetails()
        .forEach(
            employeeOvertimeDetailsDto -> {
              final String employeeId = employeeOvertimeDetailsDto.getEmployeeId();
              final EmployeesTaSetting employeesTaSetting =
                  employeesTaSettingRepository.findByEmployeeId(employeeId);
              if (employeesTaSetting != null) {
                return;
              }
              final User user = userService.findById(employeeId);
              final int defaultMessagingOn = EmailNotificationStatus.ON.getValue();
              final Office office =
                  jobUserRepository
                      .findByUserId(employeeOvertimeDetailsDto.getEmployeeId())
                      .getOffice();
              employeesTaSettings.add(
                  employeesTaSettingsMapper.convertToEmployeeTaSettings(
                      employeeId, defaultMessagingOn));
              if (user.getTimeZone() != null) {
                return;
              }
              if (office != null
                  && office.getOfficeAddress() != null
                  && allTimezones.containsKey(office.getOfficeAddress().getPostalCode())) {
                user.setTimeZone(allTimezones.get(office.getOfficeAddress().getPostalCode()));
              } else {
                user.setTimeZone(allTimezones.get(COMPANY_POSTAL_CODE));
              }
              userRepository.save(user);
            });
    employeesTaSettingRepository.saveAll(employeesTaSettings);
  }

  private List<UserCompensation> saveUserCompensations(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList, final Date startDate) {
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
                  final Timestamp startDateTimeStamp = new Timestamp(startDate.getTime());
                  if (userCompensationService.existsByUserId(userId)) {
                    final UserCompensation userCompensation =
                        userCompensationService.findByUserId(userId);
                    userCompensation.setCompensationFrequency(compensationFrequency);
                    userCompensation.setOvertimeStatus(compensationOvertimeStatus);
                    userCompensation.setWageCents(wageCents);
                    userCompensation.setStartDate(startDateTimeStamp);
                    return userCompensation;
                  } else {
                    return new UserCompensation(
                        userId,
                        wageCents,
                        compensationOvertimeStatus,
                        compensationFrequency,
                        startDateTimeStamp);
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

  public void createTimeSheets(
      final TimePeriod newTimePeriod,
      final TimeSheetStatus timeSheetStatus,
      final List<UserCompensation> userCompensationList) {
    final List<TimeSheet> timeSheets = new ArrayList<>();
    final StaticTimesheetStatus timesheetStatus =
        staticTimesheetStatusRepository.findByName(timeSheetStatus.getValue());
    final TimePeriod savedTimePeriod = timePeriodService.save(newTimePeriod);
    userCompensationList.forEach(
        userCompensation -> {
          final TimeSheet timeSheet = new TimeSheet();
          timeSheet.setTimePeriod(savedTimePeriod);
          timeSheet.setUserCompensation(userCompensation);
          timeSheet.setEmployee(new User(userCompensation.getUserId()));
          timeSheet.setStatus(timesheetStatus);
          timeSheets.add(timeSheet);
        });

    timeSheetService.saveAll(timeSheets);
  }

  public TimePeriod getNextPeriod(
      final TimePeriod currentTimePeriod, final String payPeriodFrequency, final Company company) {
    final StaticTimezone staticTimezone =
        attendanceSettingsService.findCompanySettings(company.getId()).getTimeZone();
    final Calendar currentEndDayOfPeriod =
        getCalendarInstance(currentTimePeriod.getEndDate().getTime(), staticTimezone.getName());

    final Calendar currentPayDate = copyCalendar(currentEndDayOfPeriod);
    currentPayDate.add(Calendar.DAY_OF_YEAR, DAYS_PAY_DATE_AFTER_PERIOD);
    final Calendar nextPayDate = getNextPayDate(currentPayDate, payPeriodFrequency);

    currentEndDayOfPeriod.add(Calendar.DAY_OF_YEAR, 1);
    final Timestamp periodStartDate = new Timestamp(currentEndDayOfPeriod.getTimeInMillis());
    nextPayDate.add(Calendar.DAY_OF_YEAR, -DAYS_PAY_DATE_AFTER_PERIOD);
    final Timestamp periodEndDate = new Timestamp(nextPayDate.getTimeInMillis());

    return new TimePeriod(periodStartDate, periodEndDate, company);
  }

  private Calendar getCalendarInstance(final long unixTimeStamp, final String timeZoneName) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(unixTimeStamp);
    final TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
    calendar.setTimeZone(timeZone);

    return calendar;
  }

  private Calendar copyCalendar(final Calendar calendar) {
    final Calendar newCalendar = Calendar.getInstance();
    newCalendar.setTimeInMillis(calendar.getTimeInMillis());
    newCalendar.setTimeZone(calendar.getTimeZone());
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
    return getClosestWeekDay(nextPayDate, -1);
  }

  private Calendar getClosestWeekDay(final Calendar date, final int offset) {
    while (!isWeekDay(date)) {
      date.add(Calendar.DAY_OF_YEAR, offset);
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
    final Optional<TimePeriod> timePeriod = timePeriodService.findUserCurrentPeriod(userId);
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
