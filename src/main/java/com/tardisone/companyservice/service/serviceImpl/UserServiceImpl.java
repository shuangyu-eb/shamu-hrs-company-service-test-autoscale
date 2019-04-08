/*
package com.tardisone.companyservice.service.serviceImpl;

import com.tardisone.companyservice.dto.JobDTO;
import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.dto.NormalObjectDTO;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserPersonalInformationRepository userPersonalInformationRepository;

    @Autowired
    private UserContactlInformationRepository userContactlInformationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobUserRepository jobUserRepository;

    @Autowired DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmploymentTypeRepository employmentTypeRepository;


    @Override
    public User findUserByEmail(String email) {
        return null;
    }

    @Override
    public Boolean sendVerifyEmail(String email) {
        return null;
    }

    @Override
    public Boolean finishUserVerification(String activationToken) {
        return null;
    }

    @Override
    public List<JobUserDTO> findAllEmployees() {
        return null;
    }

    @Override
    public List<NormalObjectDTO> getJobInformation() {
        List<NormalObjectDTO> resultList = new ArrayList<>();

        */
/*List<Job> jobList = jobRepository.findAll();
        List<JobDTO> allJobs = new ArrayList<>();
        for (Job job : jobList) {
            JobDTO jobDto = new JobDTO();
            String id = String.valueOf(job.getId());
            String title = job.getTitle();
            jobDto.setId(id);
            jobDto.setTitle(title);
            allJobs.add(jobDto);
        }
        NormalObjectDTO allJobDtos = new NormalObjectDTO();
        allJobDtos.setId("jobTitle");
        allJobDtos.setName(allJobs);
        resultList.add(allJobDtos);*//*


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
        allEmploymentTypeDtos.setId("reportsTo");
        allEmploymentTypeDtos.setName(allEmploymentTypes);
        resultList.add(allEmploymentTypeDtos);

        return resultList;
    }

    @Override
    public User addNewUser(EmployeeInfomationPojo pojo){
        User user = new User();
        user.setEmailWork(pojo.getWorkEmail());
        return user;

    }

    @Override
    public void handlePersonalInformation(EmployeeInfomationPojo pojo, User user){
        String firstName = pojo.getFirstName();
        String lastName = pojo.getLastName();

        UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
        userPersonalInformation.setFirstName(firstName);
        userPersonalInformation.setLastName(lastName);
        UserPersonalInformation returnEntity = userPersonalInformationRepository.save(userPersonalInformation);
        user.setUserPersonalInformation(returnEntity);
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
        String jobTitleId = pojo.getJobTitleId();
        String employmentTypeId = pojo.getEmployeeType();
        String hireDate = pojo.getHireDate();
        String managerId = pojo.getManagerId();
        String departmentId = pojo.getDepartmentId();
        String compensation = pojo.getCompensation();
        String compensationUnit = pojo.getCompensationUnit();
        String officeId = pojo.getOfficeLocation();

        JobUser jobUser = new JobUser();
        if (jobTitleId != null && !jobTitleId.equals("")) {
            Job job = new Job();
            job.setId(Long.parseLong(jobTitleId));
            jobUser.setJob(job);
            jobUser.setUser(user);
            jobUser.setStartDate(Timestamp.valueOf(hireDate));
            Department department = new Department();
            department.setId(Long.parseLong(departmentId));
            jobUser.setDepartment(department);
            EmploymentType employmentType = new EmploymentType();
            employmentType.setId(Long.parseLong(employmentTypeId));
            jobUser.setEmploymentType(employmentType);
            Office office = new Office();
            office.setOfficeId(officeId);
            jobUser.setOffice(office);
        }





        JobUser returnEntity = jobUserRepository.save(jobUser);
    }

}
*/
