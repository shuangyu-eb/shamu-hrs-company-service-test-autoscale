package com.tardisone.companyservice.service.serviceImpl;

import com.tardisone.companyservice.exception.CheckFaildException;
import com.tardisone.companyservice.model.*;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.util.Constants;
import com.tardisone.companyservice.util.FieldCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public User addNewUser(HttpServletRequest request){
        User user = new User();
        user.setEmailWork(request.getParameter(Constants.WORK_EMAIL));
        return user;

    }

    @Override
    public void handlePersonalInformation(HttpServletRequest request, User user){
        String firstName = request.getParameter(Constants.FIRST_NAME);
        String lastName = request.getParameter(Constants.LAST_NAME);

        UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
        userPersonalInformation.setFirstName(firstName);
        userPersonalInformation.setLastName(lastName);
        UserPersonalInformation returnEntity = userPersonalInformationRepository.save(userPersonalInformation);
        user.setUserPersonInformationId(returnEntity.getId());
    }

    @Override
    public void handleContactInformation(HttpServletRequest request, User user){
        String workEmail = request.getParameter(Constants.WORK_EMAIL);
        String personalEmail = request.getParameter(Constants.PERSONAL_EMAIL);
        String phoneWork = request.getParameter(Constants.WORK_PHONE);
        String phonePersonal = request.getParameter(Constants.HOME_PHONE);

        UserContactInformation userContactInformation = new UserContactInformation();
        userContactInformation.setEmailWork(workEmail);
        userContactInformation.setEmailWork(personalEmail);
        userContactInformation.setPhoneWork(phoneWork);
        userContactInformation.setPhoneHome(phonePersonal);
        UserContactInformation returnEntity = userContactlInformationRepository.save(userContactInformation);
        user.setUserContactInformationId(returnEntity.getId());
    }

    @Override
    public void handleJobInformation(HttpServletRequest request, User user) {
        String jobTitleId = request.getParameter(Constants.JOB_TITLE);
        String employeeType = request.getParameter(Constants.EMPLOYEE_TYPE);
        String hireDate = request.getParameter(Constants.HIRE_DATE);
        String managerId = request.getParameter(Constants.REPORTS_TO);
        String departmentId = request.getParameter(Constants.DEPARTMENT);
        String compensation = request.getParameter(Constants.COMPENSATION);
        String compensationUnit = request.getParameter(Constants.COMPENSATION_UNIT);
        String officeLocation = request.getParameter(Constants.OFFICE_LOCATION);

        JobUser jobUser = new JobUser();
        jobUser.setJobId(Integer.parseInt(jobTitleId));
        jobUser.setStartDate(hireDate);
        jobUser.setManagerId(Integer.parseInt(managerId));
        jobUser.setDepartmentId(Integer.parseInt(departmentId));
        jobUser.setOfficeId(Integer.parseInt(officeLocation));
        jobUser.setUserId(user.getId());

        JobUser returnEntity = jobUserReposiory.save(jobUser);
    }

}
