package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.dto.PersonalInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.EmailUtil;

@Service
public class UserServiceImpl implements UserService {

  @Autowired ITemplateEngine templateEngine;

  @Autowired UserRepository userRepository;

  @Autowired JobUserRepository jobUserRepository;

  @Autowired UserAddressRepository userAddressRepository;

  @Value("${application.systemEmailAddress}")
  String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  String frontEndAddress;

  @Autowired EmailUtil emailUtil;

  private static String errorMessage = "User does not exist!";

  @Override
  public User findUserById(Long id) {
    return userRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException(errorMessage));
  }

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmailWork(email);
  }


  @Override
  public User findUserByUserPersonalInformationId(Long userPersonalInformationId) {
    return userRepository.findByUserPersonalInformationId(userPersonalInformationId);
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
    userRepository.save(user);
  }

  @Override
  public void finishUserVerification(String activationToken) {
    User user = userRepository.findByVerificationToken(activationToken);
    if (user == null || user.getVerifiedAt() != null) {
      throw new ForbiddenException("User account does not exist or already activated!");
    }
    user.setVerifiedAt(new Timestamp(new Date().getTime()));
    userRepository.save(user);
  }

  @Override
  public List<JobUserDto> findEmployeesByCompany(Company company) {
    List<User> employees = userRepository.findByCompany(company);
    List<UserAddress> userAddresses = userAddressRepository.findAllByUserIn(employees);
    List<JobUser> jobUserList = jobUserRepository.findAllByUserIn(employees);

    return getJobUserDtoList(employees, userAddresses, jobUserList);
  }

  @Override
  public Boolean existsByEmailWork(String email) {
    return userRepository.existsByEmailWork(email);
  }

  @Override
  public PersonalInformationDto getPersonalInformation(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    UserContactInformation userContactInformation = user.getUserContactInformation();
    UserAddress userAddress =
        userAddressRepository
            .findUserAddressByUserId(userId)
            .orElse(new UserAddress());
    return new PersonalInformationDto(
        userId, userPersonalInformation, userContactInformation, userAddress);
  }

  @Override
  public List<JobUserDto> findDirectReportsByManagerId(Long id) {
    List<User> directReports = userRepository.findAllByManagerUserId(id);

    return directReports.stream().map((user) -> {
      JobUser reporterWithJob = jobUserRepository.findJobUserByUser(user);
      return new JobUserDto(user, reporterWithJob);
    }).collect(Collectors.toList());
  }

  @Override
  public JobUserDto findEmployeeInfoByEmployeeId(Long id) {

    User employee = userRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException(errorMessage));
    JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    return new JobUserDto(employee, jobUser);
  }

  @Override
  public User findEmployeeInfoByUserId(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
  }

  private List<JobUserDto> getJobUserDtoList(
      List<User> employees, List<UserAddress> userAddresses, List<JobUser> jobUsers) {
    return employees.stream()
        .map(
            employee -> {
              JobUserDto jobUserDto = new JobUserDto();
              jobUserDto.setEmail(employee.getEmailWork());
              jobUserDto.setImageUrl(employee.getImageUrl());
              jobUserDto.setId(employee.getId());

              UserPersonalInformation userPersonalInformation =
                  employee.getUserPersonalInformation();
              if (userPersonalInformation != null) {
                jobUserDto.setFirstName(userPersonalInformation.getFirstName());
                jobUserDto.setLastName(userPersonalInformation.getLastName());
              }

              userAddresses.forEach(
                  (userAddress -> {
                    User userWithAddress = userAddress.getUser();
                    if (userWithAddress != null
                        && userWithAddress.getId().equals(employee.getId())
                        && userAddress.getCity() != null) {
                      jobUserDto.setCityName(userAddress.getCity());
                    }
                  }));

              jobUsers.forEach(
                  (jobUser -> {
                    User userWithJob = jobUser.getUser();
                    if (userWithJob != null
                        && userWithJob.getId().equals(employee.getId())
                        && jobUser.getJob() != null) {
                      jobUserDto.setJobTitle(jobUser.getJob().getTitle());
                    }
                  }));
              return jobUserDto;
            })
        .collect(Collectors.toList());
  }

  public String getActivationEmail(String accountVerifyToken) {
    Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
    return templateEngine.process("account_verify_email.html", context);
  }
}