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
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.repository.CompensationTypeRepository;
import shamu.company.employee.service.EmployeeService;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.CompensationType;
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
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;

@Service
public class EmployeeServiceImpl implements EmployeeService {

  private final UserRepository userRepository;

  private final JobUserRepository jobUserRepository;

  private final UserAddressRepository userAddressRepository;

  private final DepartmentRepository departmentRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final OfficeAddressRepository officeAddressRepository;

  private final UserCompensationRepository userCompensationRepository;

  private final JobRepository jobRepository;

  private final GenderRepository genderRepository;

  private final MaritalStatusRepository maritalStatusRepository;

  private final AwsUtil awsUtil;

  private final CompensationTypeRepository compensationTypeRepository;

  private final StateProvinceRepository stateProvinceRepository;

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  private final UserService userService;

  private final UserRoleRepository userRoleRepository;

  private final UserStatusRepository userStatusRepository;

  private final EmailService emailService;

  @Autowired
  public EmployeeServiceImpl(UserAddressRepository userAddressRepository,
      UserRepository userRepository, JobUserRepository jobUserRepository,
      DepartmentRepository departmentRepository, EmploymentTypeRepository employmentTypeRepository,
      OfficeRepository officeRepository, UserService userService,
      OfficeAddressRepository officeAddressRepository,
      StateProvinceRepository stateProvinceRepository,
      UserCompensationRepository userCompensationRepository,
      UserEmergencyContactRepository userEmergencyContactRepository, JobRepository jobRepository,
      UserRoleRepository userRoleRepository, UserStatusRepository userStatusRepository,
      AwsUtil awsUtil, GenderRepository genderRepository,
      MaritalStatusRepository maritalStatusRepository, EmailService emailService,
      CompensationTypeRepository compensationTypeRepository) {
    this.userAddressRepository = userAddressRepository;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.departmentRepository = departmentRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.userService = userService;
    this.officeAddressRepository = officeAddressRepository;
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
    this.compensationTypeRepository = compensationTypeRepository;
  }

  @Override
  public List<User>
      findEmployersAndEmployeesByDepartmentIdAndCompanyId(Long departmentId, Long companyId) {
    return userRepository
        .findEmployersAndEmployeesByDepartmentIdAndCompanyId(departmentId, companyId);
  }

  @Override
  public void addEmployee(EmployeeDto employeeDto, User currentUser) {
    User employee = saveEmployeeBasicInformation(currentUser, employeeDto);

    saveEmergencyContacts(employee, employeeDto.getEmergencyContactList());

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
        employeeDto.getUserPersonalInformation();
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

    UserContactInformationDto userContactInformationDto = employeeDto.getUserContactInformation();
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

  private void saveManagerUser(User user, NewEmployeeJobInformationDto jobInformation) {
    Long managerUserId = jobInformation.getReportsTo();
    if (managerUserId != null) {
      User managerUser = userRepository.findById(managerUserId).orElseThrow(
          () -> new ResourceNotFoundException("User with id " + managerUserId + " not found!"));

      if (Role.NON_MANAGER.name().equals(user.getUserRole().getName())) {
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

    Long compensationTypeId = jobInformation.getCompensationTypeId();
    if (compensationTypeId != null) {
      CompensationType compensationType = compensationTypeRepository.getOne(compensationTypeId);
      userCompensation.setCompensationType(compensationType);
    }
    userCompensation.setUser(user);
    userCompensationRepository.save(userCompensation);
  }

  private void saveEmployeeJob(
      User employee, User currentUser, NewEmployeeJobInformationDto jobInformation) {
    Job job = new Job();

    Department department = null;
    Long departmentId = jobInformation.getDepartmentId();
    if (departmentId != null) {
      department = departmentRepository.getOne(departmentId);
      job.setDepartment(department);
    }

    job.setTitle(jobInformation.getJobTitle());
    job = jobRepository.save(job);

    JobUser jobUser = new JobUser();
    jobUser.setJob(job);
    jobUser.setCompany(currentUser.getCompany());
    jobUser.setDepartment(department);
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

    Long officeAddressId = jobInformation.getOfficeAddressId();
    if (officeAddressId != null) {
      OfficeAddress officeAddress = officeAddressRepository.getOne(officeAddressId);
      // TODO the column office_id in table office_addresses has been removed.
      //  jobUser.setOffice(officeAddress.getOffice());
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
