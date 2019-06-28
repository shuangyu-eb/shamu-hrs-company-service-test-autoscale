package shamu.company.employee.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.AwsUploadException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Office;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserRole.Role;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;

@Service
public class EmployeeServiceImpl implements EmployeeService {

  private final UserRepository userRepository;

  private final JobUserRepository jobUserRepository;

  private final UserAddressRepository userAddressRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final UserCompensationRepository userCompensationRepository;

  private final JobRepository jobRepository;

  private final GenderRepository genderRepository;

  private final MaritalStatusRepository maritalStatusRepository;

  private final AwsUtil awsUtil;

  private final CompensationFrequencyRepository compensationFrequencyRepository;

  private final StateProvinceRepository stateProvinceRepository;

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  private final UserService userService;

  private final UserRoleRepository userRoleRepository;

  private final UserStatusRepository userStatusRepository;

  private final EmailService emailService;

  private final OfficeRepository officeRepository;

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserContactInformationService userContactInformationService;

  @Autowired
  public EmployeeServiceImpl(UserAddressRepository userAddressRepository,
      UserRepository userRepository, JobUserRepository jobUserRepository,
      EmploymentTypeRepository employmentTypeRepository,
      OfficeRepository officeRepository, UserService userService,
      StateProvinceRepository stateProvinceRepository,
      UserCompensationRepository userCompensationRepository,
      UserEmergencyContactRepository userEmergencyContactRepository, JobRepository jobRepository,
      UserRoleRepository userRoleRepository, UserStatusRepository userStatusRepository,
      AwsUtil awsUtil, GenderRepository genderRepository,
      MaritalStatusRepository maritalStatusRepository, EmailService emailService,
      CompensationFrequencyRepository compensationFrequencyRepository,
      UserPersonalInformationService userPersonalInformationService,
      UserContactInformationService userContactInformationService) {
    this.userAddressRepository = userAddressRepository;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.userService = userService;
    this.stateProvinceRepository = stateProvinceRepository;
    this.userCompensationRepository = userCompensationRepository;
    this.userEmergencyContactRepository = userEmergencyContactRepository;
    this.jobRepository = jobRepository;
    this.userRoleRepository = userRoleRepository;
    this.userStatusRepository = userStatusRepository;
    this.awsUtil = awsUtil;
    this.genderRepository = genderRepository;
    this.maritalStatusRepository = maritalStatusRepository;
    this.emailService = emailService;
    this.compensationFrequencyRepository = compensationFrequencyRepository;
    this.officeRepository = officeRepository;
    this.userPersonalInformationService = userPersonalInformationService;
    this.userContactInformationService = userContactInformationService;
  }

  @Override
  public List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(Long departmentId,
      Long companyId) {
    return userRepository
        .findEmployersAndEmployeesByDepartmentIdAndCompanyId(departmentId, companyId);
  }

  @Override
  public void addEmployee(EmployeeDto employeeDto, User currentUser) {
    User employee = saveEmployeeBasicInformation(currentUser, employeeDto);

    saveEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());

    NewEmployeeJobInformationDto jobInformation = employeeDto.getJobInformation();

    if (jobInformation != null) {
      saveManagerUser(employee, jobInformation);
      saveEmployeeCompensation(employee, jobInformation);
      saveEmployeeJob(employee, currentUser, jobInformation);
    }

    saveEmployeeAddress(employee, employeeDto);

    WelcomeEmailDto welcomeEmailDto = employeeDto.getWelcomeEmail();

    saveEmailTasks(welcomeEmailDto, employee, currentUser);
  }

  @Override
  public void updateEmployee(EmployeeDto employeeDto) {
    User employee = userRepository.findByEmailWork(employeeDto.getEmailWork());
    updateEmployeeBasicInformation(employee, employeeDto);
    updateEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());
    updateEmployeeAddress(employee, employeeDto);
  }

  private String saveEmployeePhoto(String base64EncodedPhoto) {
    if (Strings.isBlank(base64EncodedPhoto)) {
      return null;
    }

    File file = null;
    try {
      String imageString = base64EncodedPhoto.split(",")[1];
      file = File.createTempFile(UUID.randomUUID().toString(), ".png");
      byte[] photo = Base64.getDecoder().decode(imageString);
      FileCopyUtils.copy(photo, file);
      return awsUtil.uploadFile(file.getCanonicalPath(), Type.IMAGE);
    } catch (IOException e) {
      throw new AwsUploadException("Error while upload employee photo!", e);
    }
  }

  private User saveEmployeeBasicInformation(User currentUser, EmployeeDto employeeDto) {
    User employee = new User();
    employee.setEmailWork(employeeDto.getEmailWork());

    String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    String photoPath = saveEmployeePhoto(base64EncodedPhoto);
    employee.setImageUrl(photoPath);

    String companyName = currentUser.getCompany().getName();
    Integer existingUserCount =
        userRepository.findExistingUserCountByCompanyId(currentUser.getCompany().getId());
    Integer employeeIndex = existingUserCount + 1;
    String employeeNumber = userService.getEmployeeNumber(companyName, employeeIndex);
    employee.setEmployeeNumber(employeeNumber);

    employee.setCompany(currentUser.getCompany());

    UserPersonalInformationDto userPersonalInformationDto =
        employeeDto.getUserPersonalInformationDto();
    UserPersonalInformation userPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation(new UserPersonalInformation());

    if (userPersonalInformation != null) {
      Gender gender = userPersonalInformation.getGender();
      if (gender != null) {
        gender = genderRepository.getOne(gender.getId());
        userPersonalInformation.setGender(gender);
      } else {
        userPersonalInformation.setGender(null);
      }

      MaritalStatus martialStatus = userPersonalInformation.getMaritalStatus();
      if (martialStatus != null) {
        martialStatus = maritalStatusRepository.getOne(martialStatus.getId());
        userPersonalInformation.setMaritalStatus(martialStatus);
      } else {
        userPersonalInformation.setMaritalStatus(null);
      }
      employee.setUserPersonalInformation(userPersonalInformation);
    }

    UserContactInformationDto userContactInformationDto = employeeDto
        .getUserContactInformationDto();
    if (userContactInformationDto != null) {
      employee.setUserContactInformation(
          userContactInformationDto.getUserContactInformation(new UserContactInformation()));
    }

    UserRole userRole = userRoleRepository.findByName(User.Role.NON_MANAGER.name());
    employee.setUserRole(userRole);

    UserStatus userStatus = userStatusRepository.findByName(Status.PENDING_VERIFICATION.name());
    employee.setUserStatus(userStatus);

    employee.setResetPasswordToken(UUID.randomUUID().toString());

    return userRepository.save(employee);
  }

  private User updateEmployeeBasicInformation(User employee, EmployeeDto employeeDto) {
    String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    String photoPath = saveEmployeePhoto(base64EncodedPhoto);
    employee.setImageUrl(photoPath);

    UserPersonalInformation userPersonalInformation = employee.getUserPersonalInformation();
    UserPersonalInformationDto userPersonalInformationDto = employeeDto
        .getUserPersonalInformationDto();
    UserPersonalInformation newUserPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation(userPersonalInformation);
    if (userPersonalInformation != null) {
      newUserPersonalInformation.setId(userPersonalInformation.getId());
    }
    UserPersonalInformation savedUserPersonalInformation = userPersonalInformationService
        .update(newUserPersonalInformation);
    employee.setUserPersonalInformation(savedUserPersonalInformation);

    UserContactInformation userContactInformation = employee.getUserContactInformation();
    UserContactInformationDto userContactInformationDto = employeeDto
        .getUserContactInformationDto();
    UserContactInformation newUserContactInformation =
        userContactInformationDto.getUserContactInformation(userContactInformation);
    if (userContactInformation != null) {
      newUserContactInformation.setId(userContactInformation.getId());
    }
    UserContactInformation savedUserContactInformation = userContactInformationService
        .update(newUserContactInformation);
    employee.setUserContactInformation(savedUserContactInformation);

    return userRepository.save(employee);
  }

  private void saveEmergencyContacts(
      User employee, List<UserEmergencyContactDto> emergencyContactDtos) {
    if (CollectionUtils.isEmpty(emergencyContactDtos)) {
      return;
    }

    emergencyContactDtos.forEach(
        emergencyContactDto -> {
          UserEmergencyContact emergencyContact = emergencyContactDto.getEmergencyContact();

          Long stateProvinceId = emergencyContact.getState().getId();
          if (stateProvinceId != null) {
            StateProvince stateProvince = stateProvinceRepository.getOne(stateProvinceId);
            emergencyContact.setState(stateProvince);
          } else {
            emergencyContact.setState(null);
          }

          emergencyContact.setUser(employee);
          userEmergencyContactRepository.save(emergencyContact);
        });
  }

  private void updateEmergencyContacts(
      User employee, List<UserEmergencyContactDto> emergencyContactDtos) {
    List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactRepository
        .findByUserId(employee.getId());
    userEmergencyContactRepository.deleteInBatch(userEmergencyContacts);
    saveEmergencyContacts(employee, emergencyContactDtos);
  }

  private void saveManagerUser(User user, NewEmployeeJobInformationDto jobInformation) {
    Long managerUserId = jobInformation.getReportsTo();
    if (managerUserId != null) {
      User managerUser = userRepository.findById(managerUserId).orElseThrow(
          () -> new ResourceNotFoundException("User with id " + managerUserId + " not found!"));

      if (Role.NON_MANAGER.name().equals(managerUser.getUserRole().getName())) {
        UserRole userRole = userRoleRepository.findByName(Role.MANAGER.name());
        managerUser.setUserRole(userRole);
        userRepository.save(managerUser);
      }

      user.setManagerUser(managerUser);
      userRepository.save(user);
    }
  }

  private void saveEmployeeCompensation(User user, NewEmployeeJobInformationDto jobInformation) {
    UserCompensation userCompensation = new UserCompensation();
    userCompensation.setWage(jobInformation.getCompensation());

    Long compensationFrequencyId = jobInformation.getCompensationFrequencyId();
    if (compensationFrequencyId != null) {
      CompensationFrequency compensationFrequency = compensationFrequencyRepository
          .getOne(compensationFrequencyId);
      userCompensation.setCompensationFrequency(compensationFrequency);
    }
    userCompensation.setUser(user);
    userCompensationRepository.save(userCompensation);
  }

  private void saveEmployeeJob(
      User employee, User currentUser, NewEmployeeJobInformationDto jobInformation) {

    Job job = jobRepository.getOne(jobInformation.getJobId());

    JobUser jobUser = new JobUser();
    jobUser.setJob(job);
    jobUser.setCompany(currentUser.getCompany());
    jobUser.setUser(employee);

    Long employmentTypeId = jobInformation.getEmploymentTypeId();
    if (employmentTypeId != null) {
      EmploymentType employmentType = employmentTypeRepository.getOne(employmentTypeId);
      jobUser.setEmploymentType(employmentType);
    } else {
      jobUser.setEmploymentType(null);
    }

    Timestamp hireDate = jobInformation.getHireDate();
    if (hireDate != null) {
      jobUser.setStartDate(new Timestamp(hireDate.getTime()));
    }

    Long officeId = jobInformation.getOfficeId();
    if (officeId != null) {
      Office office = officeRepository.getOne(officeId);
      jobUser.setOffice(office);
    } else {
      jobUser.setOffice(null);
    }

    jobUserRepository.save(jobUser);
  }

  private void saveEmployeeAddress(User employee, EmployeeDto employeeDto) {
    UserAddressDto userAddressDto = employeeDto.getUserAddress();
    UserAddress userAddress = userAddressDto.getUserAddress(new UserAddress());

    StateProvince stateProvince = userAddress.getStateProvince();
    if (stateProvince != null) {
      stateProvince = stateProvinceRepository.getOne(stateProvince.getId());
      userAddress.setStateProvince(stateProvince);
    } else {
      userAddress.setStateProvince(null);
    }

    userAddress.setUser(employee);
    userAddressRepository.save(userAddress);
  }

  private void updateEmployeeAddress(User employee, EmployeeDto employeeDto) {
    UserAddressDto userAddressDto = employeeDto.getUserAddress();
    UserAddress userAddress = new UserAddress();
    userAddress.setId(userAddressDto.getId());
    userAddress.setUser(employee);
    userAddress.setStreet1(userAddressDto.getStreet1());
    userAddress.setStreet2(userAddressDto.getStreet2());
    userAddress.setCity(userAddressDto.getCity());
    Long stateProvinceId = employeeDto.getUserAddress().getStateProvinceId();
    if (stateProvinceId != null) {
      StateProvince stateProvince = stateProvinceRepository.getOne(stateProvinceId);
      userAddress.setStateProvince(stateProvince);
    } else {
      userAddress.setStateProvince(null);
    }
    userAddress.setPostalCode(userAddressDto.getPostalCode());
    userAddressRepository.save(userAddress);
  }

  private void saveEmailTasks(WelcomeEmailDto welcomeEmailDto, User employee, User currentUser) {
    String from = currentUser.getEmailWork();
    String to = welcomeEmailDto.getSendTo();
    String content = welcomeEmailDto.getPersonalInformation();

    Context emailContext = userService
        .getWelcomeEmailContext(content, employee.getResetPasswordToken());
    content = userService.getWelcomeEmail(emailContext);
    Timestamp sendDate = welcomeEmailDto.getSendDate();

    Email email =
        new Email(from, to, "Welcome to Champion Solutions", content, currentUser, sendDate);
    emailService.saveAndScheduleEmail(email);
  }
}
