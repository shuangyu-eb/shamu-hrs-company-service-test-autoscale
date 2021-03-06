package shamu.company.user.service;

import io.micrometer.core.instrument.util.StringUtils;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.attendance.entity.CompanyTaSetting.MessagingON;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.PermissionUtils;
import shamu.company.client.DocumentClient;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.EmailAlreadyVerifiedException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.service.CompanyBenefitsSettingService;
import shamu.company.company.service.CompanyService;
import shamu.company.crypto.SecretHashRepository;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.EmailUpdateDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.helpers.s3.Type;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.scheduler.job.DeactivateUserJob;
import shamu.company.sentry.SentryLogger;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserRoleUpdateDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.DeactivationReasons;
import shamu.company.user.entity.DismissedAt;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAccessLevelEvent;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.exception.errormapping.AuthenticationFailedException;
import shamu.company.user.exception.errormapping.EmailExpiredException;
import shamu.company.user.exception.errormapping.PasswordDuplicatedException;
import shamu.company.user.exception.errormapping.UserDeletionFailedException;
import shamu.company.user.exception.errormapping.UserNotFoundByEmailException;
import shamu.company.user.exception.errormapping.UserNotFoundByInvitationTokenException;
import shamu.company.user.exception.errormapping.WorkEmailDuplicatedException;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

@Service
@Transactional
public class UserService {

  private static final SentryLogger log = new SentryLogger(UserService.class);

  private final UserRepository userRepository;
  private final SecretHashRepository secretHashRepository;

  private final ITemplateEngine templateEngine;
  private final Auth0Helper auth0Helper;
  private final AwsHelper awsHelper;
  private final AuthUserCacheManager authUserCacheManager;
  private final QuartzJobScheduler quartzJobScheduler;
  private final PermissionUtils permissionUtils;
  private final EntityManager entityManager;

  private final UserEmergencyContactService userEmergencyContactService;
  private final UserAddressService userAddressService;
  private final EmailService emailService;
  private final UserRoleService userRoleService;
  private final JobUserService jobUserService;
  private final UserStatusService userStatusService;
  private final CompanyService companyService;
  private final UserAccessLevelEventService userAccessLevelEventService;
  private final UserContactInformationService userContactInformationService;

  private final UserContactInformationMapper userContactInformationMapper;
  private final UserAddressMapper userAddressMapper;
  private final UserPersonalInformationMapper userPersonalInformationMapper;
  private final UserMapper userMapper;
  private final CompanyBenefitsSettingService companyBenefitsSettingService;
  private final DismissedAtService dismissedAtService;
  private final OvertimeService overtimeService;
  private final AttendanceTeamHoursService attendanceTeamHoursService;

  private final DocumentClient documentClient;

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Value("${application.systemEmailFirstName}")
  private String systemEmailFirstName;

  @Value("${application.systemEmailLastName}")
  private String systemEmailLastName;

  @Autowired
  public UserService(
      final ITemplateEngine templateEngine,
      final UserRepository userRepository,
      final SecretHashRepository secretHashRepository,
      final EmailService emailService,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserEmergencyContactService userEmergencyContactService,
      final UserAddressService userAddressService,
      final UserContactInformationMapper userContactInformationMapper,
      final UserAddressMapper userAddressMapper,
      final Auth0Helper auth0Helper,
      final UserMapper userMapper,
      final AuthUserCacheManager authUserCacheManager,
      final QuartzJobScheduler quartzJobScheduler,
      final AwsHelper awsHelper,
      final UserRoleService userRoleService,
      @Lazy final PermissionUtils permissionUtils,
      @Lazy final JobUserService jobUserService,
      final UserStatusService userStatusService,
      final CompanyService companyService,
      final UserAccessLevelEventService userAccessLevelEventService,
      final UserContactInformationService userContactInformationService,
      final CompanyBenefitsSettingService companyBenefitsSettingService,
      final EntityManager entityManager,
      final DismissedAtService dismissedAtService,
      final DocumentClient documentClient,
      final OvertimeService overtimeService,
      @Lazy final AttendanceTeamHoursService attendanceTeamHoursService) {
    this.templateEngine = templateEngine;
    this.userRepository = userRepository;
    this.secretHashRepository = secretHashRepository;
    this.emailService = emailService;
    this.userEmergencyContactService = userEmergencyContactService;
    this.userAddressService = userAddressService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userAddressMapper = userAddressMapper;
    this.auth0Helper = auth0Helper;
    this.userMapper = userMapper;
    this.authUserCacheManager = authUserCacheManager;
    this.quartzJobScheduler = quartzJobScheduler;
    this.awsHelper = awsHelper;
    this.userRoleService = userRoleService;
    this.permissionUtils = permissionUtils;
    this.jobUserService = jobUserService;
    this.userStatusService = userStatusService;
    this.companyService = companyService;
    this.userAccessLevelEventService = userAccessLevelEventService;
    this.userContactInformationService = userContactInformationService;
    this.companyBenefitsSettingService = companyBenefitsSettingService;
    this.entityManager = entityManager;
    this.dismissedAtService = dismissedAtService;
    this.documentClient = documentClient;
    this.overtimeService = overtimeService;
    this.attendanceTeamHoursService = attendanceTeamHoursService;
  }

  public User findById(final String id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("User with id %s not found!", id), id, "user"));
  }

  public User findActiveUserById(final String userId) {
    return userRepository.findActiveUserById(userId);
  }

  public List<User> findByManagerUser(final User managerUser) {
    return userRepository.findByManagerUser(managerUser);
  }

  public void cacheUser(final String token, final String userId) {
    final User user = findById(userId);
    final AuthUser authUser = userMapper.convertToAuthUser(user);
    authUser.setCompanyId(TenantContext.getCurrentTenant());
    final List<String> permissions = auth0Helper.getPermissionBy(user);
    authUser.setPermissions(permissions);
    authUserCacheManager.cacheAuthUser(token, authUser);
  }

  public User findUserByUserPersonalInformationId(final String userPersonalInformationId) {
    return userRepository.findByUserPersonalInformationId(userPersonalInformationId);
  }

  public User findUserByUserContactInformationId(final String userContactInformationId) {
    return userRepository.findByUserContactInformationId(userContactInformationId);
  }

  public List<User> findDirectReportsByManagerId(final String id) {
    return userRepository.findAllByManagerUserId(id);
  }

  public JobUserDto findEmployeeInfoByEmployeeId(final String id) {
    final User employee = findById(id);
    final JobUser jobUser = jobUserService.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser, null);
  }

  public Page<JobUserListItem> findAllEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    if (!permissionUtils.hasAuthority(Name.VIEW_DISABLED_USER.name())) {
      employeeListSearchCondition.setIncludeDeactivated(false);
    }

    final Pageable paramPageable = getPageable(employeeListSearchCondition);
    return getAllEmployeesByCompany(employeeListSearchCondition, paramPageable);
  }

  private Pageable getPageable(final EmployeeListSearchCondition employeeListSearchCondition) {
    final String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    final String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    return PageRequest.of(
        employeeListSearchCondition.getPage(),
        employeeListSearchCondition.getSize(),
        Sort.Direction.valueOf(sortDirection),
        sortValue);
  }

  private Page<JobUserListItem> getAllEmployeesByCompany(
      final EmployeeListSearchCondition employeeListSearchCondition, final Pageable pageable) {
    return userRepository.getAllByCondition(employeeListSearchCondition, pageable);
  }

  public Page<JobUserListItem> findAllEmployeesByName(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    final Pageable pageable = getPageable(employeeListSearchCondition);
    return userRepository.getAllByName(employeeListSearchCondition, pageable);
  }

  public User save(final User user) {
    return userRepository.save(user);
  }

  public User createNewEmployee(final User user) {
    user.setSalt();
    return save(user);
  }

  public List<JobUserDto> findAllJobUsers() {
    final List<User> policyEmployees = userRepository.findAllActiveUsers();

    return policyEmployees.stream()
        .map(
            user -> {
              final JobUser employeeWithJob = jobUserService.findJobUserByUser(user);
              final String name = getUserNameInUsers(user, policyEmployees);
              return new JobUserDto(user, employeeWithJob, name);
            })
        .collect(Collectors.toList());
  }

  public List<User> findAllUsersByCompany() {
    return userRepository.findAllActiveUsers();
  }

  public List<OrgChartDto> getOrgChart(final String userId) {
    final List<OrgChartDto> orgChartDtoList = new ArrayList<>();
    List<OrgChartDto> orgChartManagerItemList = new ArrayList<>();
    if (!StringUtils.isBlank(userId)) {
      final OrgChartDto manager = userRepository.findOrgChartItemByUserId(userId);
      orgChartManagerItemList.add(manager);
      if (!orgChartManagerItemList.isEmpty()) {
        for (final OrgChartDto managerItem : orgChartManagerItemList) {

          final List<OrgChartDto> orgChartUserItemList =
              userRepository.findOrgChartItemByManagerId(managerItem.getId());
          orgChartUserItemList.forEach(
              (orgUser -> {
                final Integer directReportsCount =
                    userRepository.findDirectReportsCount(orgUser.getId());
                orgUser.setDirectReportsCount(directReportsCount);
              }));
          managerItem.setDirectReports(orgChartUserItemList);
          managerItem.setDirectReportsCount(orgChartUserItemList.size());
          orgChartDtoList.add(managerItem);
        }
      }
    } else {
      // retrieve company admin from database
      orgChartManagerItemList = userRepository.findOrgChartItemByManagerId(null);
      final Company company = companyService.getCompany();
      final Integer allEmployeesCount = userRepository.countExistingUser();
      final OrgChartDto orgChartDto = userMapper.convertOrgChartDto(company);
      orgChartDto.setIsCompany(true);
      orgChartManagerItemList.forEach(
          (orgUser -> {
            final Integer directReportsCount =
                userRepository.findDirectReportsCount(orgUser.getId());
            orgUser.setDirectReportsCount(directReportsCount);
          }));
      orgChartDto.setDirectReportsCount(allEmployeesCount);
      orgChartDto.setDirectReports(orgChartManagerItemList);
      orgChartDtoList.add(orgChartDto);
    }
    return orgChartDtoList;
  }

  public AccountInfoDto getPreSetAccountInfoByUserId(final String id) {
    final User user = findById(id);

    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final UserPersonalInformationDto userPersonalInformationDto =
        userPersonalInformationMapper.convertToUserPersonalInformationDto(userPersonalInformation);

    final String headPortrait = user.getImageUrl();

    final UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    final UserContactInformation userContactInformation = user.getUserContactInformation();
    final UserContactInformationDto userContactInformationDto =
        userContactInformationMapper.convertToUserContactInformationDto(userContactInformation);

    final List<UserEmergencyContact> userEmergencyContacts =
        userEmergencyContactService.findUserEmergencyContacts(id);

    final List<UserEmergencyContactDto> userEmergencyContactDtos =
        userEmergencyContacts.stream()
            .map(UserEmergencyContactDto::new)
            .collect(Collectors.toList());

    return new AccountInfoDto(
        userPersonalInformationDto,
        headPortrait,
        userAddressMapper.convertToUserAddressDto(userAddress),
        userContactInformationDto,
        userEmergencyContactDtos);
  }

  public Boolean createPasswordTokenExist(final String token) {
    return userRepository.existsByResetPasswordToken(token);
  }

  public boolean createPasswordAndInvitationTokenExist(
      final String passwordToken, final String invitationToken) {
    final User user = userRepository.findByInvitationEmailToken(invitationToken);
    if (user == null) {
      throw new UserNotFoundByInvitationTokenException(
          String.format("User with invitationToken %s not found!", invitationToken),
          invitationToken);
    }
    if (Timestamp.from(Instant.now())
        .after(
            Timestamp.valueOf(user.getInvitedAt().toLocalDateTime().plus(72, ChronoUnit.HOURS)))) {
      throw new EmailExpiredException("Email is expired.");
    }

    return userRepository.existsByResetPasswordToken(passwordToken);
  }

  public Page<JobUserListItem> getMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition, final String id) {
    final Pageable paramPageable = getPageable(employeeListSearchCondition);
    final User user = findById(id);
    return userRepository.getMyTeamByManager(employeeListSearchCondition, user, paramPageable);
  }

  public User updateUserRole(
      final String email, final UserRoleUpdateDto userRoleUpdateDto, final User user) {
    if (!auth0Helper.isIndeedEnvironment()) {
      auth0Helper.login(email, userRoleUpdateDto.getPassWord());
    }

    final UserRole targetRole;
    if (userRoleUpdateDto.getUserRole() == Role.ADMIN) {
      if (!userRepository.findAllByManagerUserId(user.getId()).isEmpty()) {
        targetRole = userRoleService.getManager();
      } else {
        targetRole = userRoleService.getEmployee();
      }
    } else {
      targetRole = userRoleService.getAdmin();
    }
    user.setUserRole(targetRole);
    return userRepository.save(user);
  }

  public void deactivateUser(final UserStatusUpdateDto userStatusUpdateDto, final User user) {
    if ((Status.ACTIVE.name()).equals(userStatusUpdateDto.getUserStatus().name())
        || Status.CHANGING_EMAIL_VERIFICATION
            .name()
            .equals(userStatusUpdateDto.getUserStatus().name())) {
      userAccessLevelEventService.save(
          new UserAccessLevelEvent(user, user.getUserRole().getName()));

      user.setUserRole(userRoleService.getInactive());
      user.setUserStatus(userStatusService.findByName(Status.DISABLED.name()));
      user.setChangeWorkEmailToken(null);
      adjustUserManagerRelationshipBeforeDeleteOrDeactivate(user);
      userRepository.save(user);
    }
  }

  public User deactivateUser(
      final String email, final UserStatusUpdateDto userStatusUpdateDto, final User user) {
    if (!auth0Helper.isIndeedEnvironment()) {
      auth0Helper.login(email, userStatusUpdateDto.getPassWord());
    }

    final Date deactivationDate = userStatusUpdateDto.getDeactivationDate();

    if (deactivationDate.toString().equals(LocalDate.now().toString())) {
      deactivateUser(userStatusUpdateDto, user);
    } else {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("UserStatusUpdateDto", userStatusUpdateDto);
      jobParameter.put("User", user);
      jobParameter.put("companyId", TenantContext.getCurrentTenant());
      quartzJobScheduler.addOrUpdateJobSchedule(
          DeactivateUserJob.class,
          user.getId(),
          "deactivate_user",
          jobParameter,
          userStatusUpdateDto.getDeactivationDate());
    }

    user.setDeactivatedAt(userStatusUpdateDto.getDeactivationDate());
    user.setDeactivationReason(
        new DeactivationReasons(userStatusUpdateDto.getDeactivationReason().getId()));
    userRepository.save(user);

    attendanceTeamHoursService.removeAttendanceDetails(Collections.singletonList(user.getId()));
    return findById(user.getId());
  }

  public void deleteUser(final User employee) {
    try {
      adjustUserManagerRelationshipBeforeDeleteOrDeactivate(employee);
      final User deletedUser = entityManager.find(User.class, employee.getId());
      entityManager.remove(entityManager.merge(deletedUser));
      entityManager.flush();
      documentClient.deleteDocumentRequestUser(employee.getId());
    } catch (final PersistenceException e) {
      log.error("User with id " + employee.getId() + " deletes failed", e);
      throw new UserDeletionFailedException("User deletes failed.");
    } finally {
      if (entityManager.isOpen()) {
        entityManager.close();
      }
    }

    final String employeeWorkEmail = employee.getUserContactInformation().getEmailWork();
    if (!(auth0Helper.isIndeedEnvironment()
        && employee.getUserStatus().getName().equals(Status.PENDING_VERIFICATION.name()))) {
      auth0Helper.deleteUser(
          auth0Helper
              .getAuth0UserByIdWithByEmailFailover(employee.getId(), employeeWorkEmail)
              .getId());
    }
  }

  private void adjustUserManagerRelationshipBeforeDeleteOrDeactivate(final User user) {
    final List<User> teamEmployees = userRepository.findAllByManagerUserId(user.getId());
    if (!CollectionUtils.isEmpty(teamEmployees)) {
      final User targetManager = user.getManagerUser();
      teamEmployees.forEach(employee -> employee.setManagerUser(targetManager));
      userRepository.saveAll(teamEmployees);
    }
  }

  public void signUp(final UserSignUpDto signUpDto, final String userId) {
    final UserPersonalInformation userPersonalInformation =
        UserPersonalInformation.builder()
            .firstName(signUpDto.getFirstName())
            .lastName(signUpDto.getLastName())
            .build();

    final String emailAddress = signUpDto.getWorkEmail();
    final UserContactInformation userContactInformation =
        UserContactInformation.builder().emailWork(emailAddress).build();

    secretHashRepository.generateCompanySecretByCompanyId(TenantContext.getCurrentTenant());

    // saveCompanyBenefitsSetting();

    final UserStatus status = userStatusService.findByName(Status.ACTIVE.name());

    final User user =
        User.builder()
            .userStatus(status)
            .userPersonalInformation(userPersonalInformation)
            .userContactInformation(userContactInformation)
            .verifiedAt(Timestamp.valueOf(DateUtil.getLocalUtcTime()))
            .userRole(userRoleService.getAdmin())
            .salt(UuidUtil.getUuidString())
            .build();
    user.setId(userId);
    userRepository.save(user);

    overtimeService.createNotEligiblePolicy();
  }

  private void saveCompanyBenefitsSetting() {
    final CompanyBenefitsSetting benefitsSetting = new CompanyBenefitsSetting();
    benefitsSetting.setIsAutomaticRollover(true);
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public boolean hasUserAccess(final User currentUser, final String targetUserId) {
    final User targetUser = userRepository.findActiveUserById(targetUserId);
    if (targetUser == null) {
      throw new ResourceNotFoundException(
          String.format("User with id %s not found.", targetUserId), targetUserId, "user");
    }

    final Role userRole = currentUser.getRole();
    final Boolean isAdmin = Role.ADMIN == userRole || Role.SUPER_ADMIN == userRole;
    final User manager = targetUser.getManagerUser();

    final Boolean isManager =
        Role.MANAGER == userRole && manager != null && manager.getId().equals(currentUser.getId());

    return isAdmin || isManager;
  }

  public String getManagerUserIdById(final String userId) {
    return userRepository.getManagerUserIdById(userId);
  }

  public Boolean existsByEmailWork(final String email) {
    return auth0Helper.existsByEmail(email);
  }

  public void updatePassword(final ChangePasswordDto changePasswordDto, final String userId) {
    final com.auth0.json.mgmt.users.User user = auth0Helper.getUserByUserIdFromAuth0(userId);

    final String emailAddress = user.getEmail();
    final String fromName = systemEmailFirstName + "-" + systemEmailLastName;
    checkPassword(emailAddress, changePasswordDto.getPassword());
    if (changePasswordDto.getPassword().equals(changePasswordDto.getNewPassword())) {
      throw new PasswordDuplicatedException("New password cannot be the same as the old one.");
    }
    auth0Helper.updatePassword(user, changePasswordDto.getNewPassword());

    final Context context = new Context();
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"));
    final String currentYear =
        DateUtil.formatDateTo(
            zonedDateTime.withZoneSameInstant(ZoneId.of("America/Managua")).toLocalDateTime(),
            "YYYY");
    context.setVariable("currentYear", currentYear);
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable("isIndeedENV", auth0Helper.isIndeedEnvironment());
    final String emailContent = templateEngine.process("password_change_email.html", context);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final Email notificationEmail =
        new Email(systemEmailAddress, fromName, emailAddress, user.getName(), "Password Changed");
    notificationEmail.setSendDate(sendDate);
    notificationEmail.setContent(emailContent);
    emailService.saveAndScheduleEmail(notificationEmail);
  }

  public void checkPassword(final String email, final String password) {
    if (!auth0Helper.isPasswordValid(email, password)) {
      throw new AuthenticationFailedException("Wrong email or password.");
    }
  }

  public void updateWorkEmail(final EmailUpdateDto emailUpdateDto, final String currentUserEmail) {
    final User user = findById(emailUpdateDto.getUserId());
    checkPassword(currentUserEmail, emailUpdateDto.getPassword());

    if (user.getUserContactInformation().getEmailWork().equals(emailUpdateDto.getEmail())) {
      throw new WorkEmailDuplicatedException("New work email cannot be the same as the old one.");
    }

    if (BooleanUtils.isTrue(auth0Helper.existsByEmail(emailUpdateDto.getEmail()))) {
      throw new AlreadyExistsException("Email already exists.", "email");
    }

    user.setUserStatus(
        userStatusService.findByName(String.valueOf(Status.CHANGING_EMAIL_VERIFICATION)));
    user.setChangeWorkEmail(emailUpdateDto.getEmail());
    emailService.handleEmail(user);
    userRepository.save(user);
  }

  public void sendVerifyChangeWorkEmail(final User user) {
    final String newEmailToken = emailService.handleEmail(user);
    user.setChangeWorkEmailToken(newEmailToken);
    userRepository.save(user);
  }

  public boolean changeWorkEmailTokenExist(final String token) {

    final User currentUser = userRepository.findByChangeWorkEmailToken(token);

    if (!userRepository.existsByChangeWorkEmailToken(token)) {
      return false;
    } else {
      final com.auth0.json.mgmt.users.User user =
          auth0Helper.getUserByUserIdFromAuth0(currentUser.getId());

      auth0Helper.updateUserEmail(user, currentUser.getChangeWorkEmail());

      currentUser.setVerifyChangeWorkEmailAt(Timestamp.valueOf(LocalDateTime.now()));
      currentUser.getUserContactInformation().setEmailWork(currentUser.getChangeWorkEmail());
      currentUser.setChangeWorkEmailToken(null);
      currentUser.setUserStatus(userStatusService.findByName(String.valueOf(Status.ACTIVE)));

      userContactInformationService.save(currentUser.getUserContactInformation());
      userRepository.save(currentUser);
      return true;
    }
  }

  public CurrentUserDto getCurrentUserInfo(final String userId) {
    final User user = findById(userId);
    return getCurrentUserDto(user);
  }

  public CurrentUserDto getCurrentUserInfo(final String userId, final String userEmail) {
    User user = findById(userId);
    final boolean isIndeedUserEmailUpdate =
        auth0Helper.isIndeedEnvironment()
            && !userEmail.equals(user.getUserContactInformation().getEmailWork());

    if (isIndeedUserEmailUpdate) {
      // update indeed user email
      final UserContactInformation userContactInformation = user.getUserContactInformation();
      userContactInformation.setEmailWork(userEmail);
      user.setUserContactInformation(userContactInformation);
      user = save(user);
    }
    return getCurrentUserDto(user);
  }

  private CurrentUserDto getCurrentUserDto(final User user) {
    final List<User> teamMembers = findByManagerUser(user);
    final List<String> teamMemberIds =
        teamMembers.stream().map(User::getId).collect(Collectors.toList());
    if (user.getVerifiedAt() == null) {
      return CurrentUserDto.builder().id(user.getId()).verified(false).build();
    }

    return CurrentUserDto.builder()
        .id(user.getId())
        .teamMembers(teamMemberIds)
        .name(user.getUserPersonalInformation().getName())
        .imageUrl(user.getImageUrl())
        .verified(user.getVerifiedAt() != null)
        .userRole(user.getUserRole().getName())
        .build();
  }

  public CurrentUserDto getMockUserInfo(final String userId) {
    final User user = findById(userId);
    return getCurrentUserDto(user);
  }

  public void sendResetPasswordEmail(final String email) {
    final User targetUser = userRepository.findByEmailWork(email);
    if (targetUser == null) {
      throw new UserNotFoundByEmailException(
          String.format("User account with email %s not found.", email), email);
    }

    final com.auth0.json.mgmt.users.User user =
        auth0Helper.getUserByUserIdFromAuth0(targetUser.getId());
    if (user == null) {
      throw new ResourceNotFoundException(
          String.format("User account with id %s not found.", targetUser.getId()),
          targetUser.getId(),
          "user account");
    }

    final String passwordRestToken = UUID.randomUUID().toString();
    final String emailContent = emailService.getResetPasswordEmail(passwordRestToken, email);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final String fromName = systemEmailFirstName + "-" + systemEmailLastName;
    final Email verifyEmail =
        new Email(systemEmailAddress, fromName, email, user.getName(), "Password Reset");
    verifyEmail.setSendDate(sendDate);
    verifyEmail.setContent(emailContent);
    emailService.saveAndScheduleEmail(verifyEmail);
    targetUser.setResetPasswordToken(passwordRestToken);
    userRepository.save(targetUser);
  }

  public void resetPassword(final UpdatePasswordDto updatePasswordDto) {
    final User user =
        userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken());
    if (user == null) {
      throw new UserNotFoundByInvitationTokenException(
          String.format("User with id %s not found.", updatePasswordDto.getResetPasswordToken()),
          updatePasswordDto.getResetPasswordToken());
    }

    final com.auth0.json.mgmt.users.User auth0User =
        auth0Helper.getUserByUserIdFromAuth0(user.getId());

    if (auth0User == null) {
      throw new ResourceNotFoundException(
          String.format("Auth0 user with id %s not found.", user.getId()),
          user.getId(),
          "Auth0 user");
    }

    final String email = user.getUserContactInformation().getEmailWork();
    if (auth0Helper.isPasswordValid(email, updatePasswordDto.getNewPassword())) {
      throw new PasswordDuplicatedException("New password cannot be the same as the old one.");
    }

    auth0Helper.updatePassword(auth0User, updatePasswordDto.getNewPassword());

    final UserStatus pendingStatus =
        userStatusService.findByName(Status.PENDING_VERIFICATION.name());

    if (user.getUserStatus().equals(pendingStatus)) {
      final UserStatus userStatus = userStatusService.findByName(Status.ACTIVE.name());
      user.setUserStatus(userStatus);
    }

    user.setResetPasswordToken(null);
    userRepository.save(user);
  }

  public String handleUploadFile(final String id, final MultipartFile file) {
    final String path = awsHelper.uploadFile(file, Type.IMAGE);

    if (Strings.isBlank(path)) {
      return null;
    }

    final User user = findById(id);
    final String originalPath = user.getImageUrl();
    awsHelper.deleteFile(originalPath);

    user.setImageUrl(path);
    save(user);

    return path;
  }

  public void handleDeleteHeadPortrait(final String id) {
    final User user = findById(id);

    awsHelper.deleteFile(user.getImageUrl());

    user.setImageUrl(null);
    save(user);
  }

  public List<User> findAllActiveUsers() {
    return userRepository.findAllActiveUsers();
  }

  public List<User> findSubordinatesByManagerUserId(final String userId) {
    return userRepository.findSubordinatesByManagerUserId(userId);
  }

  public User findByEmailWork(final String email) {
    return userRepository.findByEmailWork(email);
  }

  public List<User> findAllById(final List<String> ids) {
    return userRepository.findAllById(ids);
  }

  public void resendVerificationEmail(final String email) {
    final com.auth0.json.mgmt.users.User auth0User = auth0Helper.findByEmail(email);

    if (auth0User.isEmailVerified()) {
      throw new EmailAlreadyVerifiedException(
          String.format("The account %s is already verified.", email));
    }

    auth0Helper.sendVerificationEmail(auth0User.getId());
  }

  public boolean isCurrentActiveAnnouncementDismissed(final String userId, final String id) {
    final DismissedAt dismissedAt =
        dismissedAtService.findByUserIdAndSystemAnnouncementId(userId, id);
    return dismissedAt != null;
  }

  public void dismissCurrentActiveAnnouncement(final String userId, final String id) {
    final DismissedAt dismissed =
        dismissedAtService.findByUserIdAndSystemAnnouncementId(userId, id);
    if (dismissed == null) {
      final User user = findById(userId);
      final DismissedAt dismissedAt = new DismissedAt();
      dismissedAt.setUser(user);
      dismissedAt.setSystemAnnouncementId(id);
      dismissedAtService.save(dismissedAt);
    }
  }

  public boolean checkUserVerifiedEmail(final String email) {
    final com.auth0.json.mgmt.users.User auth0User = auth0Helper.findByEmail(email);
    return auth0User != null && auth0User.isEmailVerified();
  }

  public User findSuperUser(final String companyId) {
    return userRepository.findSuperUser(companyId);
  }

  public List<User> findUsersByCompanyIdAndUserRole(final String userRole) {
    return userRepository.findUsersByUserRole(userRole);
  }

  // When the user has the same name in the user list, the user's email address needs to be added
  public String getUserNameInUsers(final User currentUser, final List<User> users) {
    final String userName = currentUser.getUserPersonalInformation().getName();
    final String userEmail = " (" + currentUser.getUserContactInformation().getEmailWork() + ")";

    int count = 0;
    for (final User user : users) {
      final String name = user.getUserPersonalInformation().getName();
      if (userName.equals(name)) {
        count++;
      }
      if (count > 1) {
        return userName.concat(userEmail);
      }
    }
    return userName;
  }

  public Boolean checkPersonalInfoComplete(final String userId) {

    final Optional<User> user = userRepository.findById(userId);

    return user.isPresent() && verifyPersonalInfoComplete(user.get());
  }

  private boolean verifyPersonalInfoComplete(final User user) {
    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final UserAddress userAddress = userAddressService.findUserAddressByUserId(user.getId());
    return userPersonalInformation.getBirthDate() != null
        && userPersonalInformation.getSsn() != null
        && userAddress.getCountry() != null
        && userAddress.getStateProvince() != null
        && userAddress.getCity() != null
        && userAddress.getStreet1() != null;
  }

  public List<User> findRegisteredUsers() {
    final List<User> users = userRepository.findAll();
    return users.stream()
        .filter(user -> !user.getUserStatus().getStatus().equals(Status.PENDING_VERIFICATION))
        .collect(Collectors.toList());
  }

  public User findByInvitationEmailTokenAndResetPasswordToken(
      final String emailToken, final String passwordToken) {
    return userRepository.findByInvitationEmailTokenAndResetPasswordToken(
        emailToken, passwordToken);
  }

  public List<User> listCompanyAttendanceEnrolledUsers() {
    return userRepository.findAttendanceEnrolledUsers();
  }

  public List<User> listMessageOnNotSubmitTimeSheetUsers(final String periodId) {
    return userRepository.findUsersByPeriodIdAndTimeSheetStatusAndMessageOn(
        periodId, TimeSheetStatus.ACTIVE.name(), MessagingON.ON.getValue());
  }

  public List<User> listHasPendingTimeSheetsManagerAndAdmin(final String periodId) {
    final List<User> notApprovedUsers =
        userRepository.findUsersByPeriodIdAndTimeSheetStatus(
            periodId, TimeSheetStatus.SUBMITTED.name());
    if (CollectionUtils.isEmpty(notApprovedUsers)) {
      return new ArrayList<>();
    }

    final List<User> managers =
        userRepository.findManagersByPeriodIdAndTimeSheetStatus(
            periodId, TimeSheetStatus.SUBMITTED.name());
    final List<User> admins = userRepository.findUsersByUserRole(Role.ADMIN.name());
    final boolean managersIsEmpty = CollectionUtils.isEmpty(managers);
    final boolean adminsIsEmpty = CollectionUtils.isEmpty(admins);
    if (managersIsEmpty && adminsIsEmpty) {
      return new ArrayList<>();
    }
    if (managersIsEmpty) {
      return admins;
    }
    if (adminsIsEmpty) {
      return managers;
    }
    admins.forEach(
        admin -> {
          if (!managers.contains(admin)) {
            managers.add(admin);
          }
        });
    return managers;
  }

  public boolean isUserInvitationCapabilityFrozen(final String userId) {
    final User user = findActiveUserById(userId);
    final Timestamp frozenAt = user.getInvitationCapabilityFrozenAt();
    if (frozenAt == null) {
      return false;
    }
    final Timestamp lockedNextDay = DateUtil.getDayOfNext24Hours(frozenAt);
    return DateUtil.getCurrentTime().before(lockedNextDay);
  }

  public void freezeUserInvitationCapability(final String userId) {
    if (!isUserInvitationCapabilityFrozen(userId)) {
      final User user = findActiveUserById(userId);
      user.setInvitationCapabilityFrozenAt(DateUtil.getCurrentTime());
      userRepository.save(user);
    }
  }

  public void unfreezeUserInvitationCapability(final String userId) {
    final User user = findActiveUserById(userId);
    user.setInvitationCapabilityFrozenAt(null);
    userRepository.save(user);
  }

  public List<User> findUsersWithoutTimezone() {
    return userRepository.findUsersWithoutTimezone();
  }
}
