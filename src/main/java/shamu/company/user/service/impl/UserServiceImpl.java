package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserService;

@Service
public class UserServiceImpl implements UserService {

  private static final String ERROR_MESSAGE = "User does not exist!";
  private static final String passwordReg = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$";
  private final ITemplateEngine templateEngine;
  private final UserRepository userRepository;
  private final JobUserRepository jobUserRepository;
  private final UserStatusRepository userStatusRepository;

  private final EmailService emailService;
  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;
  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  private final UserCompensationRepository userCompensationRepository;

  @Autowired
  public UserServiceImpl(ITemplateEngine templateEngine, UserRepository userRepository,
      JobUserRepository jobUserRepository, UserStatusRepository userStatusRepository,
      EmailService emailService, UserCompensationRepository userCompensationRepository) {
    this.templateEngine = templateEngine;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.userStatusRepository = userStatusRepository;
    this.emailService = emailService;
    this.userCompensationRepository = userCompensationRepository;
  }

  @Override
  public User findUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmailWork(email);
  }

  @Override
  public User findUserByEmailAndStatus(String email, Status userStatus) {
    return userRepository.findByEmailWorkAndStatus(email, userStatus.name());
  }

  @Override
  public User findUserByUserPersonalInformationId(Long userPersonalInformationId) {
    return userRepository.findByUserPersonalInformationId(userPersonalInformationId);
  }

  @Override
  public User findUserByUserContactInformationId(Long userContactInformationId) {
    return userRepository.findByUserContactInformationId(userContactInformationId);
  }

  @Override
  public void sendVerifyEmail(String email) {
    User user = userRepository.findByEmailWork(email);
    if (user == null) {
      throw new ForbiddenException("User account does not exist!");
    }

    String accountVerifyToken = UUID.randomUUID().toString();
    String emailContent = getActivationEmail(accountVerifyToken);
    Timestamp sendDate = new Timestamp(new Date().getTime());

    Email verifyEmail = new Email(systemEmailAddress, email, "Please activate your account!",
        emailContent, sendDate);
    emailService.saveAndScheduleEmail(verifyEmail);

    user.setVerificationToken(accountVerifyToken);
    String employeeNumber = getEmployeeNumber(user.getCompany().getName(), 1);
    user.setEmployeeNumber(employeeNumber);
    userRepository.save(user);
  }

  @Override
  public void finishUserVerification(String activationToken) {
    User user = userRepository.findByVerificationToken(activationToken);
    if (user == null || user.getVerifiedAt() != null) {
      throw new ForbiddenException("User account does not exist or already activated!");
    }

    UserStatus userStatus = userStatusRepository.findByName(Status.ACTIVE.name());
    user.setUserStatus(userStatus);
    user.setVerifiedAt(new Timestamp(new Date().getTime()));
    userRepository.save(user);
  }

  @Override
  public Boolean existsByEmailWork(String email) {
    return userRepository.existsByEmailWork(email);
  }

  @Override
  public List<JobUserDto> findDirectReportsByManagerId(Long id) {
    List<User> directReports = userRepository.findAllByManagerUserId(id);

    return directReports.stream()
        .map(
            (user) -> {
              JobUser reporterWithJob = jobUserRepository.findJobUserByUser(user);
              return new JobUserDto(user, reporterWithJob);
            })
        .collect(Collectors.toList());
  }

  @Override
  public String getWelcomeEmail(Context context) {
    return templateEngine.process("employee_invitation_email.html", context);
  }

  @Override
  public Context getWelcomeEmailContext(String welcomeMessage, String resetPasswordToken) {
    Context context = new Context();
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
    Pattern scriptPattern = Pattern.compile("<script(.*)?>.*</script>");
    Matcher scriptMatcher = scriptPattern.matcher(welcomeMessage);
    return scriptMatcher.replaceAll("");
  }

  @Override
  public JobUserDto findEmployeeInfoByEmployeeId(Long id) {

    User employee =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
    JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser);
  }

  @Override
  public User findEmployeeInfoByUserId(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }

  public Page<JobUserListItem> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition, Company company) {
    String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);

    return getAllEmployeesByCompany(employeeListSearchCondition, company, paramPageable);
  }

  @Override
  public Page<JobUserListItem> getAllEmployeesByCompany(
      EmployeeListSearchCondition employeeListSearchCondition, Company company, Pageable pageable) {
    Long companyId = company.getId();
    return userRepository.getAllByCondition(employeeListSearchCondition, companyId, pageable);
  }

  @Override
  public User getOne(Long userId) {
    return userRepository.getOne(userId);
  }

  @Override
  public void save(User user) {
    userRepository.save(user);
  }

  @Override
  public List<JobUserDto> findAllEmployees(Company company) {
    List<User> policyEmployees = userRepository.findAllByCompany(company);

    return policyEmployees.stream()
        .map(
            (user) -> {
              JobUser employeeWithJob = jobUserRepository.findJobUserByUser(user);
              return new JobUserDto(user, employeeWithJob);
            })
        .collect(Collectors.toList());
  }

  @Override
  public String getHeadPortrait(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
    return user.getImageUrl();
  }

  @Override
  public UserCompensation saveUserCompensation(UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
  }

  @Override
  public Boolean createPasswordTokenExist(String token) {
    return userRepository.existsByResetPasswordToken(token);
  }

  @Override
  public void createPassword(UpdatePasswordDto updatePasswordDto) {
    if (!Pattern.matches(passwordReg, updatePasswordDto.getNewPassword())) {
      throw new ForbiddenException("Your password doesn't meet our requirements.");
    }

    User user = userRepository.findByEmailWork(updatePasswordDto.getEmailWork());
    if (user == null
        || !updatePasswordDto.getResetPasswordToken().equals(user.getResetPasswordToken())) {
      throw new ForbiddenException("Create password Forbidden");
    }

    user.setResetPasswordToken(null);
    String pwHash = BCrypt.hashpw(updatePasswordDto.getNewPassword(), BCrypt.gensalt(10));
    user.setPassword(pwHash);
    UserStatus userStatus = userStatusRepository.findByName(Status.ACTIVE.name());
    user.setUserStatus(userStatus);
    userRepository.save(user);
  }

  @Override
  public Page<JobUserListItem> getMyTeam(EmployeeListSearchCondition employeeListSearchCondition,
      User user) {
    String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    String[] sortValue = employeeListSearchCondition.getSortField().getSortValue();
    Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);
    return userRepository
        .getMyTeamByManager(employeeListSearchCondition, user, paramPageable);
  }

  @Override
  public void unlock(UserLoginDto userLoginDto) {
    User user = userRepository.findByEmailWork(userLoginDto.getEmailWork());
    if (user == null || !BCrypt.checkpw(userLoginDto.getPassword(), user.getPassword())) {
      throw new ForbiddenException("Login Forbidden!");
    }
  }

  public String getActivationEmail(String accountVerifyToken) {
    Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
    return templateEngine.process("account_verify_email.html", context);
  }

  public String getEmployeeNumber(String companyName, Integer employeeNumber) {
    if (companyName.length() <= 3) {
      return String.format("%s%06d", companyName, employeeNumber);
    }

    String employeeNumberPrefix = companyName.substring(0, 3);
    return String.format("%s%06d", employeeNumberPrefix, employeeNumber);
  }

  @Override
  public void sendResetPasswordEmail(String email) {
    User user = userRepository.findByEmailWork(email);
    if (user == null) {
      throw new ForbiddenException("User account does not exist!");
    }
    String passwordRestToken = UUID.randomUUID().toString();
    String emailContent = getResetPasswordEmail(passwordRestToken);
    Timestamp sendDate = new Timestamp(new Date().getTime());
    Email verifyEmail = new Email(systemEmailAddress, email,  "Password Reset!",
        emailContent, sendDate);
    emailService.saveAndScheduleEmail(verifyEmail);
    user.setResetPasswordToken(passwordRestToken);
    userRepository.save(user);
  }

  @Override
  public void resetPassword(UpdatePasswordDto updatePasswordDto) {
    User user = userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken());
    if (user == null) {
      throw new ForbiddenException("Create password Forbidden");
    }
    user.setResetPasswordToken(null);
    String pwHash = BCrypt.hashpw(updatePasswordDto.getNewPassword(), BCrypt.gensalt(10));
    user.setPassword(pwHash);
    userRepository.save(user);
  }

  public String getResetPasswordEmail(String passwordRestToken) {
    Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "passwordResetAddress", String.format("account/reset-password/%s", passwordRestToken));
    return templateEngine.process("password_reset_email.html", context);
  }
}
