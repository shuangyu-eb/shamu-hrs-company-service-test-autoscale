package com.tardisone.companyservice.controller;


import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.service.EmployeeeInformationService;
import com.tardisone.companyservice.service.JobUserService;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company")
public class EmployeeController {


    @Autowired
    UserService userService;

    @Autowired
    EmployeeeInformationService employeeService;

    @Autowired
    JobUserService jobUserService;

    @GetMapping(value = {"/test"})
    public String test(){
        return "success";
    }

    @GetMapping("userEmployeesInfo/findByEmail/{email}")
    public User findEmployeeInfomationByEmail(@PathVariable String email){
        return userService.findUserByEmail(email);
    }

//    @GetMapping("userEmployeesInfo/findManagerById/{id}")
//    public User findManagerByEmployeeNumber(@PathVariable String id){
//        return employeeService.findManagerByEmployeeNumber(id);
//    }

    @GetMapping("userEmployeesInfo/findEmployeeInfoById/{id}")
    public @ResponseBody Map findInfoByEmployeeNumber(@PathVariable String id){
        User employee = employeeService.findEmployeeInfoByEmployeeNumber(id);
        Map result = new HashMap();
        result.put("EmployeeNumber",employee.getEmployeeNumber());
        JobUser employeeJob = jobUserService.findJobUserByUser(employee);
        Job employeeJobInfo = employeeJob.getJob();
        result.put("PersonalInformation",employee.getUserPersonalInformation());
        result.put("UserContactInformation",employee.getUserContactInformation());
        result.put("employeeJobTitle",employeeJobInfo.getTitle());

        JobUser managerJob = jobUserService.findJobUserByUser(employee.getManagerUser());
        Job managerJobInfo = managerJob.getJob();
        result.put("managerName",employee.getManagerUser().getUserPersonalInformation().getFirstName());
        result.put("managerJobTitle",managerJobInfo.getTitle());


        List<User> directReports = employeeService.findEmployeesByManagerId(Long.valueOf(employee.getEmployeeNumber()));
        List<Map> reporters = new LinkedList<>();
        for (int i = 0 ; i < directReports.size() ; i++){
            Map reporterInfo = new HashMap();
            JobUser reporterJob = jobUserService.findJobUserByUser(directReports.get(i));
            Job reporterJobInfo = reporterJob.getJob();
            reporterInfo.put("reportName",directReports.get(i).getUserPersonalInformation().getFirstName());
            reporterInfo.put("reportJob",reporterJobInfo.getTitle());
            reporters.add(reporterInfo);
        }
        result.put("directReporters",reporters);
        return result;
    }



}