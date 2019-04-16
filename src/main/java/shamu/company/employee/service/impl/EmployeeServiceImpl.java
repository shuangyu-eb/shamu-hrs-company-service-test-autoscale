package shamu.company.employee.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.entity.CompensationFrequency;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.EmergencyContactPojo;
import shamu.company.employee.pojo.EmployeeInfomationPojo;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.repository.CompensationFrequencyRepository;
import shamu.company.employee.repository.DepartmentRepository;
import shamu.company.employee.repository.EmploymentTypeRepository;
import shamu.company.employee.repository.GenderRepository;
import shamu.company.employee.repository.JobRepository;
import shamu.company.employee.repository.MartialStatusRepository;
import shamu.company.employee.repository.OfficeAddressRepository;
import shamu.company.employee.repository.OfficeRepository;
import shamu.company.employee.repository.StateProvinceRepository;
import shamu.company.employee.repository.UserCompensationRepository;
import shamu.company.employee.repository.UserContactlInformationRepository;
import shamu.company.employee.service.EmployeeService;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.Job;
import shamu.company.job.JobUser;
import shamu.company.job.JobUserRepository;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.FieldCheckUtil;

@Service
public class EmployeeServiceImpl implements EmployeeService {

  @Autowired
  UserRepository userRepository;

  @Autowired
  JobUserRepository jobUserRepository;

  @Autowired
  UserAddressRepository userAddressRepository;

  @Autowired
  private UserPersonalInformationRepository userPersonalInformationRepository;

  @Autowired
  private UserContactlInformationRepository userContactlInformationRepository;

  @Autowired
  DepartmentRepository departmentRepository;

  @Autowired
  private EmploymentTypeRepository employmentTypeRepository;

  @Autowired
  private JobRepository jobRepository;

  @Autowired
  private UserEmergencyContactRepository userEmergencyContactRepository;

  @Autowired
  private OfficeRepository officeRepository;

  @Autowired
  private OfficeAddressRepository officeAddressRepository;

  @Autowired
  private StateProvinceRepository stateProvinceRepository;

  @Autowired
  private UserCompensationRepository userCompensationRepository;

  @Autowired
  private CompensationFrequencyRepository compensationFrequencyRepository;

  @Autowired
  private GenderRepository genderRepository;

  @Autowired
  private MartialStatusRepository martialStatusRepository;

  private static long EMPLOYEE_ROLE_ID = 3;


  private GeneralObjectDto getGeneralDtos(List list, String dtoId, Class clazz) {
    List<GeneralObjectDto> dtos = new ArrayList<>();
    Method idMethod = null;
    Method nameMethod = null;
    try {
      idMethod = clazz.getMethod("getId");
      nameMethod = clazz.getMethod("getName");
      Method finalIdMethod = idMethod;
      finalIdMethod.setAccessible(true);
      Method finalNameMethod = nameMethod;
      finalNameMethod.setAccessible(true);
      list.forEach(element -> {
        GeneralObjectDto dto = new GeneralObjectDto();
        String id = null;
        String name = null;
        try {
          id = String.valueOf(finalIdMethod.invoke(element));
          name = (String) finalNameMethod.invoke(element);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
        dto.setId(id);
        dto.setName(name);
        dtos.add(dto);
      });
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
    GeneralObjectDto listDto = new GeneralObjectDto();
    listDto.setId(dtoId);
    listDto.setName(dtos);
    return listDto;
  }

  @Override
  public List<GeneralObjectDto> getJobInformation() {
    List<GeneralObjectDto> resultList = new ArrayList<>();

    List<Department> departmentList = departmentRepository.findAll();
    resultList.add(getGeneralDtos(departmentList, "department", Department.class));

    List<User> managerList = userRepository.findAllManagers();
    List<GeneralObjectDto> allManagers = new ArrayList<>();
    for (User manager : managerList) {
      GeneralObjectDto managerDto = new GeneralObjectDto();
      String id = String.valueOf(manager.getId());
      UserPersonalInformation userInfo = manager.getUserPersonalInformation();
      String firstName = userInfo.getFirstName();
      String middleName = userInfo.getMiddleName();
      String lastName = userInfo.getLastName();
      String name = firstName + middleName + lastName;
      managerDto.setId(id);
      managerDto.setName(name);
      allManagers.add(managerDto);
    }
    GeneralObjectDto allManagersDtos = new GeneralObjectDto();
    allManagersDtos.setId("reportsTo");
    allManagersDtos.setName(allManagers);
    resultList.add(allManagersDtos);

    List<EmploymentType> employmentTypes = employmentTypeRepository.findAll();
    resultList.add(getGeneralDtos(employmentTypes, "employmentType", EmploymentType.class));

    List<StateProvince> stateProvinces = stateProvinceRepository.findAll();
    resultList.add(getGeneralDtos(stateProvinces, "state", StateProvince.class));

    List<Office> offices = officeRepository.findAll();
    resultList.add(getGeneralDtos(offices, "office", Office.class));

    List<Gender> genders = genderRepository.findAll();
    resultList.add(getGeneralDtos(genders, "gender", Gender.class));

    List<MaritalStatus> martialStatuses = martialStatusRepository.findAll();
    resultList.add(getGeneralDtos(martialStatuses, "maritalStatus", MaritalStatus.class));

    List<CompensationFrequency> compensationFrequencies = compensationFrequencyRepository.findAll();
    resultList.add(
        getGeneralDtos(compensationFrequencies, "compensationUnit", CompensationFrequency.class));

    return resultList;
  }

  @Override
  public User addNewUser(final EmployeeInfomationPojo pojo) {
    User user = new User();
    UserRole userRole = new UserRole();
    userRole.setId(EMPLOYEE_ROLE_ID);
    user.setUserRole(userRole);
    user.setEmailWork(pojo.getWorkEmail());
    return user;
  }

  @Override
  public void saveUser(final User user) {
    User userReturned = userRepository.save(user);
  }

  @Override
  public void handlePersonalInformation(EmployeeInfomationPojo pojo, User user) {
    String firstName = pojo.getFirstName();
    String lastName = pojo.getLastName();

    UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName(firstName);
    userPersonalInformation.setLastName(lastName);

    if ("true".equals(pojo.getSetupOption())) {
      handleFullPersonalInformation(pojo, user, userPersonalInformation);
    }

    UserPersonalInformation returnEntity = userPersonalInformationRepository
        .save(userPersonalInformation);
    user.setUserPersonalInformation(returnEntity);
  }

  @Override
  public void handleFullPersonalInformation(EmployeeInfomationPojo pojo, User user,
      UserPersonalInformation userPersonalInformation) {
    final String socialSecurityNumber = pojo.getSocialSecurityNumber();
    final String gender = pojo.getGender();
    final String maritalStatus = pojo.getMaritalStatus();
    final String dateOfBirth = pojo.getDateOfBirth();
    final String street1 = pojo.getStreet1();
    final String street2 = pojo.getStreet2();
    final String city = pojo.getCity();
    final String state = pojo.getState();
    final String zip = pojo.getZip();

    userPersonalInformation.setSsn(socialSecurityNumber);
    Gender saveGender = genderRepository.findById(Long.parseLong(gender)).get();
    userPersonalInformation.setGender(saveGender);
    MaritalStatus savemaritalStatus = martialStatusRepository
        .findById(Long.parseLong(maritalStatus)).get();
    userPersonalInformation.setMaritalStatus(savemaritalStatus);
    Timestamp birthTimestamp = FieldCheckUtil.getTimestampFromString(dateOfBirth);
    userPersonalInformation.setBirthDate(birthTimestamp);

    UserAddress userAddress = new UserAddress();
    userAddress.setStreet1(street1);
    userAddress.setStreet2(street2);
    userAddress.setPostalCode(zip);
    StateProvince stateProvince = stateProvinceRepository.findById(Long.parseLong(state)).get();
    userAddress.setStateProvince(stateProvince);
    userAddress.setCity(city);
    userAddress.setUser(user);
    userAddressRepository.save(userAddress);
  }

  @Override
  public void handleContactInformation(EmployeeInfomationPojo pojo, User user) {
    String workEmail = pojo.getWorkEmail();
    String personalEmail = pojo.getPersonalEmail();
    String phoneWork = pojo.getPhoneWork();
    String phonePersonal = pojo.getPhonePersonal();

    UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork(workEmail);
    userContactInformation.setEmailWork(personalEmail);
    userContactInformation.setPhoneWork(phoneWork);
    userContactInformation.setPhoneHome(phonePersonal);
    UserContactInformation returnEntity = userContactlInformationRepository
        .save(userContactInformation);
    user.setUserContactInformation(returnEntity);
  }

  @Override
  public void handleJobInformation(EmployeeInfomationPojo pojo, User user) {
    final String jobTitle = pojo.getJobTitle();
    final String employmentTypeId = pojo.getEmployeeType();
    final String hireDate = pojo.getHireDate();
    final String managerId = pojo.getManagerId();
    final String departmentId = pojo.getDepartmentId();
    final String compensation = pojo.getCompensation();
    final String compensationUnit = pojo.getCompensationUnit();
    final String officeId = pojo.getOfficeLocation();
    JobUser jobUser = new JobUser();
    if (!"".equals(managerId) && managerId != null) {
      User managerUser = userRepository.findByManagerUser(Long.parseLong(managerId));
      user.setManagerUser(managerUser);
    }
    jobUser.setUser(user);
    Job job = new Job();
    job.setTitle(jobTitle);
    Job jobReturned = jobRepository.save(job);
    jobUser.setJob(jobReturned);

    if (!"".equals(departmentId) && departmentId != null) {
      Department department = new Department();
      department.setId(Long.parseLong(departmentId));
      Department departmentReturned = departmentRepository.save(department);
      jobUser.setDepartment(departmentReturned);
    }
    if (!"".equals(employmentTypeId) && employmentTypeId != null) {
      EmploymentType employmentType = new EmploymentType();
      employmentType.setId(Long.parseLong(employmentTypeId));
      EmploymentType employmentTypeReturned = employmentTypeRepository.save(employmentType);
      jobUser.setEmploymentType(employmentTypeReturned);

    }
    if (!"".equals(officeId) && officeId != null) {
      Office office = officeRepository.findById(Long.parseLong(officeId)).get();
      jobUser.setOffice(office);
    }

    jobUser.setStartDate(FieldCheckUtil.getTimestampFromString(hireDate));
    jobUserRepository.save(jobUser);
    UserCompensation userCompensation = new UserCompensation();
    userCompensation.setWage("".equals(compensation) ? 0 : Integer.valueOf(compensation));
    userCompensation.setUser(user);
    if (!"".equals(compensationUnit) && compensationUnit != null) {
      CompensationFrequency compensationFrequency = compensationFrequencyRepository
          .findById(Long.parseLong(compensationUnit)).get();
      userCompensation.setCompensationFrequency(compensationFrequency);
    }
    userCompensationRepository.save(userCompensation);

  }

  @Override
  public void handelEmergencyContacts(EmployeeInfomationPojo employeePojo, User user) {
    if (employeePojo.getSetupOption().equals("false")) {
      return;
    }
    List<EmergencyContactPojo> list = employeePojo.getEmergencyContactPojoList();
    List<UserEmergencyContact> entityList = new ArrayList<>();
    list.forEach(contactPojo -> {
      UserEmergencyContact contactEntity = new UserEmergencyContact();
      contactEntity.setEmail(contactPojo.getEmergencyContactEmail());
      contactEntity.setPhone(contactPojo.getEmergencyContactPhone());
      contactEntity.setCity(contactPojo.getEmergencyContactCity());
      contactEntity.setFirstName(contactPojo.getEmergencyContactFirstName());
      contactEntity.setLastName(contactPojo.getEmergencyContactLastName());
      contactEntity.setStreet1(contactPojo.getEmergencyContactStreet1());
      contactEntity.setStreet2(contactPojo.getEmergencyContactStreet2());
      contactEntity.setPostalCode(contactPojo.getEmergencyContactZip());
      boolean isPrimary = Boolean.valueOf(contactPojo.getEmergencyContactIsPrimary());
      contactEntity.setIsPrimary(isPrimary);
      contactEntity.setRelationship(contactPojo.getRelationship());
      contactEntity.setUserId(user.getId());
      entityList.add(contactEntity);
    });
    userEmergencyContactRepository.saveAll(entityList);

  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean saveEmploymentType(String employmentTypeName) {
    boolean returnResult = false;
    try {
      EmploymentType employmentType = new EmploymentType();
      employmentType.setName(employmentTypeName);
      employmentTypeRepository.save(employmentType);
      returnResult = true;
    } catch (Exception e) {
      e.printStackTrace();
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    } finally {
      return returnResult;
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean saveDepartment(String departmentName) {
    boolean returnResult = false;
    try {
      Department department = new Department();
      department.setName(departmentName);
      departmentRepository.save(department);
      returnResult = true;
    } catch (Exception e) {
      e.printStackTrace();
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    } finally {
      return returnResult;
    }

  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean saveOfficeLocation(OfficePojo officePojo) {
    boolean returnResult = false;
    try {
      final Office office = new Office();
      OfficeAddress officeAddress = new OfficeAddress();
      officeAddress.setCity(officePojo.getCity());
      if (!"".equals(officePojo.getState()) && null != officePojo.getState()) {
        StateProvince state = stateProvinceRepository
            .findById(Long.parseLong(officePojo.getState())).get();
        officeAddress.setStateProvince(state);
      }
      officeAddress.setPostalCode(officePojo.getZip());
      officeAddress.setStreet1(officePojo.getStreet1());
      officeAddress.setStreet2(officePojo.getStreet2());
      OfficeAddress officeAddressReturned = officeAddressRepository.save(officeAddress);
      office.setName(officePojo.getOfficeName());
      office.setOfficeAddress(officeAddressReturned);
      officeRepository.save(office);
      returnResult = true;
    } catch (Exception e) {
      e.printStackTrace();
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    } finally {
      return returnResult;
    }
  }
}
