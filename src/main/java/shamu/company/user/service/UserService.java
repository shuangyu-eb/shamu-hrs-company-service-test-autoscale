package shamu.company.user.service;

import io.micrometer.core.instrument.util.StringUtils;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
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
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.PermissionUtils;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.exception.response.ErrorType;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.entity.Department;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.repository.CompanySizeRepository;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.s3.AwsUtil;
import shamu.company.s3.Type;
import shamu.company.scheduler.DynamicScheduler;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserRoleUpdateDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.DeactivationReasons;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAccessLevelEvent;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.UserAccessLevelEventRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

@Service
@Transactional
public class UserService {

  private static final String ERROR_MESSAGE = "User does not exist!";
  private final ITemplateEngine templateEngine;
  private final UserRepository userRepository;
  private final JobUserRepository jobUserRepository;

  private final UserStatusRepository userStatusRepository;
  private final UserEmergencyContactService userEmergencyContactService;
  private final UserAddressService userAddressService;
  private final UserContactInformationMapper userContactInformationMapper;
  private final UserAddressMapper userAddressMapper;


  private final UserPersonalInformationMapper userPersonalInformationMapper;
  private final CompanySizeRepository companySizeRepository;
  private final PaidHolidayService paidHolidayService;
  private final CompanyRepository companyRepository;
  private final DepartmentRepository departmentRepository;
  private final JobRepository jobRepository;

  private final EmailService emailService;
  private final UserCompensationRepository userCompensationRepository;
  private final Auth0Helper auth0Helper;
  private final AwsUtil awsUtil;
  private final UserMapper userMapper;
  private final AuthUserCacheManager authUserCacheManager;
  private final UserAccessLevelEventRepository userAccessLevelEventRepository;
  private final UserContactInformationRepository userContactInformationRepository;
  private final UserPersonalInformationRepository userPersonalInformationRepository;
  private final DynamicScheduler dynamicScheduler;

  private final UserRoleService userRoleService;
  private final JobUserService jobUserService;
  private final JobUserMapper jobUserMapper;

  private final PermissionUtils permissionUtils;


  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;
  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Autowired
  public UserService(final ITemplateEngine templateEngine, final UserRepository userRepository,
      final JobUserRepository jobUserRepository, final UserStatusRepository userStatusRepository,
      final EmailService emailService, final UserCompensationRepository userCompensationRepository,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserEmergencyContactService userEmergencyContactService,
      final UserAddressService userAddressService,
      final CompanySizeRepository companySizeRepository,
      @Lazy final PaidHolidayService paidHolidayService, final CompanyRepository companyRepository,
      final UserContactInformationMapper userContactInformationMapper,
      final UserAddressMapper userAddressMapper,
      final Auth0Helper auth0Helper,
      final UserAccessLevelEventRepository userAccessLevelEventRepository,
      final DepartmentRepository departmentRepository,
      final JobRepository jobRepository,
      final UserMapper userMapper,
      final AuthUserCacheManager authUserCacheManager,
      final UserContactInformationRepository userContactInformationRepository,
      final UserPersonalInformationRepository userPersonalInformationRepository,
      final DynamicScheduler dynamicScheduler,
      final AwsUtil awsUtil,
      final UserRoleService userRoleService,
      @Lazy final PermissionUtils permissionUtils,
      @Lazy final JobUserService jobUserService,
      final JobUserMapper jobUserMapper) {
    this.templateEngine = templateEngine;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.userStatusRepository = userStatusRepository;
    this.emailService = emailService;
    this.userCompensationRepository = userCompensationRepository;
    this.userEmergencyContactService = userEmergencyContactService;
    this.userAddressService = userAddressService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userAddressMapper = userAddressMapper;
    this.companySizeRepository = companySizeRepository;
    this.paidHolidayService = paidHolidayService;
    this.companyRepository = companyRepository;
    this.auth0Helper = auth0Helper;
    this.userAccessLevelEventRepository = userAccessLevelEventRepository;
    this.departmentRepository = departmentRepository;
    this.jobRepository = jobRepository;
    this.userMapper = userMapper;
    this.authUserCacheManager = authUserCacheManager;
    this.userContactInformationRepository = userContactInformationRepository;
    this.userPersonalInformationRepository = userPersonalInformationRepository;
    this.dynamicScheduler = dynamicScheduler;
    this.awsUtil = awsUtil;
    this.userRoleService = userRoleService;
    this.permissionUtils = permissionUtils;
    this.jobUserService = jobUserService;
    this.jobUserMapper = jobUserMapper;
  }

  public User findById(final String id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }

  public User findByUserId(final String userId) {
    return userRepository.findByUserId(userId);
  }

  public List<User> findByManagerUser(final User managerUser) {
    return userRepository.findByManagerUser(managerUser);
  }

  public void cacheUser(final String token, final String userId) {
    final User user = findById(userId);
    final AuthUser authUser = userMapper.convertToAuthUser(user);
    final List<String> permissions = auth0Helper
        .getPermissionBy(user.getId());
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

  public String getWelcomeEmail(final Context context) {
    return templateEngine.process("employee_invitation_email.html", context);
  }

  public Context getWelcomeEmailContext(
          final String welcomeMessage,
          final String resetPasswordToken) {
    return getWelcomeEmailContext(welcomeMessage, resetPasswordToken, "");
  }

  public Context getWelcomeEmailContext(
          String welcomeMessage,
          final String resetPasswordToken,
          final String toEmail) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    final String emailAddress = getEncodedEmailAddress(toEmail);
    String targetLink = frontEndAddress + "account/password/" + resetPasswordToken;
    if (!"".equals(emailAddress)) {
      targetLink += "/" + emailAddress;
    }
    context.setVariable("createPasswordAddress", targetLink);
    welcomeMessage = getFilteredWelcomeMessage(welcomeMessage);
    context.setVariable("welcomeMessage", welcomeMessage);
    return context;
  }

  private String getEncodedEmailAddress(final String emailAddress) {
    if (Strings.isBlank(emailAddress) || !Pattern.matches("^[a-zA-Z0-9@.+]*$", emailAddress)) {
      return "";
    }
    return emailAddress
            .replace("@", "-at-")
            .replaceAll("\\.", "-dot-");
  }

  private String getFilteredWelcomeMessage(String welcomeMessage) {
    if (Strings.isBlank(welcomeMessage)) {
      welcomeMessage = "";
    }
    return welcomeMessage
        .replaceAll("href\\s*=\\s*(['\"])\\s*(?!http[s]?).+?\\1", "#")
        .replaceAll("<script(.*)?>.*</script>", "");
  }

  public JobUserDto findEmployeeInfoByEmployeeId(final String id) {

    final User employee = findById(id);
    final JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser);
  }

  public Page<JobUserListItem> findAllEmployees(
      final String userId, final EmployeeListSearchCondition employeeListSearchCondition) {

    if (!permissionUtils.hasAuthority(Name.VIEW_DISABLED_USER.name())) {
      employeeListSearchCondition.setIncludeDeactivated(false);
    }

    final User currentUser = findByUserId(userId);
    final String companyId = currentUser.getCompany().getId();

    final Pageable paramPageable = getPageable(employeeListSearchCondition);
    return getAllEmployeesByCompany(employeeListSearchCondition,
        companyId, paramPageable, currentUser.getRole());
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

  public Page<JobUserListItem> getAllEmployeesByCompany(
      final EmployeeListSearchCondition employeeListSearchCondition, final String companyId,
      final Pageable pageable, final Role role) {
    return userRepository.getAllByCondition(
        employeeListSearchCondition, companyId, pageable, role);
  }

  public Page<JobUserListItem> findAllEmployeesByName(
      final EmployeeListSearchCondition employeeListSearchCondition, final String companyId) {
    final Pageable pageable = getPageable(employeeListSearchCondition);
    return userRepository.getAllByName(
        employeeListSearchCondition, companyId, pageable);
  }

  public User getOne(final String userId) {
    return userRepository.getOne(userId);
  }

  public User save(final User user) {
    return userRepository.save(user);
  }

  public List<JobUserDto> findAllJobUsers(final String companyId) {
    final List<User> policyEmployees = userRepository.findAllByCompanyId(companyId);

    return policyEmployees.stream()
        .map(
            user -> {
              JobUser employeeWithJob = jobUserRepository.findJobUserByUser(user);
              return new JobUserDto(user, employeeWithJob);
            })
        .collect(Collectors.toList());
  }

  public List<User> findAllUsersByCompany(final String companyId) {
    return userRepository.findAllByCompanyId(companyId);
  }

  public String getHeadPortrait(final String userId) {
    final User user = findById(userId);
    return user.getImageUrl();
  }

  public UserCompensation saveUserCompensation(final UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
  }

  public List<OrgChartDto> getOrgChart(final String userId, final String companyId) {
    final List<OrgChartDto> orgChartDtoList = new ArrayList<>();
    List<OrgChartDto> orgChartManagerItemList = new ArrayList<>();
    if (!StringUtils.isBlank(userId)) {
      final OrgChartDto manager = userRepository.findOrgChartItemByUserId(userId, companyId);
      orgChartManagerItemList.add(manager);
    } else {
      // retrieve company admin from database
      orgChartManagerItemList = userRepository
          .findOrgChartItemByManagerId(null, companyId);
    }
    if (!orgChartManagerItemList.isEmpty()) {
      for (final OrgChartDto manager : orgChartManagerItemList) {
        if (manager == null) {
          throw new ForbiddenException("User with id " + userId + " not found!");
        }

        final List<OrgChartDto> orgChartUserItemList = userRepository
            .findOrgChartItemByManagerId(manager.getId(), companyId);
        orgChartUserItemList.forEach((orgUser -> {
          final Integer directReportsCount = userRepository
              .findDirectReportsCount(orgUser.getId(), companyId);
          orgUser.setDirectReportsCount(directReportsCount);
        }));
        manager.setDirectReports(orgChartUserItemList);
        manager.setDirectReportsCount(orgChartUserItemList.size());
        orgChartDtoList.add(manager);
      }
    }
    return orgChartDtoList;
  }

  public AccountInfoDto getPreSetAccountInfoByUserId(final String id) {
    final User user = findById(id);

    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final UserPersonalInformationDto userPersonalInformationDto =
        userPersonalInformationMapper
            .convertToUserPersonalInformationDtoWithoutSsn(userPersonalInformation);

    final String headPortrait = user.getImageUrl();

    final UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    final UserContactInformation userContactInformation = user.getUserContactInformation();
    final UserContactInformationDto userContactInformationDto =
        userContactInformationMapper.convertToUserContactInformationDto(userContactInformation);

    final List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService
        .findUserEmergencyContacts(id);

    final List<UserEmergencyContactDto> userEmergencyContactDtos =
        userEmergencyContacts.stream()
            .map(UserEmergencyContactDto::new).collect(Collectors.toList());

    return new AccountInfoDto(
        userPersonalInformationDto, headPortrait,
        userAddressMapper.convertToUserAddressDto(userAddress),
        userContactInformationDto, userEmergencyContactDtos);
  }

  public Boolean createPasswordTokenExist(final String token) {
    return userRepository.existsByResetPasswordToken(token);
  }

  public void createPassword(final CreatePasswordDto createPasswordDto) {
    final User user = userRepository.findByEmailWork(createPasswordDto.getEmailWork());

    if (user == null
        || !createPasswordDto.getResetPasswordToken().equals(user.getResetPasswordToken())) {
      throw new ResourceNotFoundException(String
          .format("The user with email %s does not exist.", createPasswordDto.getEmailWork()));
    }

    final com.auth0.json.mgmt.users.User authUser =
        auth0Helper.getUserByUserIdFromAuth0(user.getId());

    if (authUser == null) {
      throw new ForbiddenException(String.format("Cannot find user with email %s",
          createPasswordDto.getEmailWork()));
    }

    auth0Helper.updatePassword(authUser, createPasswordDto.getNewPassword());
    auth0Helper.updateVerified(authUser, true);

    final UserStatus userStatus = userStatusRepository.findByName(Status.ACTIVE.name());
    user.setUserStatus(userStatus);
    user.setResetPasswordToken(null);
    userRepository.save(user);
  }

  public Page<JobUserListItem> getMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final String id) {
    final Pageable paramPageable = getPageable(employeeListSearchCondition);
    final User user = findById(id);
    return userRepository
        .getMyTeamByManager(employeeListSearchCondition, user, paramPageable);
  }

  public User updateUserRole(final String email, final UserRoleUpdateDto userRoleUpdateDto,
      final User user) {

    auth0Helper.login(email, userRoleUpdateDto.getPassWord(), null);

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

  private void deactivateUser(final UserStatusUpdateDto userStatusUpdateDto,
      final User user) {
    if (userStatusUpdateDto.getUserStatus().name().equals(Status.ACTIVE.name())) {
      userAccessLevelEventRepository.save(
          new UserAccessLevelEvent(user, user.getUserRole().getName()));

      user.setUserRole(userRoleService.getInactive());
      user.setUserStatus(userStatusRepository.findByName(
          Status.DISABLED.name()
      ));
      adjustUserManagerRelationshipBeforeDeleteOrDeactivate(user);
      userRepository.save(user);
    }
  }


  public User deactivateUser(final String email,
      final UserStatusUpdateDto userStatusUpdateDto,
      final User user) {

    auth0Helper.login(email, userStatusUpdateDto.getPassWord(), null);

    final Date deactivationDate = userStatusUpdateDto.getDeactivationDate();

    if (deactivationDate.toString().equals(LocalDate.now().toString())) {
      deactivateUser(userStatusUpdateDto, user);
    } else {
      dynamicScheduler.updateOrAddUniqueTriggerTask(
          "deactivate_" + user.getId(),
          deactivateUserTask(userStatusUpdateDto, user),
          userStatusUpdateDto.getDeactivationDate());
    }

    user.setDeactivatedAt(userStatusUpdateDto.getDeactivationDate());
    user.setDeactivationReason(new DeactivationReasons(userStatusUpdateDto
        .getDeactivationReason().getId()));
    userRepository.save(user);

    return userRepository.findActiveAndDeactivatedUserByUserId(user.getId());
  }

  public User findActiveAndDeactivatedUserByUserId(final String id) {
    return userRepository.findActiveAndDeactivatedUserByUserId(id);
  }

  public void deleteUser(final User employee) {

    adjustUserManagerRelationshipBeforeDeleteOrDeactivate(employee);

    auth0Helper.deleteUser(auth0Helper.getUserByUserIdFromAuth0(employee.getId()).getId());

    userRepository.delete(employee);

    userContactInformationRepository.delete(employee.getUserContactInformation());

    userPersonalInformationRepository.delete(employee.getUserPersonalInformation());
  }

  private void adjustUserManagerRelationshipBeforeDeleteOrDeactivate(final User user) {
    final List<User> teamEmployees = userRepository.findAllByManagerUserId(user.getId());
    if (!CollectionUtils.isEmpty(teamEmployees)) {
      if (user.getManagerUser() != null) {
        teamEmployees.forEach(
            employee -> employee.setManagerUser(user.getManagerUser()));
      } else {
        teamEmployees.forEach(
            employee -> {
              employee.setManagerUser(null);
              employee.setUserRole(userRoleService.getAdmin());
            });
      }
      userRepository.saveAll(teamEmployees);
    }
  }

  private Runnable deactivateUserTask(final UserStatusUpdateDto userStatusUpdateDto,
      final User user) {
    return () -> deactivateUser(userStatusUpdateDto, user);
  }

  public void signUp(final UserSignUpDto signUpDto) {
    final Optional<User> existingUser = userRepository.findById(signUpDto.getUserId());

    if (existingUser.isPresent()) {
      throw new DataIntegrityViolationException("User "
          + "already signed up successfully in previous attempts.");
    }

    if (companyRepository.existsByName(signUpDto.getCompanyName())) {
      throw new ForbiddenException("Company name already exists!", ErrorType.COMPANY_NAME_CONFLICT);
    }

    addSignUpInformation(signUpDto);
  }

  public void addSignUpInformation(final UserSignUpDto signUpDto) {
    final UserPersonalInformation userPersonalInformation = UserPersonalInformation.builder()
        .firstName(signUpDto.getFirstName())
        .lastName(signUpDto.getLastName())
        .build();

    final String emailAddress = findUserEmailOnAuth0(signUpDto.getUserId());
    final UserContactInformation userContactInformation = UserContactInformation.builder()
        .emailWork(emailAddress)
        .phoneWork(signUpDto.getPhone())
        .build();

    final CompanySize companySize = companySizeRepository
        .findById(signUpDto.getCompanySizeId())
        .orElseThrow(() -> new ResourceNotFoundException("CompanySize was not found."));

    Company company = Company.builder()
        .name(signUpDto.getCompanyName())
        .companySize(companySize)
        .build();
    company = companyRepository.save(company);

    Department department = new Department();
    department.setName(signUpDto.getDepartment());
    department.setCompany(company);
    department = departmentRepository.save(department);

    Job job = new Job();
    job.setTitle(signUpDto.getJobTitle());
    job.setDepartment(department);
    job = jobRepository.save(job);

    final UserStatus status = userStatusRepository.findByName(Status.ACTIVE.name());

    User user = User.builder()
        .userStatus(status)
        .userPersonalInformation(userPersonalInformation)
        .userContactInformation(userContactInformation)
        .company(company)
        .verifiedAt(Timestamp.valueOf(DateUtil.getLocalUtcTime()))
        .userRole(userRoleService.getAdmin())
        .salt(UuidUtil.getUuidString())
        .build();
    user.setId(signUpDto.getUserId());

    user = userRepository.save(user);

    final JobUser jobUser = new JobUser();
    jobUser.setUser(user);
    jobUser.setJob(job);
    jobUser.setCompany(company);
    jobUserRepository.save(jobUser);

    paidHolidayService.initDefaultPaidHolidays(user.getCompany());
  }

  private String findUserEmailOnAuth0(final String userId) {
    final com.auth0.json.mgmt.users.User auth0User = auth0Helper
        .getUserByUserIdFromAuth0(userId);
    if (auth0User == null) {
      throw new ForbiddenException("User not registered!");
    }

    return auth0User.getEmail();
  }

  public boolean hasUserAccess(final User currentUser, final String targetUserId) {
    final User targetUser = userRepository.findByIdAndCompanyId(targetUserId,
        currentUser.getCompany().getId());
    if (targetUser == null) {
      throw new ForbiddenException("Cannot find user!");
    }

    final Role userRole = currentUser.getRole();
    final Boolean isAdmin = Role.ADMIN == userRole;
    final User manager = targetUser.getManagerUser();

    final Boolean isManager = Role.MANAGER == userRole
        && manager != null && manager.getId().equals(currentUser.getId());

    return isAdmin || isManager;
  }

  public String getManagerUserIdById(final String userId) {
    return userRepository.getManagerUserIdById(userId);
  }

  public Boolean existsByEmailWork(final String email) {
    return auth0Helper.existsByEmail(email);
  }

  public void updatePassword(final ChangePasswordDto changePasswordDto, final String userId) {
    final com.auth0.json.mgmt.users.User user = auth0Helper
        .getUserByUserIdFromAuth0(userId);

    final String emailAddress = user.getEmail();
    checkPassword(emailAddress, changePasswordDto.getPassWord());
    if (changePasswordDto.getPassWord().equals(changePasswordDto.getNewPassword())) {
      throw new ForbiddenException("New password cannot be the same as the old one.");
    }
    auth0Helper.updatePassword(user, changePasswordDto.getNewPassword());

    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    final String emailContent = templateEngine.process("password_change_email.html", context);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final Email notificationEmail = new Email(systemEmailAddress,
        emailAddress, "Password Changed!",
        emailContent, sendDate);
    emailService.saveAndScheduleEmail(notificationEmail);

  }

  public void checkPassword(final String email, final String password) {
    if (!auth0Helper.isPasswordValid(email, password)) {
      throw new ForbiddenException("Wrong email or password.");
    }
  }

  public void sendChangeWorkEmail(final String userId, final String newEmail) {

    final User user = findById(userId);

    if (user.getUserContactInformation().getEmailWork().equals(newEmail)) {
      throw new ForbiddenException(" your new work email should be different");
    }

    if (!auth0Helper.existsByEmail(newEmail)) {

      user.setUserStatus(
          userStatusRepository.findByName(String.valueOf(Status.CHANGING_EMAIL_VERIFICATION)));

      user.setChangeWorkEmail(newEmail);

      handleEmail(user);

      userRepository.save(user);
    } else {
      throw new ForbiddenException(String.format(" %s already be used by other", newEmail));
    }
  }

  public void sendVerifyChangeWorkEmail(final User user) {
    final String newEmailToken = handleEmail(user);
    user.setChangeWorkEmailToken(newEmailToken);
    userRepository.save(user);
  }

  private String handleEmail(final User user) {
    final String newEmailToken = UUID.randomUUID().toString();
    user.setChangeWorkEmailToken(newEmailToken);

    final String emailContent = getVerifiedEmail(newEmailToken);

    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());

    final Email verifyChangeWorkEmail = new Email(systemEmailAddress,
        user.getChangeWorkEmail(), "Verify New Work Email", emailContent, sendDate);

    emailService.saveAndScheduleEmail(verifyChangeWorkEmail);
    return newEmailToken;
  }

  public boolean changeWorkEmailTokenExist(final String token) {

    final User currentUser = userRepository.findByChangeWorkEmailToken(token);

    if (userRepository.existsByChangeWorkEmailToken(token)) {
      final com.auth0.json.mgmt.users.User user = auth0Helper
          .getUserByUserIdFromAuth0(currentUser.getId());

      auth0Helper.updateUserEmail(user, currentUser.getChangeWorkEmail());

      currentUser.setVerifyChangeWorkEmailAt(Timestamp.valueOf(LocalDateTime.now()));
      currentUser.getUserContactInformation().setEmailWork(currentUser.getChangeWorkEmail());
      currentUser.setChangeWorkEmailToken(null);
      currentUser.setUserStatus(userStatusRepository.findByName(
          String.valueOf(Status.ACTIVE)));

      final UserContactInformationDto updateEmailWork = new UserContactInformationDto();
      updateEmailWork.setEmailWork(currentUser.getChangeWorkEmail());
      final UserContactInformation updateContactInfo =
          userContactInformationMapper.updateFromUserContactInformationDto(
              currentUser.getUserContactInformation(), updateEmailWork);

      userContactInformationRepository.save(updateContactInfo);

      userRepository.save(currentUser);

      return true;
    }

    return false;

  }

  public CurrentUserDto getCurrentUserInfo(final String userId) {
    final User user = findByUserId(userId);
    return getCurrentUserDto(user);
  }

  private CurrentUserDto getCurrentUserDto(final User user) {
    final List<User> teamMembers = findByManagerUser(user);
    final List<String> teamMemberIds = teamMembers.stream().map(User::getId)
        .collect(Collectors.toList());
    if (user.getVerifiedAt() == null) {
      return CurrentUserDto.builder()
          .id(user.getId())
          .verified(false)
          .build();
    }

    return CurrentUserDto.builder()
        .id(user.getId())
        .teamMembers(teamMemberIds)
        .name(user.getUserPersonalInformation().getName())
        .imageUrl(user.getImageUrl())
        .verified(user.getVerifiedAt() != null)
        .build();
  }

  public CurrentUserDto getMockUserInfo(final String userId) {
    final User user = findById(userId);
    return getCurrentUserDto(user);
  }

  public void sendResetPasswordEmail(final String email) {
    final User targetUser = userRepository.findByEmailWork(email);
    if (targetUser == null) {
      throw new ForbiddenException("User account does not exist!");
    }

    final com.auth0.json.mgmt.users.User user = auth0Helper
        .getUserByUserIdFromAuth0(targetUser.getId());
    if (user == null) {
      throw new ForbiddenException("User account does not exist!");
    }

    final String passwordRestToken = UUID.randomUUID().toString();
    final String emailContent = getResetPasswordEmail(passwordRestToken);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final Email verifyEmail = new Email(systemEmailAddress, email, "Password Reset!",
        emailContent, sendDate);
    emailService.saveAndScheduleEmail(verifyEmail);
    targetUser.setResetPasswordToken(passwordRestToken);
    userRepository.save(targetUser);
  }

  public void resetPassword(final UpdatePasswordDto updatePasswordDto) {
    final User user = userRepository
        .findByResetPasswordToken(updatePasswordDto.getResetPasswordToken());
    if (user == null) {
      throw new ForbiddenException("Reset password Forbidden");
    }

    final com.auth0.json.mgmt.users.User auth0User = auth0Helper
        .getUserByUserIdFromAuth0(user.getId());

    if (auth0User == null) {
      throw new ForbiddenException("Email account does not exist.");
    }

    final String email = user.getUserContactInformation().getEmailWork();
    if (auth0Helper.isPasswordValid(email, updatePasswordDto.getNewPassword())) {
      throw new ForbiddenException("New password cannot be the same as the old one.");
    }

    auth0Helper.updatePassword(auth0User, updatePasswordDto.getNewPassword());

    user.setResetPasswordToken(null);
    userRepository.save(user);
  }

  private String getResetPasswordEmail(final String passwordRestToken) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "passwordResetAddress", String.format("account/reset-password/%s", passwordRestToken));
    return templateEngine.process("password_reset_email.html", context);
  }

  private String getVerifiedEmail(final String changePasswordToken) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "changePasswordToken", String.format("account/change-work-email/%s", changePasswordToken));
    return templateEngine.process("verify_change_work_email.html", context);
  }

  public String handleUploadFile(final String id, final MultipartFile file) {
    final String path = awsUtil.uploadFile(file, Type.IMAGE);

    if (Strings.isBlank(path)) {
      return null;
    }

    final User user = findById(id);
    final String originalPath = user.getImageUrl();
    if (originalPath != null) {
      awsUtil.deleteFile(originalPath);
    }

    user.setImageUrl(path);
    save(user);

    return path;
  }

  public Boolean isOldPwdCorrect(final String password, final String email) {
    return auth0Helper.isPasswordValid(email, password);
  }

  public Context findWelcomeEmailPreviewContext(final String id,
      final String welcomeEmailPersonalMessage) {
    final User currentUser = findById(id);
    final Context context = getWelcomeEmailContext(welcomeEmailPersonalMessage,
        null);
    context.setVariable("createPasswordAddress", "#");
    context.setVariable("companyName", currentUser.getCompany().getName());
    return context;
  }

  public List<User> findByCompanyId(final String companyId) {
    return userRepository.findByCompanyId(companyId);
  }

  public List<User> findDirectReportsByManagerUserId(
          final String companyId, final String userId) {
    return userRepository.findDirectReportsByManagerUserId(companyId, userId);
  }

  public User findByEmailWork(final String email) {
    return userRepository.findByEmailWork(email);
  }

  public List<User> findAllById(final List<String> ids) {
    return userRepository.findAllById(ids);
  }

  public List<User> findAllByCompanyId(final String companyId) {
    return userRepository.findAllByCompanyId(companyId);
  }

  public BasicJobInformationDto findJobMessage(final String targetUserId,
                                              final String authUserId) {
    final JobUser target = jobUserService.getJobUserByUserId(targetUserId);

    if (target == null) {
      final User targetUser = findById(targetUserId);
      return userMapper.convertToBasicJobInformationDto(targetUser);
    }

    // The user's full job message can only be accessed by admin, the manager and himself.
    final User currentUser = findById(authUserId);
    final Role userRole = currentUser.getRole();
    if (authUserId.equals(targetUserId) || userRole == Role.ADMIN) {
      return jobUserMapper.convertToJobInformationDto(target);
    }

    if (userRole == Role.MANAGER && target.getUser().getManagerUser() != null
            && authUserId.equals(target.getUser().getManagerUser().getId())) {
      return jobUserMapper.convertToJobInformationDto(target);
    }

    return jobUserMapper
            .convertToBasicJobInformationDto(target);
  }
}
