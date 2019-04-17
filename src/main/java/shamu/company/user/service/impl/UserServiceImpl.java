package shamu.company.user.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResouceNotFoundException;
import shamu.company.job.JobUser;
import shamu.company.job.JobUserDto;
import shamu.company.job.JobUserRepository;
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

  @Autowired
  ITemplateEngine templateEngine;

  @Autowired
  UserRepository userRepository;

  @Autowired
  JobUserRepository jobUserRepository;

  @Autowired
  UserAddressRepository userAddressRepository;

  @Value("${application.systemEmailAddress}")
  String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  String frontEndAddress;

  @Autowired
  EmailUtil emailUtil;

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmailWork(email);
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
  public List<JobUserDto> findAllEmployees() {
    List<User> employees = userRepository.findAllEmployees();
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
            .orElseThrow(() -> new ResouceNotFoundException("User does not exist"));
    UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    UserContactInformation userContactInformation = user.getUserContactInformation();
    UserAddress userAddress = userAddressRepository.findUserAddressByUserId(userId);
    PersonalInformationDto personalInformationDto =
        new PersonalInformationDto(
            userId, userPersonalInformation, userContactInformation, userAddress);
    return personalInformationDto;
  }

  @Override
  public List<JobUserDto> findDirectReportsByManagerId(Long mid) {
    List<User> directReports = userRepository.findAllByManagerUserId(mid);

    List<JobUserDto> reportsInfo = directReports.stream()
        .map(
            (user)->{
              JobUser reporterWithJob = jobUserRepository.findJobUserByUser(user);
              JobUserDto jobUserDto = new JobUserDto(user,reporterWithJob);
              return  jobUserDto;
            }).collect(Collectors.toList());

    return reportsInfo;
  }


  @Override
  public JobUserDto findEmployeeInfoByEmployeeId(Long id) {

    User employee = userRepository.findById(id)
        .orElseThrow(() -> new ResouceNotFoundException("User does not exist"));

    JobUser jobUser = jobUserRepository.findJobUserByUser(employee);
    JobUserDto jobUserDto = new JobUserDto(employee,jobUser);

    return jobUserDto;
  }

  @Override
  public User findEmployeeInfoByUserId(Long uid) {
    return userRepository.findById(uid)
        .orElseThrow(() -> new ResouceNotFoundException("User does not exist"));
  }

  private List<JobUserDto> getJobUserDtoList(
      List<User> employees, List<UserAddress> userAddresses, List<JobUser> jobUsers) {
    return employees.stream()
        .map(
            (employee) -> {
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
