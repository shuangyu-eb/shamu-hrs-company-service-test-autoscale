package shamu.company.employee.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.AwsUploadException;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Office;
import shamu.company.email.Email;
import shamu.company.email.EmailRepository;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.event.Auth0UserCreatedEvent;
import shamu.company.employee.service.EmployeeService;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
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
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;
import shamu.company.utils.FileValidateUtil;
import shamu.company.utils.FileValidateUtil.FileType;

@Service
@Transactional
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

  private final CountryRepository countryRepository;

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  private final UserService userService;

  private final UserRoleRepository userRoleRepository;

  private final UserStatusRepository userStatusRepository;

  private final EmailRepository emailRepository;

  private final EmailService emailService;

  private final OfficeRepository officeRepository;

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserContactInformationService userContactInformationService;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  private final UserAddressMapper userAddressMapper;

  private final UserContactInformationMapper userContactInformationMapper;

  private final UserEmergencyContactMapper userEmergencyContactMapper;

  private final Auth0Util auth0Util;

  private final ApplicationEventPublisher applicationEventPublisher;

  private static final String subject = "Welcome to Champion Solutions";


  @Autowired
  public EmployeeServiceImpl(
      final UserAddressRepository userAddressRepository,
      final UserRepository userRepository,
      final JobUserRepository jobUserRepository,
      final EmploymentTypeRepository employmentTypeRepository,
      final OfficeRepository officeRepository,
      final UserService userService,
      final StateProvinceRepository stateProvinceRepository,
      final CountryRepository countryRepository,
      final UserCompensationRepository userCompensationRepository,
      final UserEmergencyContactRepository userEmergencyContactRepository,
      final JobRepository jobRepository,
      final UserRoleRepository userRoleRepository,
      final UserStatusRepository userStatusRepository,
      final AwsUtil awsUtil,
      final GenderRepository genderRepository,
      final MaritalStatusRepository maritalStatusRepository,
      final EmailService emailService,
      final CompensationFrequencyRepository compensationFrequencyRepository,
      final EmailRepository emailRepository,
      final UserPersonalInformationService userPersonalInformationService,
      final UserContactInformationService userContactInformationService,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserAddressMapper userAddressMapper,
      final UserContactInformationMapper userContactInformationMapper,
      final UserEmergencyContactMapper userEmergencyContactMapper,
      final Auth0Util auth0Util,
      final ApplicationEventPublisher applicationEventPublisher) {
    this.userAddressRepository = userAddressRepository;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.userService = userService;
    this.stateProvinceRepository = stateProvinceRepository;
    this.countryRepository = countryRepository;
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
    this.emailRepository = emailRepository;
    this.userPersonalInformationService = userPersonalInformationService;
    this.userContactInformationService = userContactInformationService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userAddressMapper = userAddressMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userEmergencyContactMapper = userEmergencyContactMapper;
    this.auth0Util = auth0Util;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(
      final Long departmentId, final Long companyId) {
    return userRepository.findEmployersAndEmployeesByDepartmentIdAndCompanyId(
        departmentId, companyId);
  }

  @Override
  public void addEmployee(final EmployeeDto employeeDto, final User currentUser) {
    final User employee = saveEmployeeBasicInformation(currentUser, employeeDto);

    saveEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());

    final NewEmployeeJobInformationDto jobInformation = employeeDto.getJobInformation();

    if (jobInformation != null) {
      saveManagerUser(employee, jobInformation);
      saveEmployeeCompensation(employee, jobInformation);
      saveEmployeeJob(employee, currentUser, jobInformation);
    }

    saveEmployeeAddress(employee, employeeDto);

    final WelcomeEmailDto welcomeEmailDto = employeeDto.getWelcomeEmail();

    saveEmailTasks(welcomeEmailDto, employee, currentUser);
  }

  @Override
  public void updateEmployee(final EmployeeDto employeeDto, final User employee) {
    updateEmployeeBasicInformation(employee, employeeDto);
    updateEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());
    updateEmployeeAddress(employee, employeeDto);
  }

  @Override
  public void resendEmail(final EmailResendDto emailResendDto) {
    final User user = userRepository.findById(emailResendDto.getUserId()).orElseThrow(
        () -> new ResourceNotFoundException(
            "User not found with id: " + emailResendDto.getUserId()));

    if (user.getUserStatus().getStatus() != Status.PENDING_VERIFICATION) {
      throw new ForbiddenException("User is not in Pending Verification!");
    }

    final String email = emailResendDto.getEmail();
    final String originalEmail = user.getUserContactInformation().getEmailWork();
    if (!originalEmail.equals(email)) {
      if (userRepository.existsByEmailWork(email)) {
        throw new ForbiddenException("This Email already exists!");
      }
      user.setEmailWork(email);
      UserContactInformation userContactInformation = user.getUserContactInformation();
      if (userContactInformation == null) {
        userContactInformation = new UserContactInformation();
      }
      userContactInformation.setEmailWork(email);
      userRepository.save(user);
      userContactInformationService.update(userContactInformation);
    }

    final Email emailInfo = getWelcomeEmail(originalEmail);

    final Email welcomeEmail = new Email(emailInfo);
    welcomeEmail.setSendDate(Timestamp.from(Instant.now()));
    welcomeEmail.setTo(email);

    emailService.saveAndScheduleEmail(welcomeEmail);
  }

  @Override
  public Email getWelcomeEmail(final String email) {
    return emailRepository.getFirstByToAndSubjectOrderBySendDateDesc(email, subject);
  }

  private String saveEmployeePhoto(final String base64EncodedPhoto) {
    if (Strings.isBlank(base64EncodedPhoto)) {
      return null;
    }

    File file = null;
    try {
      final String imageString = base64EncodedPhoto.split(",")[1];
      file = File.createTempFile(UUID.randomUUID().toString(), ".png");
      final byte[] photo = Base64.getDecoder().decode(imageString);
      FileCopyUtils.copy(photo, file);
      FileValidateUtil
          .validate(file, 2 * FileValidateUtil.MB, FileType.JPEG, FileType.PNG, FileType.GIF);
      return awsUtil.uploadFile(file.getCanonicalPath(), Type.IMAGE);
    } catch (final IOException e) {
      throw new AwsUploadException("Error while upload employee photo!", e);
    }
  }

  private User saveEmployeeBasicInformation(final User currentUser, final EmployeeDto employeeDto) {
    User employee = new User();
    employee.setEmailWork(employeeDto.getEmailWork());

    final String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    final String photoPath = saveEmployeePhoto(base64EncodedPhoto);
    employee.setImageUrl(photoPath);

    employee.setCompany(currentUser.getCompany());

    employee = this.saveInvitedEmployeeAdditionalInformation(employee,
        employeeDto.getUserPersonalInformationDto(),
        employeeDto.getUserContactInformationDto());

    final UserRole userRole = userRoleRepository.findByName(User.Role.NON_MANAGER.name());
    employee.setUserRole(userRole);

    final UserStatus userStatus = userStatusRepository
        .findByName(Status.PENDING_VERIFICATION.name());
    employee.setUserStatus(userStatus);

    employee.setResetPasswordToken(UUID.randomUUID().toString());

    final com.auth0.json.mgmt.users.User user =
        auth0Util.addUser(employeeDto.getEmailWork(),
            null, User.Role.NON_MANAGER.getValue());
    applicationEventPublisher.publishEvent(new Auth0UserCreatedEvent(user));

    String userId = null;
    final Map<String, Object> appMetaData = user.getAppMetadata();
    if (appMetaData != null) {
      userId = (String) appMetaData.get("id");
    }

    employee.setUserId(userId);
    return userRepository.save(employee);
  }

  User saveInvitedEmployeeAdditionalInformation(final User employee,
      final UserPersonalInformationDto userPersonalInformationDto,
      final UserContactInformationDto userContactInformationDto) {
    final UserPersonalInformation userPersonalInformation =
        userPersonalInformationMapper
            .createFromUserPersonalInformationDto(userPersonalInformationDto);

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

    if (userContactInformationDto != null) {
      employee.setUserContactInformation(
          userContactInformationMapper
              .createFromUserContactInformationDto(userContactInformationDto));
    }

    return employee;
  }

  private User updateEmployeeBasicInformation(final User employee, final EmployeeDto employeeDto) {
    final String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    // The photo has been updated
    if (base64EncodedPhoto != null && !base64EncodedPhoto.equals(employee.getImageUrl())) {
      final String photoPath = saveEmployeePhoto(base64EncodedPhoto);
      employee.setImageUrl(photoPath);
    }

    final UserPersonalInformation userPersonalInformation = employee.getUserPersonalInformation();
    final UserPersonalInformationDto userPersonalInformationDto =
        employeeDto.getUserPersonalInformationDto();
    userPersonalInformationMapper
        .updateFromUserPersonalInformationDto(userPersonalInformation, userPersonalInformationDto);
    if (userPersonalInformation != null) {
      userPersonalInformation.setId(userPersonalInformation.getId());
    }
    final UserPersonalInformation savedUserPersonalInformation =
        userPersonalInformationService.update(userPersonalInformation);
    employee.setUserPersonalInformation(savedUserPersonalInformation);

    final UserContactInformation userContactInformation = employee.getUserContactInformation();
    final UserContactInformationDto userContactInformationDto =
        employeeDto.getUserContactInformationDto();
    final UserContactInformation newUserContactInformation =
        userContactInformationMapper
            .updateFromUserContactInformationDto(userContactInformation, userContactInformationDto);
    if (userContactInformation != null) {
      newUserContactInformation.setId(userContactInformation.getId());
    }
    final UserContactInformation savedUserContactInformation =
        userContactInformationService.update(newUserContactInformation);
    employee.setUserContactInformation(savedUserContactInformation);

    return userRepository.save(employee);
  }

  private void saveEmergencyContacts(
      final User employee, final List<UserEmergencyContactDto> emergencyContactDtos) {
    if (CollectionUtils.isEmpty(emergencyContactDtos)) {
      return;
    }

    emergencyContactDtos.forEach(
        emergencyContactDto -> {
          final UserEmergencyContact emergencyContact = userEmergencyContactMapper
              .createFromUserEmergencyContactDto(emergencyContactDto);

          final Long stateProvinceId = emergencyContact.getState().getId();
          if (stateProvinceId != null) {
            final StateProvince stateProvince = stateProvinceRepository.getOne(stateProvinceId);
            emergencyContact.setState(stateProvince);
          } else {
            emergencyContact.setState(null);
          }

          emergencyContact.setUser(employee);
          emergencyContact.setId(null);
          userEmergencyContactRepository.save(emergencyContact);
        });
  }

  private void updateEmergencyContacts(
      final User employee, final List<UserEmergencyContactDto> emergencyContactDtos) {
    final List<BigInteger> userEmergencyContactIds =
        userEmergencyContactRepository.findAllIdByUserId(employee.getId());
    userEmergencyContactRepository.deleteInBatch(userEmergencyContactIds.stream()
        .map(BigInteger::longValue)
        .collect(Collectors.toList()));
    saveEmergencyContacts(employee, emergencyContactDtos);
  }

  private void saveManagerUser(final User user, final NewEmployeeJobInformationDto jobInformation) {
    final Long managerUserId = jobInformation.getReportsTo();
    if (managerUserId != null && !managerUserId.equals(user.getId())) {
      User managerUser =
          userRepository
              .findById(managerUserId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "User with id " + managerUserId + " not found!"));

      if (Role.NON_MANAGER.name().equals(managerUser.getUserRole().getName())) {
        final UserRole userRole = userRoleRepository.findByName(Role.MANAGER.name());
        managerUser.setUserRole(userRole);
        managerUser = userRepository.save(managerUser);
      }

      user.setManagerUser(managerUser);
      userRepository.save(user);
    }
  }

  private void saveEmployeeCompensation(final User user,
      final NewEmployeeJobInformationDto jobInformation) {
    if (jobInformation.getCompensation() != null
        && jobInformation.getCompensationFrequencyId() != null) {
      final UserCompensation userCompensation = new UserCompensation();
      userCompensation.setWage(jobInformation.getCompensation());

      final Long compensationFrequencyId = jobInformation.getCompensationFrequencyId();
      final CompensationFrequency compensationFrequency =
          compensationFrequencyRepository.findById(compensationFrequencyId)
              .orElseThrow(() -> new GeneralException(
                  "CompensationFrequency was not found."));
      userCompensation.setCompensationFrequency(compensationFrequency);
      userCompensation.setUserId(user.getId());
      final UserCompensation userCompensationReturned = userCompensationRepository
          .save(userCompensation);
      user.setUserCompensation(userCompensationReturned);
      userRepository.save(user);
    }
  }

  private void saveEmployeeJob(
      final User employee, final User currentUser,
      final NewEmployeeJobInformationDto jobInformation) {

    final Job job = jobRepository.getOne(jobInformation.getJobId());

    final JobUser jobUser = new JobUser();
    jobUser.setJob(job);
    jobUser.setCompany(currentUser.getCompany());
    jobUser.setUser(employee);

    final Long employmentTypeId = jobInformation.getEmploymentTypeId();
    if (employmentTypeId != null) {
      final EmploymentType employmentType = employmentTypeRepository.getOne(employmentTypeId);
      jobUser.setEmploymentType(employmentType);
    } else {
      jobUser.setEmploymentType(null);
    }

    final Timestamp hireDate = jobInformation.getHireDate();
    if (hireDate != null) {
      jobUser.setStartDate(new Timestamp(hireDate.getTime()));
    }

    final Long officeId = jobInformation.getOfficeId();
    if (officeId != null) {
      final Office office = officeRepository.getOne(officeId);
      jobUser.setOffice(office);
    } else {
      jobUser.setOffice(null);
    }

    jobUserRepository.save(jobUser);
  }

  private void saveEmployeeAddress(final User employee, final EmployeeDto employeeDto) {
    final UserAddressDto userAddressDto = employeeDto.getUserAddress();
    final UserAddress userAddress = userAddressMapper.createFromUserAddressDto(userAddressDto);

    StateProvince stateProvince = userAddress.getStateProvince();
    if (stateProvince != null) {
      stateProvince = stateProvinceRepository.getOne(stateProvince.getId());
      userAddress.setStateProvince(stateProvince);
    } else {
      userAddress.setStateProvince(null);
    }

    Country country = userAddress.getCountry();
    if (country != null) {
      country = countryRepository.getOne(country.getId());
      userAddress.setCountry(country);
    } else {
      userAddress.setCountry(null);
    }

    userAddress.setUser(employee);
    userAddressRepository.save(userAddress);
  }

  private void updateEmployeeAddress(final User employee, final EmployeeDto employeeDto) {
    final UserAddressDto userAddressDto = employeeDto.getUserAddress();
    final UserAddress userAddress = new UserAddress();
    userAddress.setId(userAddressDto.getId());
    userAddress.setUser(employee);
    userAddress.setStreet1(userAddressDto.getStreet1());
    userAddress.setStreet2(userAddressDto.getStreet2());
    userAddress.setCity(userAddressDto.getCity());
    final Long stateProvinceId = employeeDto.getUserAddress().getStateProvinceId();
    if (stateProvinceId != null) {
      final StateProvince stateProvince = stateProvinceRepository.getOne(stateProvinceId);
      userAddress.setStateProvince(stateProvince);
    } else {
      userAddress.setStateProvince(null);
    }
    userAddress.setPostalCode(userAddressDto.getPostalCode());
    userAddressRepository.save(userAddress);
  }

  private void saveEmailTasks(final WelcomeEmailDto welcomeEmailDto, final User employee,
      final User currentUser) {
    final String from = currentUser.getUserContactInformation().getEmailWork();
    final String to = welcomeEmailDto.getSendTo();
    String content = welcomeEmailDto.getPersonalInformation();

    final Context emailContext =
        userService.getWelcomeEmailContext(content, employee.getResetPasswordToken());
    content = userService.getWelcomeEmail(emailContext);
    final Timestamp sendDate = welcomeEmailDto.getSendDate();

    final Email email =
        new Email(from, to, subject, content, currentUser, sendDate);
    emailService.saveAndScheduleEmail(email);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  void removeAuth0User(final Auth0UserCreatedEvent userCreatedEvent) {
    final com.auth0.json.mgmt.users.User auth0User = userCreatedEvent.getUser();
    auth0Util.deleteUser(auth0User.getId());
  }
}
