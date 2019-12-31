package shamu.company.employee.service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import shamu.company.common.exception.AwsException;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.entity.Office;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDetailDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.event.Auth0UserCreatedEvent;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.helpers.s3.Type;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobService;
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
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.event.UserEmailUpdatedEvent;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.FileValidateUtil;
import shamu.company.utils.FileValidateUtil.FileType;
import shamu.company.utils.UuidUtil;

@Service
@Transactional
public class EmployeeService {

  private static final String SUBJECT = "Welcome to ";
  private final UserAddressService userAddressService;
  private final EmploymentTypeService employmentTypeService;
  private final UserCompensationService userCompensationService;
  private final JobService jobService;
  private final GenderService genderService;
  private final MaritalStatusService maritalStatusService;
  private final AwsHelper awsHelper;
  private final CompensationFrequencyService compensationFrequencyService;
  private final StateProvinceService stateProvinceService;
  private final CountryService countryService;
  private final UserEmergencyContactService userEmergencyContactService;
  private final UserService userService;
  private final UserStatusService userStatusService;
  private final EmailService emailService;
  private final JobUserService jobUserService;
  private final OfficeService officeService;
  private final UserPersonalInformationService userPersonalInformationService;
  private final UserContactInformationService userContactInformationService;
  private final UserPersonalInformationMapper userPersonalInformationMapper;
  private final UserAddressMapper userAddressMapper;
  private final UserContactInformationMapper userContactInformationMapper;
  private final UserEmergencyContactMapper userEmergencyContactMapper;
  private final JobUserMapper jobUserMapper;
  private final Auth0Helper auth0Helper;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final UserRoleService userRoleService;

  private final EncryptorUtil encryptorUtil;

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;

  @Autowired
  public EmployeeService(
      final UserAddressService userAddressService,
      final EmploymentTypeService employmentTypeService,
      final OfficeService officeService,
      final UserService userService,
      final StateProvinceService stateProvinceService,
      final CountryService countryService,
      final UserCompensationService userCompensationService,
      final UserEmergencyContactService userEmergencyContactService,
      final JobService jobService,
      final UserStatusService userStatusService,
      final AwsHelper awsHelper,
      final GenderService genderService,
      final MaritalStatusService maritalStatusService,
      final EmailService emailService,
      final CompensationFrequencyService compensationFrequencyService,
      final UserPersonalInformationService userPersonalInformationService,
      final UserContactInformationService userContactInformationService,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserAddressMapper userAddressMapper,
      final UserContactInformationMapper userContactInformationMapper,
      final UserEmergencyContactMapper userEmergencyContactMapper,
      final Auth0Helper auth0Helper,
      final ApplicationEventPublisher applicationEventPublisher,
      final JobUserMapper jobUserMapper,
      @Lazy final JobUserService jobUserService,
      final UserRoleService userRoleService,
      final EncryptorUtil encryptorUtil) {
    this.userAddressService = userAddressService;
    this.employmentTypeService = employmentTypeService;
    this.userService = userService;
    this.stateProvinceService = stateProvinceService;
    this.countryService = countryService;
    this.userCompensationService = userCompensationService;
    this.userEmergencyContactService = userEmergencyContactService;
    this.jobService = jobService;
    this.userStatusService = userStatusService;
    this.awsHelper = awsHelper;
    this.genderService = genderService;
    this.maritalStatusService = maritalStatusService;
    this.emailService = emailService;
    this.compensationFrequencyService = compensationFrequencyService;
    this.officeService = officeService;
    this.userPersonalInformationService = userPersonalInformationService;
    this.userContactInformationService = userContactInformationService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userAddressMapper = userAddressMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userEmergencyContactMapper = userEmergencyContactMapper;
    this.auth0Helper = auth0Helper;
    this.applicationEventPublisher = applicationEventPublisher;
    this.jobUserMapper = jobUserMapper;
    this.jobUserService = jobUserService;
    this.userRoleService = userRoleService;
    this.encryptorUtil = encryptorUtil;
  }

  public List<User> findByCompanyId(final String companyId) {
    return userService.findByCompanyId(companyId);
  }

  public List<User> findDirectReportsByManagerUserId(
      final String companyId, final String userId) {
    return userService.findDirectReportsByManagerUserId(
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
    saveEmailTasks(employeeDto.getWelcomeEmail(), employee, currentUser);
  }

  public void updateEmployee(final EmployeeDto employeeDto, final User employee) {

    updateEmployeeBasicInformation(employee, employeeDto);
    updateEmergencyContacts(employee, employeeDto.getUserEmergencyContactDto());
    updateEmployeeAddress(employee, employeeDto);
  }

  public void resendEmail(final EmailResendDto emailResendDto) {
    final User user = userService.findById(emailResendDto.getUserId());

    if (user.getUserStatus().getStatus() != Status.PENDING_VERIFICATION) {
      throw new ForbiddenException("User is not in Pending Verification!");
    }

    final String email = emailResendDto.getEmail();
    final String originalEmail = user.getUserContactInformation().getEmailWork();
    if (!originalEmail.equals(email)) {
      if (userService.findByEmailWork(email) != null) {
        throw new ForbiddenException("This email already exists!");
      }

      auth0Helper.updateEmail(user.getId(), emailResendDto.getEmail());
      applicationEventPublisher
          .publishEvent(new UserEmailUpdatedEvent(user.getId(), originalEmail));
      user.getUserContactInformation().setEmailWork(email);
      userService.save(user);
    }

    final Email emailInfo = findWelcomeEmail(originalEmail, user.getCompany().getName());
    emailInfo.setSendDate(Timestamp.from(Instant.now()));
    emailInfo.setTo(email);
    emailService.saveAndScheduleEmail(emailInfo);
  }

  public Email findWelcomeEmail(final String email, final String companyName) {
    return emailService.findFirstByToAndSubjectOrderBySendDateDesc(email, SUBJECT + companyName);
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
      return awsHelper.uploadFile(file.getCanonicalPath(), Type.IMAGE);
    } catch (final IOException e) {
      throw new AwsException("Error while uploading employee photo!", e);
    }
  }

  private User saveEmployeeBasicInformation(final User currentUser, final EmployeeDto employeeDto) {
    final User employee = new User();
    employee.setSalt(UuidUtil.getUuidString());
    final String base64EncodedPhoto = employeeDto.getPersonalPhoto();
    final String photoPath = saveEmployeePhoto(base64EncodedPhoto);
    employee.setImageUrl(photoPath);

    employee.setCompany(currentUser.getCompany());

    final UserStatus userStatus = userStatusService
        .findByName(Status.PENDING_VERIFICATION.name());
    employee.setUserStatus(userStatus);

    employee.setResetPasswordToken(UUID.randomUUID().toString());

    final com.auth0.json.mgmt.users.User user =
        auth0Helper.addUser(employeeDto.getEmailWork(),
            null, Role.EMPLOYEE.getValue());
    applicationEventPublisher.publishEvent(new Auth0UserCreatedEvent(user));

    final String userId = auth0Helper.getUserId(user);
    employee.setId(userId);
    employee.setUserRole(userRoleService.getEmployee());
    saveInvitedEmployeeAdditionalInformation(employee, employeeDto);
    return userService.save(employee);
  }

  private void saveInvitedEmployeeAdditionalInformation(final User employee,
      final EmployeeDto employeeDto) {
    final UserPersonalInformation userPersonalInformation =
        userPersonalInformationMapper
            .createFromUserPersonalInformationDto(employeeDto.getUserPersonalInformationDto());

    if (userPersonalInformation != null) {
      Gender gender = userPersonalInformation.getGender();
      if (userPersonalInformation.getGender() != null
          && !StringUtils.isEmpty(gender.getId())) {
        gender = genderService.findById(gender.getId());
        userPersonalInformation.setGender(gender);
      }

      MaritalStatus martialStatus = userPersonalInformation.getMaritalStatus();
      if (martialStatus != null && !StringUtils.isEmpty(martialStatus.getId())) {
        martialStatus = maritalStatusService.findById(martialStatus.getId());
        userPersonalInformation.setMaritalStatus(martialStatus);
      }

      encryptorUtil
          .encryptSsn(employee, userPersonalInformation.getSsn(), userPersonalInformation);

      employee.setUserPersonalInformation(userPersonalInformation);
    }

    UserContactInformation userContactInformation = userContactInformationMapper
        .createFromUserContactInformationDto(employeeDto.getUserContactInformationDto());

    if (userContactInformation == null) {
      userContactInformation = new UserContactInformation();
    }

    userContactInformation.setEmailWork(employeeDto.getEmailWork());
    employee.setUserContactInformation(userContactInformation);
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

    encryptorUtil
        .encryptSsn(employee.getId(), userPersonalInformationDto.getSsn(), userPersonalInformation);

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
    return userService.save(employee);
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

          final StateProvince state = emergencyContact.getState();
          if (null != state && !StringUtils.isEmpty(state.getId())) {
            final StateProvince stateProvince = stateProvinceService.findById(state.getId());
            emergencyContact.setState(stateProvince);
          } else {
            emergencyContact.setState(null);
          }

          emergencyContact.setUser(employee);
          emergencyContact.setId(null);
          userEmergencyContactService.save(emergencyContact);
        });
  }

  private void updateEmergencyContacts(
      final User employee, final List<UserEmergencyContactDto> emergencyContactDtos) {
    if (emergencyContactDtos.isEmpty()) {
      return;
    }

    final List<String> userEmergencyContactIds =
        userEmergencyContactService.findAllIdByUserId(employee.getId());
    if (!userEmergencyContactIds.isEmpty()) {
      userEmergencyContactService.deleteInBatch(userEmergencyContactIds);
    }
    saveEmergencyContacts(employee, emergencyContactDtos);
  }

  private void saveManagerUser(final User user, final NewEmployeeJobInformationDto jobInformation) {
    final String managerUserId = jobInformation.getReportsTo();
    if (StringUtils.isEmpty(managerUserId) || managerUserId.equals(user.getId())) {
      return;
    }

    final User managerUser =
        userService.findById(managerUserId);
    if (StringUtils.equals(managerUser.getUserRole().getName(), Role.EMPLOYEE.getValue())) {
      managerUser.setUserRole(userRoleService.getManager());
    }

    user.setManagerUser(managerUser);
    userService.save(managerUser);
  }

  private void saveEmployeeCompensation(final User user,
      final NewEmployeeJobInformationDto jobInformation) {

    if (jobInformation.getCompensation() == null
        || jobInformation.getCompensationFrequencyId() == null) {
      return;
    }

    final UserCompensation userCompensation = new UserCompensation();
    userCompensation.setWage(jobInformation.getCompensation());

    final String compensationFrequencyId = jobInformation.getCompensationFrequencyId();
    final CompensationFrequency compensationFrequency =
        compensationFrequencyService.findById(compensationFrequencyId);
    userCompensation.setCompensationFrequency(compensationFrequency);
    userCompensation.setUserId(user.getId());
    final UserCompensation userCompensationReturned = userCompensationService
        .save(userCompensation);
    user.setUserCompensation(userCompensationReturned);
  }

  private void saveEmployeeJob(
      final User employee, final User currentUser,
      final NewEmployeeJobInformationDto jobInformation) {

    final Job job = jobService.findById(jobInformation.getJobId());

    final JobUser jobUser = new JobUser();
    jobUser.setJob(job);
    jobUser.setCompany(currentUser.getCompany());
    jobUser.setUser(employee);

    final String employmentTypeId = jobInformation.getEmploymentTypeId();
    if (StringUtils.isNotEmpty(employmentTypeId)) {
      final EmploymentType employmentType = employmentTypeService.findById(employmentTypeId);
      jobUser.setEmploymentType(employmentType);
    }

    final Timestamp hireDate = jobInformation.getHireDate();
    if (hireDate != null) {
      jobUser.setStartDate(new Timestamp(hireDate.getTime()));
    }

    final String officeId = jobInformation.getOfficeId();
    if (StringUtils.isNotEmpty(officeId)) {
      final Office office = officeService.findById(officeId);
      jobUser.setOffice(office);
    }

    jobUserService.save(jobUser);
  }

  private void saveEmployeeAddress(final User employee, final EmployeeDto employeeDto) {
    final UserAddressDto userAddressDto = employeeDto.getUserAddress();
    final UserAddress userAddress = userAddressMapper.createFromUserAddressDto(userAddressDto);

    StateProvince stateProvince = userAddress.getStateProvince();
    if (stateProvince != null) {
      stateProvince = stateProvinceService.findById(stateProvince.getId());
      userAddress.setStateProvince(stateProvince);
    }

    Country country = userAddress.getCountry();
    if (country != null) {
      country = countryService.findById(country.getId());
      userAddress.setCountry(country);
    }

    userAddress.setUser(employee);
    userAddressService.save(userAddress);
  }

  private void updateEmployeeAddress(final User employee, final EmployeeDto employeeDto) {
    final UserAddressDto userAddressDto = employeeDto.getUserAddress();
    final UserAddress userAddress = new UserAddress();
    BeanUtils.copyProperties(userAddressDto, userAddress);
    userAddress.setUser(employee);
    final String stateProvinceId = employeeDto.getUserAddress().getStateId();
    if (StringUtils.isNotEmpty(stateProvinceId)) {
      final StateProvince stateProvince = stateProvinceService.findById(stateProvinceId);
      userAddress.setStateProvince(stateProvince);
    }
    if (StringUtils.isNotEmpty(userAddressDto.getCountryId())) {
      final Country country = new Country();
      country.setId(userAddressDto.getCountryId());
      userAddress.setCountry(country);
    }
    userAddress.setPostalCode(userAddressDto.getPostalCode());
    userAddressService.save(userAddress);
  }

  private void saveEmailTasks(final WelcomeEmailDto welcomeEmailDto, final User employee,
      final User currentUser) {
    final String toEmail = welcomeEmailDto.getSendTo();
    String content = welcomeEmailDto.getPersonalInformation();

    final Context emailContext =
        userService.getWelcomeEmailContext(content, employee.getResetPasswordToken(), toEmail);
    emailContext.setVariable("companyName", currentUser.getCompany().getName());
    content = userService.getWelcomeEmail(emailContext);
    final Timestamp sendDate = welcomeEmailDto.getSendDate();
    final String fullSubject = SUBJECT + currentUser.getCompany().getName();
    final Email email =
        new Email(systemEmailAddress, toEmail, fullSubject, content, currentUser, sendDate);
    emailService.saveAndScheduleEmail(email);
  }

  public EmployeeDetailDto getEmployeeInfoByUserId(final String id) {
    final User employee = userService.findById(id);
    final String emailAddress = employee.getUserContactInformation().getEmailWork();
    final Status userStatus = employee.getUserStatus().getStatus();
    final String roleName = employee.getUserRole().getName();

    Timestamp sendDate = null;
    Email email = null;
    if (userStatus == Status.PENDING_VERIFICATION
        && ((email = findWelcomeEmail(emailAddress, employee.getCompany().getName())) != null)) {
      sendDate = email.getSendDate();
    }

    JobUserDto managerJobUserDto = null;
    if (employee.getManagerUser() != null) {
      managerJobUserDto =
          userService.findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    }
    final JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    final List<JobUserDto> reports = userService.findDirectReportsByManagerId(id).stream()
        .map(user -> userService.findEmployeeInfoByEmployeeId(user.getId()))
        .collect(Collectors.toList());

    return jobUserMapper.convertToEmployeeRelatedInformationDto(emailAddress,
        userStatus.name(), sendDate, jobUserDto,
        managerJobUserDto, reports, roleName);
  }

  public BasicUserPersonalInformationDto findPersonalMessage(final String targetUserId,
      final String authUserId) {
    final User targetUser = userService.findById(targetUserId);
    final UserPersonalInformation personalInformation = targetUser.getUserPersonalInformation();

    // The user's full personal message can only be accessed by admin and himself.
    final User currentUser = userService.findById(authUserId);
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

  public BasicUserContactInformationDto findContactMessage(final String targetUserId,
      final String authUserId) {
    final User targetUser = userService.findById(targetUserId);
    final UserContactInformation contactInformation = targetUser.getUserContactInformation();

    // The user's full contact message can only be accessed by admin, the manager and himself.
    final User currentUser = userService.findById(authUserId);
    final Role userRole = currentUser.getRole();
    if (authUserId.equals(targetUserId)
        || targetUser.getManagerUser().getId().equals(authUserId)
        || userRole == Role.ADMIN) {
      return userContactInformationMapper
          .convertToEmployeeContactInformationDto(contactInformation);
    }

    return userContactInformationMapper.convertToBasicUserContactInformationDto(contactInformation);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  @SuppressWarnings("unused")
  public void removeAuth0User(final Auth0UserCreatedEvent userCreatedEvent) {
    final com.auth0.json.mgmt.users.User auth0User = userCreatedEvent.getUser();
    auth0Helper.deleteUser(auth0User.getId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  @SuppressWarnings("unused")
  public void restoreUserRole(final UserEmailUpdatedEvent userEmailUpdatedEvent) {
    auth0Helper.updateEmail(userEmailUpdatedEvent.getUserId(), userEmailUpdatedEvent.getEmail());
  }
}
