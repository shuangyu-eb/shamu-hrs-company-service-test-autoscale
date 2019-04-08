package com.tardisone.companyservice.pojo;

import com.tardisone.companyservice.utils.Constants;

import javax.servlet.http.HttpServletRequest;

public class EmployeeInfomationPojo {

    private String firstName;

    private String lastName;

    private String workEmail;

    private String personalEmail;

    private String phoneWork;

    private String phonePersonal;

    private String jobTitleId;

    private String employeeType;

    private String hireDate;

    private String managerId;

    private String departmentId;

    private String compensation;

    private String compensationUnit;

    private String officeLocation;

    public EmployeeInfomationPojo(HttpServletRequest request){
        setFirstName(request.getParameter(Constants.FIRST_NAME));
        setLastName(request.getParameter(Constants.LAST_NAME));
        setWorkEmail(request.getParameter(Constants.WORK_EMAIL));
        setPersonalEmail(request.getParameter(Constants.WORK_EMAIL));
        setPhoneWork(request.getParameter(Constants.PERSONAL_EMAIL));
        setPhonePersonal(request.getParameter(Constants.HOME_PHONE));
        setJobTitleId(request.getParameter(Constants.JOB_TITLE));
        setEmployeeType(request.getParameter(Constants.EMPLOYEE_TYPE));
        setHireDate(request.getParameter(Constants.HIRE_DATE));
        setManagerId(request.getParameter(Constants.REPORTS_TO));
        setDepartmentId(request.getParameter(Constants.DEPARTMENT));
        setCompensation(request.getParameter(Constants.COMPENSATION));
        setCompensationUnit(request.getParameter(Constants.COMPENSATION_UNIT));
        setOfficeLocation(request.getParameter(Constants.OFFICE_LOCATION));
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getPhonePersonal() {
        return phonePersonal;
    }

    public void setPhonePersonal(String phonePersonal) {
        this.phonePersonal = phonePersonal;
    }

    public String getJobTitleId() {
        return jobTitleId;
    }

    public void setJobTitleId(String jobTitleId) {
        this.jobTitleId = jobTitleId;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getCompensation() {
        return compensation;
    }

    public void setCompensation(String compensation) {
        this.compensation = compensation;
    }

    public String getCompensationUnit() {
        return compensationUnit;
    }

    public void setCompensationUnit(String compensationUnit) {
        this.compensationUnit = compensationUnit;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }
}
