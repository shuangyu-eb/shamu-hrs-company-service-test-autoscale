package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.CompanyRepository;
import shamu.company.company.CompanySizeRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.entity.Department;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.repository.JobRepository;
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
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.DateUtil;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  private static final String ERROR_MESSAGE = "User does not exist!";
  private final ITemplateEngine templateEngine;
  private final UserRepository userRepository;
  private final JobUserRepository jobUserRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserEmergencyContactService userEmergencyContactService;
  private final UserAddressService userAddressService;
  private final UserContactInformationMapper userContactInformationMapper;
  private final UserAddressMapper userAddressMapper;
  private final TaskScheduler taskScheduler;


  private final UserPersonalInformationMapper userPersonalInformationMapper;
  private final CompanySizeRepository companySizeRepository;
  private final PaidHolidayService paidHolidayService;
  private final CompanyRepository companyRepository;
  private final DepartmentRepository departmentRepository;
  private final JobRepository jobRepository;

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
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserEmergencyContactService userEmergencyContactService,
      final UserAddressService userAddressService,
      final CompanySizeRepository companySizeRepository,
      @Lazy final PaidHolidayService paidHolidayService, final CompanyRepository companyRepository,
      final UserContactInformationMapper userContactInformationMapper,
      final UserAddressMapper userAddressMapper,
      final Auth0Util auth0Util,
      final UserAccessLevelEventRepository userAccessLevelEventRepository,
      final TaskScheduler taskScheduler,
      final DepartmentRepository departmentRepository,
      final JobRepository jobRepository) {
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
    this.auth0Util = auth0Util;
    this.userAccessLevelEventRepository = userAccessLevelEventRepository;
    this.taskScheduler = taskScheduler;
    this.departmentRepository = departmentRepository;
    this.jobRepository = jobRepository;
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
    return welcomeMessage
        .replaceAll("href\\s*=\\s*(['\"])\\s*(?!http[s]?).+?\\1", "#")
        .replaceAll("<script(.*)?>.*</script>", "");
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
      final Long companyId, final Role role) {
    final String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    final String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    final Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);

    return getAllEmployeesByCompany(employeeListSearchCondition, companyId, paramPageable, role);
  }

  @Override
  public Page<JobUserListItem> getAllEmployeesByCompany(
      final EmployeeListSearchCondition employeeListSearchCondition, final Long companyId,
      final Pageable pageable, final Role role) {
    return userRepository.getAllByCondition(
        employeeListSearchCondition, companyId, pageable, role);
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
    auth0Util.updateRoleWithEmail(user.getUserContactInformation().getEmailWork(),
        role.getValue());
    save(user);
  }

  @Override
  public List<JobUserDto> findAllJobUsers(final Long companyId) {
    final List<User> policyEmployees = userRepository.findAllByCompanyId(companyId);

    return policyEmployees.stream()
        .map(
            (user) -> {
              JobUser employeeWithJob = jobUserRepository.findJobUserByUser(user);
              return new JobUserDto(user, employeeWithJob);
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<User> findAllUsersByCompany(final Long companyId) {
    return userRepository.findAllByCompanyId(companyId);
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
  public List<OrgChartDto> getOrgChart(final Long userId, final Long companyId) {
    final List<OrgChartDto> orgChartDtoList = new ArrayList<>();
    List<OrgChartDto> orgChartManagerItemList = new ArrayList<>();
    if (userId != null) {
      OrgChartDto manager = null;
      manager = userRepository.findOrgChartItemByUserId(userId, companyId);
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
      throw new ForbiddenException(String.format("Cannot find user with email %s",
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
    targetUser.setVerifiedAt(Timestamp.valueOf(DateUtil.getLocalUtcTime()));
    userRepository.save(targetUser);
  }

  @Override
  public Page<JobUserListItem> getMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final Long id) {
    final String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    final String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    final Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);
    final User user =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
    return userRepository
        .getMyTeamByManager(employeeListSearchCondition, user, paramPageable);
  }

  @Override
  public User updateUserRole(final String email, final UserRoleUpdatePojo userRoleUpdatePojo,
      final User user) {

    auth0Util.login(email, userRoleUpdatePojo.getPassWord());

    final String updateUserRole;
    if (userRoleUpdatePojo.getUserRole() == Role.ADMIN) {
      if (!userRepository.findAllByManagerUserId(user.getId()).isEmpty()) {
        updateUserRole = Role.MANAGER.getValue();
      } else {
        updateUserRole = Role.EMPLOYEE.getValue();
      }
    } else {
      updateUserRole = Role.ADMIN.getValue();
    }
    auth0Util.updateRoleWithEmail(user.getUserContactInformation().getEmailWork(),
        updateUserRole);

    return user;
  }

  @Override
  public User inactivateUser(final String email,
      final UserStatusUpdatePojo userStatusUpdatePojo,
      final User user) {

    auth0Util.login(email, userStatusUpdatePojo.getPassWord());

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
      auth0Util.updateAuthRole(auth0User.getId(), Role.INACTIVATE.name());

      final Role previousUserRole = auth0Util
          .getUserRole(user.getUserContactInformation().getEmailWork());
      userAccessLevelEventRepository.save(
          new UserAccessLevelEvent(user, previousUserRole.getValue()));

      user.setUserStatus(userStatusRepository.findByName(
          Status.DISABLED.name()
      ));
      List<User> teamEmployees = userRepository.findAllByManagerUserId(user.getId());
      final User manager = user.getManagerUser();

      if (teamEmployees.size() > 0) {
        if (manager != null) {
          teamEmployees = teamEmployees.stream().map(
              employee -> {
                employee.setManagerUser(manager);
                return employee;
              }
          ).collect(Collectors.toList());
        } else {
          teamEmployees = teamEmployees.stream().map(
              employee -> {
                employee.setManagerUser(null);
                auth0Util.updateAuthRole(employee.getUserId(), Role.ADMIN.name());
                return employee;
              }
          ).collect(Collectors.toList());
        }

        userRepository.saveAll(teamEmployees);

      }
      user.setDeactivatedAt(userStatusUpdatePojo.getDeactivationDate());
      user.setDeactivationReason(new DeactivationReasons(userStatusUpdatePojo
          .getDeactivationReason().getId()));
      userRepository.save(user);

      auth0Util.updateRoleWithEmail(user.getUserContactInformation().getEmailWork(),
          Role.INACTIVATE.getValue());
    }
  }

  private Runnable inactivateUserTask(final UserStatusUpdatePojo userStatusUpdatePojo,
      final User user) {
    return () -> inactivateUser(userStatusUpdatePojo, user);
  }

  @Override
  public void signUp(final UserSignUpDto signUpDto) {
    final User existingUser = userRepository.findByUserId(signUpDto.getUserId());

    if (existingUser != null) {
      throw new DataIntegrityViolationException("User "
          + "already signed up successfully in previous attempts.");
    }

    addSignUpInformation(signUpDto);
  }

  public void addSignUpInformation(final UserSignUpDto signUpDto) {
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
        .userId(signUpDto.getUserId())
        .userStatus(status)
        .userPersonalInformation(userPersonalInformation)
        .userContactInformation(userContactInformation)
        .company(company)
        .emailWork(signUpDto.getEmail())
        .verifiedAt(Timestamp.valueOf(DateUtil.getLocalUtcTime()))
        .build();

    user = userRepository.save(user);

    final JobUser jobUser = new JobUser();
    jobUser.setUser(user);
    jobUser.setJob(job);
    jobUser.setCompany(company);
    jobUserRepository.save(jobUser);

    paidHolidayService.initDefaultPaidHolidays(user.getCompany());
  }

  @Override
  public boolean hasUserAccess(final User currentUser, final Long targetUserId) {
    final User targetUser = userRepository.findByIdAndCompanyId(targetUserId,
        currentUser.getCompany().getId());
    if (targetUser == null) {
      throw new ForbiddenException("Cannot find user!");
    }

    final Role userRole = auth0Util
        .getUserRole(currentUser.getUserContactInformation().getEmailWork());
    final Boolean isAdmin = Role.ADMIN == userRole;
    final User manager = targetUser.getManagerUser();

    final Boolean isManager = Role.MANAGER == userRole
        && manager != null && manager.getId().equals(currentUser.getId());

    if (isAdmin || isManager) {
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
  public void updatePassword(final ChangePasswordPojo changePasswordPojo, final String email) {
    final com.auth0.json.mgmt.users.User user = auth0Util
        .getUserByEmailFromAuth0(email);

    auth0Util.login(email, changePasswordPojo.getPassWord());
    auth0Util.updatePassword(user, changePasswordPojo.getNewPassword());

    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    final String emailContent = templateEngine.process("password_change_email.html", context);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final Email notificationEmail = new Email(systemEmailAddress,
        email, "Password Changed!",
        emailContent, sendDate);
    emailService.saveAndScheduleEmail(notificationEmail);

  }

  @Override
  public CurrentUserDto getCurrentUserInfo(final String userId) {
    final User user = findByUserId(userId);

    if (user.getVerifiedAt() == null) {
      return CurrentUserDto.builder()
          .id(user.getId())
          .verified(false)
          .build();
    }

    final List<User> teamMembers = findByManagerUser(user);
    final List<Long> teamMemberIds = teamMembers.stream().map(BaseEntity::getId)
        .collect(Collectors.toList());

    return CurrentUserDto.builder()
        .id(user.getId())
        .teamMembers(teamMemberIds)
        .name(user.getUserPersonalInformation().getName())
        .imageUrl(user.getImageUrl())
        .verified(user.getVerifiedAt() != null)
        .build();
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
      throw new ForbiddenException("New password cannot be the same as the old one.");
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
