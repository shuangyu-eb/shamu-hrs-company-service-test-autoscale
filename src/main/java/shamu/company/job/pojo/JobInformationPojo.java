package shamu.company.job.pojo;

import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UserNameUtil;

@Data
@NoArgsConstructor
public class JobInformationPojo {

  private String jobTitle;

  private String employmentTypeName;

  private Long employmentTypeId;

  private Date startDate;

  private Date endDate;

  private String managerName;

  private Long managerId;

  private String departmentName;

  private Long departmentId;

  private Integer compensation;

  private String compensationFrequencyName;

  private Long compensationFrequencyId;

  private String officeName;

  private OfficeAddress officeAddress;

  private Long officeAddressId;

  private Long locationId;

  private Long companyId;

  public JobInformationPojo(User user, Job job, JobUser jobUser) {
    this.jobTitle = job.getTitle();
    if (jobUser.getEmploymentType() != null) {
      this.employmentTypeId = jobUser.getEmploymentType().getId();
    }
    this.startDate = jobUser.getStartDate();
    this.managerId = user.getManagerUser().getId();
    if (jobUser.getDepartment() != null) {
      this.departmentId = jobUser.getDepartment().getId();
    }
    if (user.getUserCompensation() != null) {
      this.compensation = user.getUserCompensation().getWage();
    }
    if (user.getUserCompensation() != null
        && user.getUserCompensation().getCompensationFrequency() != null) {
      this.compensationFrequencyId = user.getUserCompensation().getCompensationFrequency().getId();
    }
    if (jobUser.getOffice() != null) {
      this.locationId = jobUser.getOffice().getId();
    }
    if (jobUser.getCompany() != null) {
      this.companyId = jobUser.getCompany().getId();
    }
  }

  public JobInformationPojo(JobUser jobUser) {
    String jobTitle = jobUser.getJob().getTitle();
    this.jobTitle = jobTitle;
    String employmentTypeName = null;
    if (jobUser.getEmploymentType() != null) {
      employmentTypeName = jobUser.getEmploymentType().getName();
    }
    this.employmentTypeName = employmentTypeName;

    User user = jobUser.getUser();
    UserPersonalInformation information = user.getManagerUser().getUserPersonalInformation();
    String firstName = information.getFirstName();
    String middleName = information.getMiddleName();
    String lastName = information.getLastName();
    this.managerName = UserNameUtil.getUserName(firstName, middleName, lastName);

    String departmentName = null;
    if (jobUser.getDepartment() != null) {
      departmentName = jobUser.getDepartment().getName();
    }
    this.departmentName = departmentName;

    Integer compensation = null;
    if (user.getUserCompensation() != null) {
      compensation = user.getUserCompensation().getWage();
    }
    this.compensation = compensation;

    String compensationFrequencyName = null;
    if (user.getUserCompensation() != null
        && user.getUserCompensation().getCompensationFrequency() != null) {
      compensationFrequencyName = user.getUserCompensation().getCompensationFrequency().getName();
    }
    this.compensationFrequencyName = compensationFrequencyName;

    OfficeAddress officeAddress = null;
    String officeName = null;
    Office office = jobUser.getOffice();
    if (office != null && office.getOfficeAddress() != null) {
      officeAddress = office.getOfficeAddress();
      officeName = office.getName();
    }
    this.officeAddress = officeAddress;
    this.officeName = officeName;
    this.startDate = jobUser.getStartDate();
    this.endDate = jobUser.getEndDate();
  }
}
