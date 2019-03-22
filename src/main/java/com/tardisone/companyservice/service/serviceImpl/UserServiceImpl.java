package com.tardisone.companyservice.service.serviceImpl;

import com.tardisone.companyservice.dto.NormalObjectDto;
import com.tardisone.companyservice.model.*;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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
    private JobUserReposiory jobUserReposiory;

    @Autowired DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;



    @Override
    public List<NormalObjectDto> getJobInformation() {
        List<NormalObjectDto> resultList = new ArrayList<NormalObjectDto>();

        List<Job> jobList = jobRepository.findAll();
        List<NormalObjectDto> allJobs = new ArrayList<NormalObjectDto>();
        for (Job job : jobList) {
            NormalObjectDto jobDto = new NormalObjectDto();
            String key = String.valueOf(job.getId());
            String value = job.getTitle();
            jobDto.setFid(key);
            jobDto.setValue(value);
            allJobs.add(jobDto);
        }
        NormalObjectDto allJobDtos = new NormalObjectDto();
        allJobDtos.setFid("jobTitle");
        allJobDtos.setValue(allJobs);
        resultList.add(allJobDtos);

        List<Department> departmentList = departmentRepository.findAll();
        List<NormalObjectDto> allDepartments = new ArrayList<NormalObjectDto>();
        for(Department department : departmentList) {
            NormalObjectDto departmentDto = new NormalObjectDto();
            String key = String.valueOf(department.getId());
            String value = department.getName();
            departmentDto.setFid(key);
            departmentDto.setValue(value);
            allDepartments.add(departmentDto);
        }
        NormalObjectDto allDepartmentDtos = new NormalObjectDto();
        allDepartmentDtos.setFid("department");
        allDepartmentDtos.setValue(allDepartments);
        resultList.add(allDepartmentDtos);


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
        user.setUserPersonalInformationId(returnEntity.getId());
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
        user.setUserContactInformationId(returnEntity.getId());
    }

    @Override
    public void handleJobInformation(EmployeeInfomationPojo pojo, User user) {
        String jobTitleId = pojo.getJobTitleId();
        String employeeType = pojo.getEmployeeType();
        String hireDate = pojo.getHireDate();
        String managerId = pojo.getManagerId();
        String departmentId = pojo.getDepartmentId();
        String compensation = pojo.getCompensation();
        String compensationUnit = pojo.getCompensationUnit();
        String officeLocation = pojo.getOfficeLocation();

        JobUser jobUser = new JobUser();
        if (jobTitleId != null && !jobTitleId.equals("")) {
            jobUser.setJobId(Integer.parseInt(jobTitleId));
        }
        /*jobUser.setStartDate(hireDate);
        if (managerId != null && !managerId.equals("")) {
            jobUser.setManagerId(Integer.parseInt(managerId));
        }*/

        if (departmentId != null && !departmentId.equals("")) {
            jobUser.setDepartmentId(Integer.parseInt(departmentId));
        }
        jobUser.setDepartmentId(Integer.parseInt(departmentId));
        if (officeLocation != null && !officeLocation.equals("")) {
            jobUser.setOfficeId(Integer.parseInt(officeLocation));
        }
        jobUser.setUserId(user.getId());

        JobUser returnEntity = jobUserReposiory.save(jobUser);
    }

}
