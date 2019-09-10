package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.CompanyRepository;
import shamu.company.company.CompanySizeRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanySize;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserSignUpDto;
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
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.pojo.ChangePasswordPojo;
import shamu.company.user.pojo.UserRoleUpdatePojo;
import shamu.company.user.pojo.UserStatusUpdatePojo;
import shamu.company.user.repository.UserAccessLevelEventRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  private static final String ERROR_MESSAGE = "User does not exist!";
  private final ITemplateEngine templateEngine;
  private final UserRepository userRepository;
  private final JobUserRepository jobUserRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserEmergencyContactService userEmergencyContactService;
  private final UserAddressService userAddressService;
  private final UserContactInformationMapper userContactInformationMapper;
  private final UserAddressMapper userAddressMapper;
  private final TaskScheduler taskScheduler;


  private final UserPersonalInformationMapper userPersonalInformationMapper;
  private final CompanySizeRepository companySizeRepository;
  private final PaidHolidayService paidHolidayService;
  private final CompanyRepository companyRepository;

  private final EmailService emailService;
  private final UserCompensationRepository userCompensationRepository;
  private final Auth0Util auth0Util;
  private final UserAccessLevelEventRepository userAccessLevelEventRepository;

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;
  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Autowired
  public UserServiceImpl(final ITemplateEngine templateEngine, final UserRepository userRepository,
      final JobUserRepository jobUserRepository, final UserStatusRepository userStatusRepository,
      final EmailService emailService, final UserCompensationRepository userCompensationRepository,
      final UserRoleRepository userRoleRepository,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserEmergencyContactService userEmergencyContactService,
      final UserAddressService userAddressService,
      final CompanySizeRepository companySizeRepository,
      @Lazy final PaidHolidayService paidHolidayService, final CompanyRepository companyRepository,
      final UserContactInformationMapper userContactInformationMapper,
      final UserAddressMapper userAddressMapper,
      final Auth0Util auth0Util,
      final UserAccessLevelEventRepository userAccessLevelEventRepository,
      final TaskScheduler taskScheduler) {
    this.templateEngine = templateEngine;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.userStatusRepository = userStatusRepository;
    this.emailService = emailService;
    this.userCompensationRepository = userCompensationRepository;
    this.userRoleRepository = userRoleRepository;
    this.userEmergencyContactService = userEmergencyContactService;
    this.userAddressService = userAddressService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userAddressMapper = userAddressMapper;
    this.companySizeRepository = companySizeRepository;
    this.paidHolidayService = paidHolidayService;
    this.companyRepository = companyRepository;
    this.auth0Util = auth0Util;
    this.userAccessLevelEventRepository = userAccessLevelEventRepository;
    this.taskScheduler = taskScheduler;
  }

  @Override
  public User findUserById(final Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }

  @Override
  public User findByUserId(final String userId) {
    return userRepository.findByUserId(userId);
  }

  @Override
  public List<User> findByManagerUser(final User managerUser) {
    return userRepository.findByManagerUser(managerUser);
  }

  @Override
  public User findUserByEmail(final String email) {
    return userRepository.findByEmailWork(email);
  }

  @Override
  public User findUserByUserIdAndStatus(final String userId, final Status userStatus) {
    return userRepository.findByUserIdAndStatus(userId, userStatus.name());
  }

  @Override
  public User findUserByUserPersonalInformationId(final Long userPersonalInformationId) {
    return userRepository.findByUserPersonalInformationId(userPersonalInformationId);
  }

  @Override
  public User findUserByUserContactInformationId(final Long userContactInformationId) {
    return userRepository.findByUserContactInformationId(userContactInformationId);
  }

  @Override
  public List<User> findDirectReportsByManagerId(final Long id) {
    return userRepository.findAllByManagerUserId(id);
  }

  @Override
  public String getWelcomeEmail(final Context context) {
    return templateEngine.process("employee_invitation_email.html", context);
  }

  @Override
  public Context getWelcomeEmailContext(String welcomeMessage, final String resetPasswordToken) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "createPasswordAddress",
        frontEndAddress + "account/password/" + resetPasswordToken);
    welcomeMessage = getFilteredWelcomeMessage(welcomeMessage);
    context.setVariable("welcomeMessage", welcomeMessage);
    return context;
  }

  private String getFilteredWelcomeMessage(String welcomeMessage) {
    if (Strings.isBlank(welcomeMessage)) {
      welcomeMessage = "";
    }
    final Pattern scriptPattern = Pattern.compile("<script(.*)?>.*</script>");
    final Matcher scriptMatcher = scriptPattern.matcher(welcomeMessage);
    return scriptMatcher.replaceAll("");
  }

  @Override
  public JobUserDto findEmployeeInfoByEmployeeId(final Long id) {

    final User employee =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
    final JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser);
  }

  @Override
  public User findEmployeeInfoByUserId(final Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }

  @Override
  public Page<JobUserListItem> getAllEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final Company company, final Boolean isAdmin) {
    final String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    final String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    final Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);

    return getAllEmployeesByCompany(employeeListSearchCondition, company, paramPageable, isAdmin);
  }

  @Override
  public Page<JobUserListItem> getAllEmployeesByCompany(
      final EmployeeListSearchCondition employeeListSearchCondition, final Company company,
      final Pageable pageable, final Boolean isAdmin) {
    final Long companyId = company.getId();
    return userRepository.getAllByCondition(
        employeeListSearchCondition, companyId, pageable, isAdmin);
  }

  @Override
  public User getOne(final Long userId) {
    return userRepository.getOne(userId);
  }

  @Override
  public void save(final User user) {
    userRepository.save(user);
  }

  @Override
  public void saveUserWithRole(final User user, final Role role) {
    final UserRole userRole = userRoleRepository.findByName(role.name());
    user.setUserRole(userRole);
    save(user);
  }

  @Override
  public List<JobUserDto> findAllJobUsers(final Company company) {
    final List<User> policyEmployees = userRepository.findAllByCompanyId(company.getId());

    return policyEmployees.stream()
        .map(
            (user) -> {
              JobUser employeeWithJob = jobUserRepository.findJobUserByUser(user);
              return new JobUserDto(user, employeeWithJob);
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<User> findAllUsersByCompany(final Company company) {
    return userRepository.findAllByCompanyId(company.getId());
  }

  @Override
  public String getHeadPortrait(final Long userId) {
    final User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
    return user.getImageUrl();
  }

  @Override
  public UserCompensation saveUserCompensation(final UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
  }

  @Override
  public OrgChartDto getOrgChart(final Long userId, final Company currentCompany) {
    OrgChartDto manager = null;
    if (userId != null) {
      manager = userRepository.findOrgChartItemByUserId(userId, currentCompany.getId());
    } else {
      // retrieve company admin from database
      final List<OrgChartDto> orgChartItemList = userRepository
          .findOrgChartItemByManagerId(null, currentCompany.getId());
      if (!orgChartItemList.isEmpty()) {
        manager = orgChartItemList.get(0);
      }
    }

    if (manager == null) {
      throw new ForbiddenException("User with id " + userId + " not found!");
    }

    final List<OrgChartDto> orgChartItemList = userRepository
        .findOrgChartItemByManagerId(manager.getId(), currentCompany.getId());
    orgChartItemList.forEach((orgUser -> {
      final Integer directReportsCount = userRepository
          .findDirectReportsCount(orgUser.getId(), currentCompany.getId());
      orgUser.setDirectReportsCount(directReportsCount);
    }));
    manager.setDirectReports(orgChartItemList);
    manager.setDirectReportsCount(orgChartItemList.size());
    return manager;
  }

  @Override
  public AccountInfoDto getPreSetAccountInfoByUserId(final Long id) {
    final User user = findUserById(id);

    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final UserPersonalInformationDto userPersonalInformationDto =
        userPersonalInformationMapper.convertToUserPersonalInformationDto(userPersonalInformation);

    final String headPortrait = user.getImageUrl();

    final UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    final UserContactInformation userContactInformation = user.getUserContactInformation();
    final UserContactInformationDto userContactInformationDto =
        userContactInformationMapper.convertToUserContactInformationDto(userContactInformation);

    final List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService
        .getUserEmergencyContacts(id);

    final List<UserEmergencyContactDto> userEmergencyContactDtos =
        userEmergencyContacts.stream()
            .map(UserEmergencyContactDto::new).collect(Collectors.toList());

    return new AccountInfoDto(
        userPersonalInformationDto, headPortrait,
        userAddressMapper.convertToUserAddressDto(userAddress),
        userContactInformationDto, userEmergencyContactDtos);
  }

  @Override
  public Boolean createPasswordTokenExist(final String token) {
    return userRepository.existsByResetPasswordToken(token);
  }

  @Override
  public void createPassword(final CreatePasswordDto createPasswordDto) {
    final com.auth0.json.mgmt.users.User user = auth0Util
        .getUserByEmailFromAuth0(createPasswordDto.getEmailWork());

    if (user == null) {
      throw new ForbiddenException(String.format("Can not find user with email %s",
          createPasswordDto.getEmailWork()));
    }

    final String userId = auth0Util.getUserId(user);
    final User targetUser = userRepository.findByUserId(userId);
    if (targetUser == null
        || !createPasswordDto.getResetPasswordToken().equals(targetUser.getResetPasswordToken())) {
      throw new ResourceNotFoundException(String
          .format("The user with email %s does not exist.", createPasswordDto.getEmailWork()));
    }

    auth0Util.updatePassword(user, createPasswordDto.getNewPassword());
    auth0Util.updateVerified(user, true);

    final UserStatus userStatus = userStatusRepository.findByName(Status.ACTIVE.name());
    targetUser.setUserStatus(userStatus);
    targetUser.setResetPasswordToken(null);
    userRepository.save(targetUser);
  }

  @Override
  public Page<JobUserListItem> getMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final User user) {
    final String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    final String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    final Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);
    return userRepository
        .getMyTeamByManager(employeeListSearchCondition, user, paramPageable);
  }

  @Override
  public User updateUserRole(final User currentUser, final UserRoleUpdatePojo userRoleUpdatePojo,
      final User user) {

    auth0Util.login(currentUser.getUserContactInformation().getEmailWork(),
        userRoleUpdatePojo.getPassWord());

    final String updateUserRole;
    if (userRoleUpdatePojo.getUserRole() == UserRole.Role.ADMIN) {
      if (!userRepository.findAllByManagerUserId(user.getId()).isEmpty()) {
        updateUserRole = UserRole.Role.MANAGER.name();
      } else {
        updateUserRole = UserRole.Role.NON_MANAGER.name();
      }
    } else {
      updateUserRole = UserRole.Role.ADMIN.name();
    }
    user.setUserRole(userRoleRepository.findByName(updateUserRole));
    userRepository.save(user);
    return user;
  }

  @Override
  public User inactivateUser(final User currentUser,
      final UserStatusUpdatePojo userStatusUpdatePojo,
      final User user) {

    auth0Util.login(currentUser.getUserContactInformation().getEmailWork(),
        userStatusUpdatePojo.getPassWord());

    final Date deactivationDate = userStatusUpdatePojo.getDeactivationDate();

    if (deactivationDate.toString().equals(LocalDate.now().toString())) {
      inactivateUser(userStatusUpdatePojo, user);
    } else {
      taskScheduler.schedule(
          inactivateUserTask(userStatusUpdatePojo, user),
          userStatusUpdatePojo.getDeactivationDate());
    }
    return userRepository.findByUserId(user.getUserId());
  }

  private void inactivateUser(final UserStatusUpdatePojo userStatusUpdatePojo,
      final User user) {
    if (userStatusUpdatePojo.getUserStatus().name().equals(Status.ACTIVE.name())) {
      // inactivate user in auth0
      final com.auth0.json.mgmt.users.User auth0User = auth0Util
          .getUserByEmailFromAuth0(user.getUserContactInformation().getEmailWork());
      auth0Util.inactivate(auth0User.getId());

      userAccessLevelEventRepository.save(
          new UserAccessLevelEvent(user, user.getRole().getValue()));

      user.setUserStatus(userStatusRepository.findByName(
          Status.DISABLED.name()
      ));
      final UserRole userRole = userRoleRepository.findByName(Role.INACTIVATE.name());
      user.setUserRole(userRole);
      user.setDeactivatedAt(userStatusUpdatePojo.getDeactivationDate());
      user.setDeactivationReason(new DeactivationReasons(userStatusUpdatePojo
          .getDeactivationReason().getId()));
      userRepository.save(user);
    }
  }

  private Runnable inactivateUserTask(final UserStatusUpdatePojo userStatusUpdatePojo,
      final User user) {
    return () -> inactivateUser(userStatusUpdatePojo, user);
  }

  @Override
  public void signUp(final UserSignUpDto signUpDto) {
    final UserPersonalInformation userPersonalInformation = UserPersonalInformation.builder()
        .firstName(signUpDto.getFirstName())
        .lastName(signUpDto.getLastName())
        .build();

    final UserContactInformation userContactInformation = UserContactInformation.builder()
        .emailWork(signUpDto.getEmail())
        .phoneWork(signUpDto.getPhone())
        .build();

    final CompanySize companySize = companySizeRepository
        .findCompanySizeByName(signUpDto.getCompanySize());

    Company company = Company.builder()
        .name(signUpDto.getCompanyName())
        .companySize(companySize)
        .build();
    company = companyRepository.save(company);

    final UserRole role = userRoleRepository.findByName(Role.ADMIN.name());

    final UserStatus status = userStatusRepository.findByName(Status.ACTIVE.name());

    final String employeeNumber = getEmployeeNumber(company.getName(), 1);

    User user = User.builder()
        .userId(signUpDto.getUserId())
        .userRole(role)
        .userStatus(status)
        .userPersonalInformation(userPersonalInformation)
        .userContactInformation(userContactInformation)
        .employeeNumber(employeeNumber)
        .company(company)
        .emailWork(signUpDto.getEmail())
        .build();

    user = userRepository.save(user);

    paidHolidayService.initDefaultPaidHolidays(user.getCompany());
  }

  @Override
  public boolean hasUserAccess(final User currentUser, final Long targetUserId) {
    final User targetUser = userRepository.findByIdAndCompanyId(targetUserId,
        currentUser.getCompany().getId());
    if (targetUser == null) {
      throw new ForbiddenException("Can not find user!");
    }

    final Boolean isAdmin = Role.ADMIN.name().equals(currentUser.getRole().name());
    if (isAdmin) {
      return true;
    }
    return false;
  }

  @Override
  public Long getManagerUserIdById(final Long userId) {
    return userRepository.getManagerUserIdById(userId);
  }

  @Override
  public Boolean existsByEmailWork(final String email) {
    return auth0Util.getUserByEmailFromAuth0(email) != null;
  }

  @Override
  public void updatePassword(final ChangePasswordPojo changePasswordPojo, final User currentUser) {
    final com.auth0.json.mgmt.users.User user = auth0Util
        .getUserByEmailFromAuth0(currentUser.getUserContactInformation().getEmailWork());

    auth0Util.login(currentUser.getUserContactInformation().getEmailWork(),
        changePasswordPojo.getPassWord());

    auth0Util.updatePassword(user, changePasswordPojo.getNewPassword());

  }

  @Override
  public CurrentUserDto getCurrentUserInfo(final String userId) {
    final User user = findByUserId(userId);
    final List<User> teamMembers = findByManagerUser(user);
    final List<Long> teamMemberIds = teamMembers.stream().map(BaseEntity::getId)
        .collect(Collectors.toList());

    return CurrentUserDto.builder()
        .id(user.getId())
        .teamMembers(teamMemberIds)
        .name(user.getUserPersonalInformation().getName())
        .imageUrl(user.getImageUrl())
        .build();
  }

  @Override
  public String getEmployeeNumber(final String companyName, final Integer employeeNumber) {
    if (companyName.length() <= 3) {
      return String.format("%s%06d", companyName, employeeNumber);
    }

    final String employeeNumberPrefix = companyName.substring(0, 3);
    return String.format("%s%06d", employeeNumberPrefix, employeeNumber);
  }

  @Override
  public void sendResetPasswordEmail(final String email) {
    final com.auth0.json.mgmt.users.User user = auth0Util.getUserByEmailFromAuth0(email);
    final User targetUser = userRepository.findByEmailWork(email);

    if (user == null || targetUser == null) {
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

  @Override
  public void resetPassword(final UpdatePasswordDto updatePasswordDto) {
    final User user = userRepository
        .findByResetPasswordToken(updatePasswordDto.getResetPasswordToken());
    if (user == null) {
      throw new ForbiddenException("Reset password Forbidden");
    }

    final com.auth0.json.mgmt.users.User currentUser = auth0Util
        .getUserByEmailFromAuth0(user.getUserContactInformation().getEmailWork());

    if (currentUser == null) {
      throw new ForbiddenException("Email account does not exist.");
    }

    final String email = user.getUserContactInformation().getEmailWork();
    if (auth0Util.isPasswordValid(email, updatePasswordDto.getNewPassword())) {
      throw new ForbiddenException("New password can not be the same as the old one.");
    }

    auth0Util.updatePassword(currentUser, updatePasswordDto.getNewPassword());

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
}
