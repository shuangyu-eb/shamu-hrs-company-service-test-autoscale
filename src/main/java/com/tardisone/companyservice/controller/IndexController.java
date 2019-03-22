package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.dto.NormalObjectDto;
import com.tardisone.companyservice.model.User;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.util.FieldCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tardisone.companyservice.util.Constants;

@RestController
public class IndexController {

    @Autowired
    UserService userService;

    @GetMapping(value = {"/index", "/"})
    public String index() {
        return "hello world";
    }

    @GetMapping(value = {"/company/getJobInformation"})
    @ResponseBody
    @Transactional
    public List<NormalObjectDto>  getJobInformation(){
        return userService.getJobInformation();
    }

    @PostMapping(value = "/company/handleUserInformation")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public boolean handleUserInformation(HttpServletRequest request){
        EmployeeInfomationPojo employeePojo = new EmployeeInfomationPojo(request);

        boolean checkFlag = false;
        if(checkAllFields(request)){
            try{
                User user = userService.addNewUser(employeePojo);
                userService.handlePersonalInformation(employeePojo, user);
                userService.handleContactInformation(employeePojo, user);
                userService.handleJobInformation(employeePojo, user);
                checkFlag = true;
            }catch (Exception e){
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return checkFlag;
    }

    private boolean checkAllFields(HttpServletRequest request){
        List<Map<String,String>> checkList = new ArrayList<Map<String,String>>();

        Map<String, String> firstNameMap = FieldCheckUtil.getFieldValueMap(request, Constants.FIRST_NAME, Constants.PERSONNAME_REGEX);
        Map<String, String> lastNameMap = FieldCheckUtil.getFieldValueMap(request, Constants.FIRST_NAME, Constants.PERSONNAME_REGEX);
        Map<String, String> workEmailMap = FieldCheckUtil.getFieldValueMap(request, Constants.WORK_EMAIL, Constants.EMAIL_REGEX);
        Map<String, String> personalEmailMap = FieldCheckUtil.getFieldValueMap(request, Constants.PERSONAL_EMAIL, Constants.EMAIL_REGEX);
        Map<String, String> phoneWorkMap = FieldCheckUtil.getFieldValueMap(request, Constants.WORK_PHONE, Constants.PHONE_NUMBER_REGEX);
        Map<String, String> phonePersonalMap = FieldCheckUtil.getFieldValueMap(request, Constants.HOME_PHONE, Constants.PHONE_NUMBER_REGEX);

        Map<String, String> jobTitleIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.JOB_TITLE, Constants.INTEGER_REGEX);
        Map<String, String> employeeTypeMap = FieldCheckUtil.getFieldValueMap(request, Constants.EMPLOYEE_TYPE, Constants.INTEGER_REGEX);
        Map<String, String> hireDateMap = FieldCheckUtil.getFieldValueMap(request, Constants.HIRE_DATE, Constants.PHONE_NUMBER_REGEX);
        Map<String, String> managerIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.REPORTS_TO, Constants.PHONE_NUMBER_REGEX);
        Map<String, String> departmentIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.DEPARTMENT, Constants.INTEGER_REGEX);
        Map<String, String> compensationMap = FieldCheckUtil.getFieldValueMap(request, Constants.COMPENSATION, Constants.LONG_INTEGER_REGEX);
        Map<String, String> compensationUnitMap = FieldCheckUtil.getFieldValueMap(request, Constants.COMPENSATION_UNIT, Constants.NORMAL_REGEX);
        Map<String, String> officeLocationMap = FieldCheckUtil.getFieldValueMap(request, Constants.OFFICE_LOCATION, Constants.NORMAL_REGEX);

        checkList.add(firstNameMap);
        checkList.add(lastNameMap);
        checkList.add(workEmailMap);
        checkList.add(personalEmailMap);
        checkList.add(phoneWorkMap);
        checkList.add(phonePersonalMap);

        checkList.add(jobTitleIdMap);
        checkList.add(employeeTypeMap);
        checkList.add(phonePersonalMap);
        checkList.add(hireDateMap);
        checkList.add(managerIdMap);
        checkList.add(departmentIdMap);
        checkList.add(compensationMap);
        checkList.add(compensationUnitMap);
        checkList.add(officeLocationMap);

        return FieldCheckUtil.checkAllField(checkList);
    }


}
