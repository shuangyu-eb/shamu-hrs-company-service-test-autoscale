package shamu.company.employee.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
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
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.event.Auth0UserCreatedEvent;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;
import shamu.company.utils.DateUtil;
import shamu.company.utils.FileValidateUtil;
import shamu.company.utils.FileValidateUtil.FileType;

@Service
@Transactional
public class EmployeeService {

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

  private final UserStatusRepository userStatusRepository;

  private final EmailRepository emailRepository;

  private final EmailService emailService;

  private final JobUserService jobUserService;

  private final OfficeRepository officeRepository;

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserContactInformationService userContactInformationService;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  private final UserAddressMapper userAddressMapper;

  private final UserContactInformationMapper userContactInformationMapper;

  private final UserEmergencyContactMapper userEmergencyContactMapper;

  private final JobUserMapper jobUserMapper;

  private final UserMapper userMapper;

  private final Auth0Util auth0Util;

  private final ApplicationEventPublisher applicationEventPublisher;

  private final UserRoleService userRoleService;

  private static final String subject = "Welcome to ";

  @Autowired
  public EmployeeService(
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
      final ApplicationEventPublisher applicationEventPublisher,
      final JobUserMapper jobUserMapper,
      @Lazy final JobUserService jobUserService,
      final UserMapper userMapper,
      final UserRoleService userRoleService) {
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
    this.jobUserMapper = jobUserMapper;
    this.jobUserService = jobUserService;
    this.userMapper = userMapper;
    this.userRoleService = userRoleService;
  }

  public List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(
      final Long departmentId, final Long companyId) {
    return userRepository.findEmployersAndEmployeesByDepartmentIdAndCompanyId(
        departmentId, companyId);
  }

  public List<User> findDirectReportsEmployersAndEmployeesByCompanyId(
      final Long companyId, final Long userId) {
    return userRepository.findDirectReportsEmployersAndEmployeesByCompanyId(
        companyId, userId);
  }

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

  public void updateEmployee(final EmployeeDto employeeDto, final User employee) {

    updateEmployeeBasicInformation(employee, employeeDto);
    updateEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());
    updateEmployeeAddress(employee, employeeDto);
  }

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
      if (userRepository.findByEmailWork(email) != null) {
        throw new ForbiddenException("This email already exists!");
      }

      auth0Util.updateEmail(user.getUserId(), emailResendDto.getEmail());
      final UserContactInformation userContactInformation = user.getUserContactInformation();
      userContactInformation.setEmailWork(email);
      userRepository.save(user);
      userContactInformationService.update(userContactInformation);
    }

    final Email emailInfo = getWelcomeEmail(originalEmail, user.getCompany().getName());

    final Email welcomeEmail = new Email(emailInfo);
    welcomeEmail.setSendDate(Timestamp.from(Instant.now()));
    welcomeEmail.setTo(email);

    emailService.saveAndScheduleEmail(welcomeEmail);
  }

  public Email getWelcomeEmail(final String email, final String companyName) {
    return emailRepository.getFirstByToAndSubjectOrderBySendDateDesc(email, subject + companyName);
  }

  private String saveEmployeePhoto(final String base64EncodedPhoto) {
    if (Strings.isBlank(base64EncodedPhoto)) {
      return null;
    }

    final File file;
    try {
      final String imageString = base64EncodedPhoto.split(",")[1];
      file = File.createTempFile(UUID.randomUUID().toString(), ".png");
      final byte[] photo = Base64.getDecoder().decode(imageString);
      FileCopyUtils.copy(photo, file);
      FileValidateUtil
          .validate(file, 2 * FileValidateUtil.MB, FileType.JPEG, FileType.PNG, FileType.GIF);
      return awsUtil.uploadFile(file.getCanonicalPath(), Type.IMAGE);
    } catch (final IOException e) {
      throw new AwsUploadException("Error while uploading employee photo!", e);
    }
  }

  private User saveEmployeeBasicInformation(final User currentUser, final EmployeeDto employeeDto) {
    User employee = new User();

    final String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    final String photoPath = saveEmployeePhoto(base64EncodedPhoto);
    employee.setImageUrl(photoPath);

    employee.setCompany(currentUser.getCompany());

    employee = saveInvitedEmployeeAdditionalInformation(employee,
        employeeDto);

    final UserStatus userStatus = userStatusRepository
        .findByName(Status.PENDING_VERIFICATION.name());
    employee.setUserStatus(userStatus);

    employee.setResetPasswordToken(UUID.randomUUID().toString());

    final com.auth0.json.mgmt.users.User user =
        auth0Util.addUser(employeeDto.getEmailWork(),
            null, Role.EMPLOYEE.getValue());
    applicationEventPublisher.publishEvent(new Auth0UserCreatedEvent(user));

    final String userId = auth0Util.getUserId(user);
    employee.setUserId(userId);
    employee.setUserRole(userRoleService.getEmployee());
    return userRepository.save(employee);
  }

  User saveInvitedEmployeeAdditionalInformation(final User employee,
      final EmployeeDto employeeDto) {
    final UserPersonalInformation userPersonalInformation =
        userPersonalInformationMapper
            .createFromUserPersonalInformationDto(employeeDto.getUserPersonalInformationDto());

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

    UserContactInformation userContactInformation = userContactInformationMapper
        .createFromUserContactInformationDto(employeeDto.getUserContactInformationDto());

    if (userContactInformation == null) {
      userContactInformation = new UserContactInformation();
    }

    userContactInformation.setEmailWork(employeeDto.getEmailWork());
    employee.setUserContactInformation(userContactInformation);
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

    employee.setVerifiedAt(Timestamp.valueOf(DateUtil.getLocalUtcTime()));
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
    if (!emergencyContactDtos.isEmpty()) {
      final List<BigInteger> userEmergencyContactIds =
          userEmergencyContactRepository.findAllIdByUserId(employee.getId());
      userEmergencyContactRepository.deleteInBatch(userEmergencyContactIds.stream()
          .map(BigInteger::longValue)
          .collect(Collectors.toList()));
      saveEmergencyContacts(employee, emergencyContactDtos);
    }
  }

  private void saveManagerUser(final User user, final NewEmployeeJobInformationDto jobInformation) {
    final Long managerUserId = jobInformation.getReportsTo();
    if (managerUserId != null && !managerUserId.equals(user.getId())) {
      final User managerUser =
          userRepository
              .findById(managerUserId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "User with id " + managerUserId + " not found!"));

      if (StringUtils.equals(managerUser.getUserRole().getName(), Role.EMPLOYEE.getValue())) {
        managerUser.setUserRole(userRoleService.getManager());
      }

      user.setManagerUser(managerUser);
      userRepository.saveAll(Arrays.asList(managerUser, user));
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
    emailContext.setVariable("companyName", currentUser.getCompany().getName());
    content = userService.getWelcomeEmail(emailContext);
    final Timestamp sendDate = welcomeEmailDto.getSendDate();
    final String fullSubject = subject + currentUser.getCompany().getName();
    final Email email =
        new Email(from, to, fullSubject, content, currentUser, sendDate);
    emailService.saveAndScheduleEmail(email);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  @SuppressWarnings("unused")
  public void removeAuth0User(final Auth0UserCreatedEvent userCreatedEvent) {
    final com.auth0.json.mgmt.users.User auth0User = userCreatedEvent.getUser();
    auth0Util.deleteUser(auth0User.getId());
  }

  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(final Long id) {
    final User employee = userService.findUserById(id);
    final String emailAddress = employee.getUserContactInformation().getEmailWork();
    final Status userStatus = employee.getUserStatus().getStatus();
    final String roleName = employee.getUserRole().getName();

    Timestamp sendDate = null;
    if (userStatus == Status.PENDING_VERIFICATION) {

      final Email email = getWelcomeEmail(emailAddress, employee.getCompany().getName());
      sendDate = email != null ? email.getSendDate() : null;
    }

    JobUserDto managerjobUserDto = null;
    if (employee.getManagerUser() != null) {
      managerjobUserDto =
          userService.findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    }
    final JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    final List<JobUserDto> reports = userService.findDirectReportsByManagerId(id).stream()
        .map(user -> userService.findEmployeeInfoByEmployeeId(user.getId()))
        .collect(Collectors.toList());

    return jobUserMapper.convertToEmployeeRelatedInformationDto(id, emailAddress,
        userStatus.name(), sendDate, jobUserDto,
        managerjobUserDto, reports, roleName);
  }

  public BasicUserPersonalInformationDto getPersonalMessage(final Long targetUserId,
      final Long authUserId) {
    final User targetUser = userService.findUserById(targetUserId);
    final UserPersonalInformation personalInformation = targetUser.getUserPersonalInformation();

    // The user's full personal message can only be accessed by admin and himself.
    final User currentUser = userService.findUserById(authUserId);
    final Role userRole = currentUser.getRole();

    if (authUserId.equals(targetUserId) || userRole == Role.ADMIN) {
      return userPersonalInformationMapper
          .convertToEmployeePersonalInformationDto(personalInformation);
    }
    if (targetUser.getManagerUser().getId().equals(authUserId)) {
      return userPersonalInformationMapper
          .convertToUserPersonalInformationForManagerDto(personalInformation);
    }

    return userPersonalInformationMapper
        .convertToBasicUserPersonalInformationDto(personalInformation);
  }

  public BasicUserContactInformationDto getContactMessage(final Long targetUserId,
      final Long authUserId) {
    final User targetUser = userService.findUserById(targetUserId);
    final UserContactInformation contactInformation = targetUser.getUserContactInformation();

    // The user's full contact message can only be accessed by admin, the manager and himself.
    final User currentUser = userService.findUserById(authUserId);
    final Role userRole = currentUser.getRole();
    if (authUserId.equals(targetUserId)
        || targetUser.getManagerUser().getId().equals(authUserId)
        || userRole == Role.ADMIN) {
      return userContactInformationMapper
          .convertToEmployeeContactInformationDto(contactInformation);
    }

    return userContactInformationMapper.convertToBasicUserContactInformationDto(contactInformation);
  }

  public BasicJobInformationDto getJobMessage(final Long targetUserId,
      final Long authUserId) {
    final JobUser target = jobUserService.getJobUserByUserId(targetUserId);

    if (target == null) {
      final User targetUser = userService.findUserById(targetUserId);
      return userMapper.convertToBasicJobInformationDto(targetUser);
    }

    // The user's full job message can only be accessed by admin, the manager and himself.
    final User currentUser = userService.findUserById(authUserId);
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
