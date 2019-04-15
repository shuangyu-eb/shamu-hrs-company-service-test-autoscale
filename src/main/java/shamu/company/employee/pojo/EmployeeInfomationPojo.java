package shamu.company.employee.pojo;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import shamu.company.utils.JobUserConstants;

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

  public EmployeeInfomationPojo(HttpServletRequest request) {
    setSetupOption(request.getParameter(JobUserConstants.SETUP_OPTION));
    if (getSetupOption().equals("true")) {
      setMiddleName(request.getParameter(JobUserConstants.MIDDLE_NAME));
      setPreferredName(request.getParameter(JobUserConstants.PREFERRED_NAME));
      setSocialSecurityNumber(request.getParameter(JobUserConstants.SOCIAL_SECURITY_NUMBER));
      setGender(request.getParameter(JobUserConstants.GENDER));
      setMaritalStatus(request.getParameter(JobUserConstants.MARITAL_STATUS));
      setDateOfBirth(request.getParameter(JobUserConstants.DATE_OF_BIRTH));
      setStreet1(request.getParameter(JobUserConstants.STREET1));
      setStreet1(request.getParameter(JobUserConstants.STREET2));
      setCity(request.getParameter(JobUserConstants.CITY));
      setState(request.getParameter(JobUserConstants.STATE));
      setZip(request.getParameter(JobUserConstants.ZIP));

      String emergencyContactsJson = request.getParameter(JobUserConstants.EMERGENCY_CONTACTS);
      setEmergencyContactPojoList(
          EmergencyContactPojo.getEmergencyContactPojoList(emergencyContactsJson));
    }
    setFirstName(request.getParameter(JobUserConstants.FIRST_NAME));
    setLastName(request.getParameter(JobUserConstants.LAST_NAME));
    setWorkEmail(request.getParameter(JobUserConstants.WORK_EMAIL));
    setPersonalEmail(request.getParameter(JobUserConstants.WORK_EMAIL));
    setPhoneWork(request.getParameter(JobUserConstants.PERSONAL_EMAIL));
    setPhonePersonal(request.getParameter(JobUserConstants.HOME_PHONE));
    setJobTitle(request.getParameter(JobUserConstants.JOB_TITLE));
    setEmployeeType(request.getParameter(JobUserConstants.EMPLOYEE_TYPE));
    setHireDate(request.getParameter(JobUserConstants.HIRE_DATE));
    setManagerId(request.getParameter(JobUserConstants.REPORTS_TO));
    setDepartmentId(request.getParameter(JobUserConstants.DEPARTMENT));
    setCompensation(request.getParameter(JobUserConstants.COMPENSATION));
    setCompensationUnit(request.getParameter(JobUserConstants.COMPENSATION_UNIT));
    setOfficeLocation(request.getParameter(JobUserConstants.OFFICE_LOCATION));


  }

}
