package shamu.company.employee.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.Repository.*;
import shamu.company.employee.dto.NormalObjectDTO;
import shamu.company.employee.entity.CompensationFrequency;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.EmergencyContactPojo;
import shamu.company.employee.pojo.EmployeeInfomationPojo;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.Job;
import shamu.company.job.JobUser;
import shamu.company.job.JobUserRepository;
import shamu.company.user.entity.*;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserEmergencyContactRepository;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.FieldCheckUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<NormalObjectDTO> getJobInformation() {
        List<NormalObjectDTO> resultList = new ArrayList<>();

        List<Department> departmentList = departmentRepository.findAll();
        List<NormalObjectDTO> allDepartments = new ArrayList<>();
        for(Department department : departmentList) {
            NormalObjectDTO departmentDto = new NormalObjectDTO();
            String id = String.valueOf(department.getId());
            String name = department.getName();
            departmentDto.setId(id);
            departmentDto.setName(name);
            allDepartments.add(departmentDto);
        }
        NormalObjectDTO allDepartmentDtos = new NormalObjectDTO();
        allDepartmentDtos.setId("department");
        allDepartmentDtos.setName(allDepartments);
        resultList.add(allDepartmentDtos);

        List<User> managerList = userRepository.findAllManagers();
        List<NormalObjectDTO> allManagers = new ArrayList<>();
        for(User manager : managerList) {
            NormalObjectDTO managerDto = new NormalObjectDTO();
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
        NormalObjectDTO allManagersDtos = new NormalObjectDTO();
        allManagersDtos.setId("reportsTo");
        allManagersDtos.setName(allManagers);
        resultList.add(allManagersDtos);

        List<EmploymentType> employmentTypes = employmentTypeRepository.findAll();
        List<NormalObjectDTO> allEmploymentTypes = new ArrayList<>();
        for(EmploymentType employmentType : employmentTypes) {
            NormalObjectDTO employmentTypeDto = new NormalObjectDTO();
            String id = String.valueOf(employmentType.getId());
            String name = employmentType.getName();
            employmentTypeDto.setId(id);
            employmentTypeDto.setName(name);
            allEmploymentTypes.add(employmentTypeDto);
        }
        NormalObjectDTO allEmploymentTypeDtos = new NormalObjectDTO();
        allEmploymentTypeDtos.setId("employmentType");
        allEmploymentTypeDtos.setName(allEmploymentTypes);
        resultList.add(allEmploymentTypeDtos);

        List<StateProvince> stateProvinces = stateProvinceRepository.findAll();
        List<NormalObjectDTO> stateDtos = new ArrayList<>();
        for(StateProvince state : stateProvinces) {
            NormalObjectDTO stateDto = new NormalObjectDTO();
            stateDto.setId(String.valueOf(state.getId()));
            stateDto.setName(state.getName());
            stateDtos.add(stateDto);
        }
        NormalObjectDTO allstateDtos = new NormalObjectDTO();
        allstateDtos.setId("state");
        allstateDtos.setName(stateDtos);
        resultList.add(allstateDtos);

        List<Office> offices = officeRepository.findAll();
        List<NormalObjectDTO> officeDtos = new ArrayList<>();
        for(Office office : offices) {
            NormalObjectDTO officeDto = new NormalObjectDTO();
            officeDto.setId(String.valueOf(office.getId()));
            officeDto.setName(office.getName());
            officeDtos.add(officeDto);
        }
        NormalObjectDTO allofficeDtos = new NormalObjectDTO();
        allofficeDtos.setId("office");
        allofficeDtos.setName(officeDtos);
        resultList.add(allofficeDtos);

        List<Gender> genders = genderRepository.findAll();
        List<NormalObjectDTO> genderDtos = new ArrayList<>();
        for(Gender gender : genders) {
            NormalObjectDTO genderDto = new NormalObjectDTO();
            genderDto.setId(String.valueOf(gender.getId()));
            genderDto.setName(gender.getName());
            genderDtos.add(genderDto);
        }
        NormalObjectDTO allGenderDtos = new NormalObjectDTO();
        allGenderDtos.setId("gender");
        allGenderDtos.setName(genderDtos);
        resultList.add(allGenderDtos);


        List<MaritalStatus> martialStatuses = martialStatusRepository.findAll();
        List<NormalObjectDTO> martialStatusDtos = new ArrayList<>();
        for(MaritalStatus maritalStatus : martialStatuses) {
            NormalObjectDTO martialStatusDto = new NormalObjectDTO();
            martialStatusDto.setId(String.valueOf(maritalStatus.getId()));
            martialStatusDto.setName(maritalStatus.getName());
            martialStatusDtos.add(martialStatusDto);
        }
        NormalObjectDTO allmartialStatusDtos = new NormalObjectDTO();
        allmartialStatusDtos.setId("maritalStatus");
        allmartialStatusDtos.setName(martialStatusDtos);
        resultList.add(allmartialStatusDtos);

        List<CompensationFrequency> compensationFrequencies = compensationFrequencyRepository.findAll();
        List<NormalObjectDTO> compensationFrequenceDtos = new ArrayList<>();
        for(CompensationFrequency compensationFrequency : compensationFrequencies) {
            NormalObjectDTO compensationFrequenceDto = new NormalObjectDTO();
            compensationFrequenceDto.setId(String.valueOf(compensationFrequency.getId()));
            compensationFrequenceDto.setName(compensationFrequency.getName());
            compensationFrequenceDtos.add(compensationFrequenceDto);
        }
        NormalObjectDTO allcompensationFrequenceDtos = new NormalObjectDTO();
        allcompensationFrequenceDtos.setId("compensationUnit");
        allcompensationFrequenceDtos.setName(compensationFrequenceDtos);
        resultList.add(allcompensationFrequenceDtos);

        return resultList;
    }

    @Override
    public User addNewUser(EmployeeInfomationPojo pojo){
        User user = new User();
        UserRole userRole = new UserRole();
        userRole.setId(EMPLOYEE_ROLE_ID);
        user.setUserRole(userRole);
        user.setEmailWork(pojo.getWorkEmail());
        return user;
    }

    @Override
    public void saveUser(User user){
        User userReturned = userRepository.save(user);
    }

    @Override
    public void handlePersonalInformation(EmployeeInfomationPojo pojo, User user){
        String firstName = pojo.getFirstName();
        String lastName = pojo.getLastName();

        UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
        userPersonalInformation.setFirstName(firstName);
        userPersonalInformation.setLastName(lastName);

        if("true".equals(pojo.getSetupOption())){
            handleFullPersonalInformation(pojo, user, userPersonalInformation);
        }

        UserPersonalInformation returnEntity = userPersonalInformationRepository.save(userPersonalInformation);
        user.setUserPersonalInformation(returnEntity);
    }

    @Override
    public void handleFullPersonalInformation(EmployeeInfomationPojo pojo, User user, UserPersonalInformation userPersonalInformation) {
        String socialSecurityNumber = pojo.getSocialSecurityNumber();
        String gender = pojo.getGender();
        String maritalStatus = pojo.getMaritalStatus();
        String dateOfBirth = pojo.getDateOfBirth();
        String street1 = pojo.getStreet1();
        String street2 = pojo.getStreet2();
        String city = pojo.getCity();
        String state = pojo.getState();
        String zip = pojo.getZip();

        userPersonalInformation.setSsn(socialSecurityNumber);
        Gender saveGender = genderRepository.findById(Long.parseLong(gender)).get();
        userPersonalInformation.setGender(saveGender);
        MaritalStatus savemaritalStatus = martialStatusRepository.findById(Long.parseLong(maritalStatus)).get();
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
    public void handleContactInformation(EmployeeInfomationPojo pojo, User user){
        String workEmail = pojo.getWorkEmail();
        String personalEmail = pojo.getPersonalEmail();
        String phoneWork = pojo.getPhoneWork();
        String phonePersonal = pojo.getPhonePersonal();

        UserContactInformation userContactInformation = new UserContactInformation();
        userContactInformation.setEmailWork(workEmail);
        userContactInformation.setEmailWork(personalEmail);
        userContactInformation.setPhoneWork(phoneWork);
        userContactInformation.setPhoneHome(phonePersonal);
        UserContactInformation returnEntity = userContactlInformationRepository.save(userContactInformation);
        user.setUserContactInformation(returnEntity);
    }

    @Override
    public void handleJobInformation(EmployeeInfomationPojo pojo, User user) {
        String jobTitle = pojo.getJobTitle();
        String employmentTypeId = pojo.getEmployeeType();
        String hireDate = pojo.getHireDate();
        String managerId = pojo.getManagerId();
        String departmentId = pojo.getDepartmentId();
        String compensation = pojo.getCompensation();
        String compensationUnit = pojo.getCompensationUnit();
        String officeId = pojo.getOfficeLocation();
        JobUser jobUser = new JobUser();
        if(!"".equals(managerId) && managerId != null){
            User managerUser = userRepository.findByManagerUser(Long.parseLong(managerId));
            user.setManagerUser(managerUser);
        }
        jobUser.setUser(user);
        Job job = new Job();
        job.setTitle(jobTitle);
        Job jobReturned = jobRepository.save(job);
        jobUser.setJob(jobReturned);

        if(!"".equals(departmentId) && departmentId != null) {
            Department department = new Department();
            department.setId(Long.parseLong(departmentId));
            Department departmentReturned = departmentRepository.save(department);
            jobUser.setDepartment(departmentReturned);
        }
        if(!"".equals(employmentTypeId) && employmentTypeId != null) {
            EmploymentType employmentType = new EmploymentType();
            employmentType.setId(Long.parseLong(employmentTypeId));
            EmploymentType employmentTypeReturned = employmentTypeRepository.save(employmentType);
            jobUser.setEmploymentType(employmentTypeReturned);

        }
        if(!"".equals(officeId) && officeId != null) {
            Office office = officeRepository.findById(Long.parseLong(officeId)).get();
            jobUser.setOffice(office);
        }

        jobUser.setStartDate(FieldCheckUtil.getTimestampFromString(hireDate));
        jobUserRepository.save(jobUser);
        UserCompensation userCompensation = new UserCompensation();
        userCompensation.setWage("".equals(compensation) ? 0 : Integer.valueOf(compensation));
        userCompensation.setUser(user);
        if(!"".equals(compensationUnit) && compensationUnit != null) {
            CompensationFrequency compensationFrequency = compensationFrequencyRepository.findById(Long.parseLong(compensationUnit)).get();
            userCompensation.setCompensationFrequency(compensationFrequency);
        }
        userCompensationRepository.save(userCompensation);

    }

    @Override
    public void handelEmergencyContacts(EmployeeInfomationPojo employeePojo, User user) {
        if(employeePojo.getSetupOption().equals("false")){
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
            contactEntity.setIsPrimary((byte) (isPrimary ? 1 : 0));
            contactEntity.setRelationships(contactPojo.getRelationship());
            contactEntity.setUser(user);
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
            Office office = new Office();
            OfficeAddress officeAddress = new OfficeAddress();
            officeAddress.setCity(officePojo.getCity());
            if(!"".equals(officePojo.getState()) && null != officePojo.getState()){
                StateProvince state = stateProvinceRepository.findById(Long.parseLong(officePojo.getState())).get();
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
