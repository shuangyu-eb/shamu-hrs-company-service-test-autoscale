package com.tardisone.companyservice.controller.info;

import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.pojo.JobInfoEditPojo;
import com.tardisone.companyservice.pojo.JobInfomationPojo;
import com.tardisone.companyservice.pojo.OfficeAddressPojo;
import com.tardisone.companyservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

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
        User u=new User();
        u.setId(userId);
        JobUser jobUser= jobUserService.findJobUserByUser(u);
        Job job=jobUser.getJob();
        User user=jobUser.getUser();
        String jobTitle=job.getTitle();
        String employmentType=jobUser.getEmploymentType().getName();
        Date hireDate=jobUser.getStartDate();
        UserPersonalInformation information=user.getManagerUser().getUserPersonalInformation();
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
        User u=new User();
        u.setId(userId);
        JobUser jobUser=jobUserService.findJobUserByUser(u);
        Job job=jobUser.getJob();
        User user=jobUser.getUser();
        Map jobInfo=new HashMap();
        jobInfo.put("jobTitle",job.getTitle());
        jobInfo.put("employmentType",jobUser.getEmploymentType().getId());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM/dd/yyyy");
        String hireDate=simpleDateFormat.format(jobUser.getStartDate());
        jobInfo.put("hireDate",hireDate);
        jobInfo.put("manager",user.getManagerUser().getId());
        jobInfo.put("department",jobUser.getDepartment().getId());
        jobInfo.put("compensation",user.getUserCompensation().getWage());
        jobInfo.put("frequency",user.getUserCompensation().getCompensation_frequency_id());
        jobInfo.put("location",jobUser.getOffice().getOfficeAddress().getId());
        List<EmploymentType> employmentTypes=employmentTypeService.getAllEmploymentType();
        List<Map> managers= userService.getAllManager();
        List<Map> newManagers=new LinkedList<>();
        for (int i=0;i<managers.size();i++){
            Map manager=new HashMap<String,String>();
            manager.put("id",managers.get(i).get("id"));
            manager.put("name",managers.get(i).get("first_name")+" "+managers.get(i).get("last_name"));
            newManagers.add(manager);
        }
        List<Department> departments  =departmentService.getAllDepartments(jobUser.getCompany().getId());
        List locations  =jobService.getAllByCompanyId(jobUser.getCompany().getId());
        Map result=new HashMap();
        result.put("jobInfo",jobInfo);
        result.put("employmentTypes",employmentTypes);
        result.put("managers",newManagers);
        result.put("departments",departments);
        result.put("locations",locations);
        return result;
    }
    @PostMapping(value = {"info/editJobInfo"})
    public HttpEntity editJobInfo(JobInfoEditPojo jobInfoEditPojo) {
        Long userId=jobInfoEditPojo.getUserId();
        User u=new User();
        u.setId(userId);
        JobUser jobUser=jobUserService.findJobUserByUser(u);
        jobService.saveCompensatios(jobInfoEditPojo.getCompensation(),jobInfoEditPojo.getFrequency(),userId);
        jobService.saveJob(jobInfoEditPojo.getJobTitle(),userId);
        EmploymentType employmentType=jobService.findEmployTypeById(jobInfoEditPojo.getEmploymentType());
        jobService.saveOffice(jobInfoEditPojo.getLocation(),jobUser.getOffice().getId());
        jobService.saveUserManager(jobInfoEditPojo.getManager(),userId);
        SimpleDateFormat sf=new SimpleDateFormat("MM/dd/yyyy");
        Date startDate=sf.parse(jobInfoEditPojo.getHireDate().trim(),new ParsePosition(0));
        jobService.saveJobUser(jobInfoEditPojo.getEmploymentType(),startDate,userId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = {"info/addEmploymentType"})
    public HttpEntity addEmploymentType(String name) {
        jobService.saveEmploymentType(name);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = {"info/addDepartment"})
    public HttpEntity addDepartment(String name,Long companyId) {
        jobService.saveDepartment(name,companyId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = {"info/addOfficeAddress"})
    public HttpEntity addOfficeAddress(OfficeAddressPojo addressPojo) {
        Long userId=addressPojo.getUserId();
        System.out.println("userId:"+userId);
        User u=new User();
        u.setId(userId);
        JobUser jobUser=jobUserService.findJobUserByUser(u);
        jobService.saveOfficeAddress(addressPojo,jobUser.getCompany().getId());
        return new ResponseEntity(HttpStatus.OK);
    }


}
