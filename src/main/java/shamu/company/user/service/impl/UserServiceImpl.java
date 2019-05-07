package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.EmailUtil;

@Service
public class UserServiceImpl implements UserService {

  private final ITemplateEngine templateEngine;

  private final UserRepository userRepository;

  private final JobUserRepository jobUserRepository;

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  private final EmailUtil emailUtil;

  private final UserStatusRepository userStatusRepository;

  @Autowired
  public UserServiceImpl(ITemplateEngine templateEngine, UserRepository userRepository,
      JobUserRepository jobUserRepository, EmailUtil emailUtil,
      UserStatusRepository userStatusRepository) {
    this.templateEngine = templateEngine;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.emailUtil = emailUtil;
    this.userStatusRepository = userStatusRepository;
  }

  private static final String errorMessage = "User does not exist!";

  @Override
  public User findUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
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
    emailUtil.send(systemEmailAddress, email, "Please activate your account!", emailContent);

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
  public Context getWelcomeEmailContext(String welcomeMessage) {
    Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable("createPasswordAddress", frontEndAddress + "account/create-password");
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
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser);
  }

  @Override
  public User findEmployeeInfoByUserId(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
  }

  public PageImpl<JobUserDto> getJobUserDtoList(
      EmployeeListSearchCondition employeeListSearchCondition, Company company) {
    String sortDirection = employeeListSearchCondition.getSortDirection().toUpperCase();

    String sortValue = employeeListSearchCondition.getSortField().getSortValue();
    Pageable paramPageable =
        PageRequest.of(
            employeeListSearchCondition.getPage(),
            employeeListSearchCondition.getSize(),
            Sort.Direction.valueOf(sortDirection),
            sortValue);

    Page<JobUserListItem> jobUserPageItem =
        findAllEmployees(employeeListSearchCondition, company, paramPageable);
    List<JobUserListItem> jobUsers = jobUserPageItem.getContent();
    List<JobUserDto> jobUserDtos =
        jobUsers.stream()
            .map(
                (jobUser -> {
                  JobUserDto jobUserDto = new JobUserDto();
                  BeanUtils.copyProperties(jobUser, jobUserDto);
                  return jobUserDto;
                }))
            .collect(Collectors.toList());
    return new PageImpl<JobUserDto>(
        jobUserDtos, jobUserPageItem.getPageable(), jobUserPageItem.getTotalElements());
  }

  @Override
  public Page<JobUserListItem> findAllEmployees(
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
  public String getHeadPortrait(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    return user.getImageUrl();
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
}
