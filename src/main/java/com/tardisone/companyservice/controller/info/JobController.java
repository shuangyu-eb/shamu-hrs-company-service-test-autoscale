package com.tardisone.companyservice.controller.info;

import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.pojo.JobInfomationPojo;
import com.tardisone.companyservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company")
public class JobController {


    @Autowired
    JobUserService jobUserService;

    @Autowired
    JobService jobService;

    @Autowired
    UserService userService;

    @Autowired
    EmploymentTypeService employmentTypeService;

    @Autowired
    DepartmentService departmentService;


    @GetMapping(value = {"info/job/{userId}"})
    public JobInfomationPojo getJobInfo(@PathVariable("userId") Long userId) {
        JobUser jobUser=jobUserService.findJobUserByUserId(userId);
        Job job=jobService.findJobById(jobUser.getJobId());
        User user=userService.findById(jobUser.getUserId());
        String jobTitle=job.getTitle();
        String employmentType=jobUser.getEmploymentType().getName();
        Date hireDate=jobUser.getStartDate();
        UserPersonalInformation information=jobUser.getManagerId().getUserPersonalInformation();
        String manager=information.getFirstName()+" "+information.getLastName();
        String department=jobUser.getDepartment().getName();
        String compensation="show";
        Office office =jobUser.getOffice();
        OfficeAddresses officeAddresses=office.getOfficeAddress();
        JobInfomationPojo jobInfomationPojo=new JobInfomationPojo( jobTitle,  employmentType,  hireDate,  manager,  department,  compensation,  office.getName(),officeAddresses);
        return jobInfomationPojo;
    }

    @GetMapping(value = {"info/editJobInfoBefore/{userId}"})
    public Map editJobInfoBefore(@PathVariable("userId") Long userId) {
        JobUser jobUser=jobUserService.findJobUserByUserId(userId);
        Job job=jobService.findJobById(jobUser.getJobId());
        User user=userService.findById(jobUser.getUserId());
        Map map=new HashMap();
        map.put("jobTitle",job.getTitle());
        map.put("employmentType",jobUser.getEmploymentType().getId());
        map.put("hireDate",jobUser.getStartDate());
        map.put("manager",jobUser.getManagerId().getId());
        map.put("department",jobUser.getDepartment().getId());
        map.put("compensation",user.getUserCompensation().getAmount());
        map.put("frequency",user.getUserCompensation().getFrequency());
        map.put("location",jobUser.getOffice().getOfficeAddress().getId());
        return map;
    }

    @GetMapping(value = {"info/getAllEmploymentType"})
    public List<EmploymentType> getAllEmploymentType() {
        return employmentTypeService.getAllEmploymentType();
    }

    @GetMapping(value = {"info/getAllManager"})
    public List<Map> getAllManager() {
        return userService.getAllManager();
    }

    @GetMapping(value = {"info/getAllDepartment/{userId}"})
    public List getAllDepartment(@PathVariable("userId") Long userId) {
        JobUser jobUser=jobUserService.findJobUserByUserId(userId);
        return departmentService.getAllDepartments(jobUser.getCompany().getId());
    }

    @GetMapping(value = {"info/getAllLocations/{userId}"})
    public List getAllLocations(@PathVariable("userId") Long userId) {
        JobUser jobUser=jobUserService.findJobUserByUserId(userId);
        return jobService.getAllByCompanyId(jobUser.getCompany().getId());
    }



}
