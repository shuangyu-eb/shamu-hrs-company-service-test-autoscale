package com.tardisone.companyservice.pojo;

import com.tardisone.companyservice.utils.Constants;
import lombok.Data;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Data
public class EmployeeInfomationPojo {

    private String setupOption;

    private String firstName;

    private String middleName;

    private String preferredName;

    private String socialSecurityNumber;

    private String gender;

    private String maritalStatus;

    private String dateOfBirth;

    private String street1;

    private String street2;

    private String city;

    private String state;

    private String zip;

    private String lastName;

    private String workEmail;

    private String personalEmail;

    private String phoneWork;

    private String phonePersonal;

    private String jobTitle;

    private String employeeType;

    private String hireDate;

    private String managerId;

    private String departmentId;

    private String compensation;

    private String compensationUnit;

    private String officeLocation;

    private String uploadPhoto;

    private List<EmergencyContactPojo> emergencyContactPojoList;

    public EmployeeInfomationPojo(HttpServletRequest request){
        setSetupOption(request.getParameter(Constants.SETUP_OPTION));
        if(getSetupOption().equals("true")){
            setMiddleName(request.getParameter(Constants.MIDDLE_NAME));
            setPreferredName(request.getParameter(Constants.PREFERRED_NAME));
            setSocialSecurityNumber(request.getParameter(Constants.SOCIAL_SECURITY_NUMBER));
            setGender(request.getParameter(Constants.GENDER));
            setMaritalStatus(request.getParameter(Constants.MARITAL_STATUS));
            setDateOfBirth(request.getParameter(Constants.DATE_OF_BIRTH));
            setStreet1(request.getParameter(Constants.STREET1));
            setStreet1(request.getParameter(Constants.STREET2));
            setCity(request.getParameter(Constants.CITY));
            setState(request.getParameter(Constants.STATE));
            setZip(request.getParameter(Constants.ZIP));

            String emergencyContactsJSON = request.getParameter(Constants.EMERGENCY_CONTACTS);
            setEmergencyContactPojoList(EmergencyContactPojo.getEmergencyContactPojoList(emergencyContactsJSON));
        }
        setFirstName(request.getParameter(Constants.FIRST_NAME));
        setLastName(request.getParameter(Constants.LAST_NAME));
        setWorkEmail(request.getParameter(Constants.WORK_EMAIL));
        setPersonalEmail(request.getParameter(Constants.WORK_EMAIL));
        setPhoneWork(request.getParameter(Constants.PERSONAL_EMAIL));
        setPhonePersonal(request.getParameter(Constants.HOME_PHONE));
        setJobTitle(request.getParameter(Constants.JOB_TITLE));
        setEmployeeType(request.getParameter(Constants.EMPLOYEE_TYPE));
        setHireDate(request.getParameter(Constants.HIRE_DATE));
        setManagerId(request.getParameter(Constants.REPORTS_TO));
        setDepartmentId(request.getParameter(Constants.DEPARTMENT));
        setCompensation(request.getParameter(Constants.COMPENSATION));
        setCompensationUnit(request.getParameter(Constants.COMPENSATION_UNIT));
        setOfficeLocation(request.getParameter(Constants.OFFICE_LOCATION));


    }

}
