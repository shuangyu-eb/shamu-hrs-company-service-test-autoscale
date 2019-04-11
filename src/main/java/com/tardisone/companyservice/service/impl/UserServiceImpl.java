package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.dto.NormalObjectDTO;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.pojo.EmergencyContactPojo;
import com.tardisone.companyservice.pojo.OfficePojo;
import com.tardisone.companyservice.repository.JobUserRepository;
import com.tardisone.companyservice.repository.UserAddressRepository;
import com.tardisone.companyservice.repository.UserRepository;
import com.tardisone.companyservice.exception.EmailException;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.utils.EmailUtil;
import com.tardisone.companyservice.utils.FieldCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private UserPersonalInformationRepository userPersonalInformationRepository;

    @Autowired
    private UserContactlInformationRepository userContactlInformationRepository;

    @Autowired DepartmentRepository departmentRepository;

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

    @Value("${application.systemEmailAddress}")
    String systemEmailAddress;

    @Value("${application.frontEndAddress}")
    String frontEndAddress;

    @Autowired
    EmailUtil emailUtil;

    private static Long employeeUserRoleId = 3L;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailWork(email);
    }

    @Override
    public Boolean sendVerifyEmail(String email) {
        User user = userRepository.findByEmailWork(email);
        if (user == null) {
            return false;
        }

        String accountVerifyToken = UUID.randomUUID().toString();
        String emailContent = getActivationEmail(accountVerifyToken);
        Boolean emailResult = emailUtil.send(systemEmailAddress, email, "Please activate your account!", emailContent);
        if (!emailResult) {
            throw new EmailException("Error when sending out verify email!");
        }

        user.setVerificationToken(accountVerifyToken);
        userRepository.save(user);
        return true;
    }

    @Override
    public Boolean finishUserVerification(String activationToken) {
        User user = userRepository.findByVerificationToken(activationToken);
        if (user == null || user.getVerifiedAt() != null) {
            return false;
        }
        user.setVerifiedAt(new Timestamp(new Date().getTime()));
        userRepository.save(user);
        return true;
    }

    @Override
    public List<JobUserDTO> findAllEmployees() {
        List<User> employees = userRepository.findAllEmployees();
        List<UserAddress> userAddresses = userAddressRepository.findAllByUserIn(employees);
        List<JobUser> jobUserList = jobUserRepository.findAllByUserIn(employees);

        return getJobUserDTOList(employees, userAddresses, jobUserList);
    }

    @Override
    public Boolean existsByEmailWork(String email) {
        return null;
    }

    private List<JobUserDTO> getJobUserDTOList(List<User> employees, List<UserAddress> userAddresses, List<JobUser> jobUsers) {
        return employees.stream().map((employee) -> {
            JobUserDTO jobUserDTO = new JobUserDTO();
            jobUserDTO.setEmail(employee.getEmailWork());
            jobUserDTO.setImageUrl(employee.getImageUrl());
            jobUserDTO.setId(employee.getId());

            UserPersonalInformation userPersonalInformation = employee.getUserPersonalInformation();
            if (userPersonalInformation != null) {
                jobUserDTO.setFirstName(userPersonalInformation.getFirstName());
                jobUserDTO.setLastName(userPersonalInformation.getLastName());
            }

            userAddresses.forEach((userAddress -> {
                User userWithAddress = userAddress.getUser();
                if (userWithAddress != null
                        && userWithAddress.getId().equals(employee.getId())
                        && userAddress.getCity() != null) {
                    jobUserDTO.setCityName(userAddress.getCity());
                }
            }));

            jobUsers.forEach((jobUser -> {
                User userWithJob = jobUser.getUser();
                if (userWithJob != null
                        && userWithJob.getId().equals(employee.getId())
                        && jobUser.getJob() != null) {
                    jobUserDTO.setJobTitle(jobUser.getJob().getTitle());
                }
            }));
            return jobUserDTO;
        }).collect(Collectors.toList());
    }

    public String getActivationEmail(String accountVerifyToken) {
        Context context = new Context();
        context.setVariable("frontEndAddress", frontEndAddress);
        context.setVariable("accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
        return templateEngine.process("account_verify_email.html", context);
    }

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
        userRole.setId(employeeUserRoleId);
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

        User managerUser = userRepository.findByManagerUser(Long.parseLong(managerId));
        user.setManagerUser(managerUser);

        jobUser.setUser(user);
        Job job = new Job();
        job.setTitle(jobTitle);
        Job jobReturned = jobRepository.save(job);
        jobUser.setJob(jobReturned);

        Department department = new Department();
        department.setId(Long.parseLong(departmentId));
        Department departmentReturned = departmentRepository.save(department);
        jobUser.setDepartment(departmentReturned);

        EmploymentType employmentType = new EmploymentType();
        employmentType.setId(Long.parseLong(employmentTypeId));
        EmploymentType employmentTypeReturned = employmentTypeRepository.save(employmentType);
        jobUser.setEmploymentType(employmentTypeReturned);

        Office office = officeRepository.findById(Long.parseLong(officeId)).get();
        jobUser.setOffice(office);

        jobUser.setStartDate(FieldCheckUtil.getTimestampFromString(hireDate));

        JobUser returnEntity = jobUserRepository.save(jobUser);

        UserCompensation userCompensation = new UserCompensation();
        userCompensation.setWage(Integer.valueOf(compensation));
        userCompensation.setUser(user);
        CompensationFrequency compensationFrequency = compensationFrequencyRepository.findById(Long.parseLong(compensationUnit)).get();
        userCompensation.setCompensationFrequency(compensationFrequency);
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

            StateProvince state = stateProvinceRepository.findById(Long.parseLong(officePojo.getState())).get();
            officeAddress.setStateProvince(state);
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
