package shamu.company.job.pojo;

import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.hashids.HashidsFormat;
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

  @HashidsFormat
  private Long managerId;

  private String departmentName;

  @HashidsFormat
  private Long departmentId;

  private Integer compensation;

  private String compensationFrequencyName;

  private Long compensationFrequencyId;

  private String officeName;

  private OfficeAddress officeAddress;

  @HashidsFormat
  private Long officeAddressId;

  @HashidsFormat
  private Long locationId;

  @HashidsFormat
  private Long companyId;

  public JobInformationPojo(User user, Job job, JobUser jobUser) {
    this.jobTitle = job.getTitle();
    if (jobUser.getEmploymentType() != null) {
      this.employmentTypeId = jobUser.getEmploymentType().getId();
    }
    this.startDate = jobUser.getStartDate();
    if (user.getManagerUser() != null) {
      this.managerId = user.getManagerUser().getId();
    }

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

    this.jobTitle = jobUser.getJob().getTitle();

    if (jobUser.getEmploymentType() != null) {
      this.employmentTypeName = jobUser.getEmploymentType().getName();
    }

    User user = jobUser.getUser();
    if (user.getManagerUser() != null) {
      UserPersonalInformation information = user.getManagerUser().getUserPersonalInformation();
      String firstName = information.getFirstName();
      String middleName = information.getMiddleName();
      String lastName = information.getLastName();
      this.managerName = UserNameUtil.getUserName(firstName, middleName, lastName);
    }

    if (jobUser.getDepartment() != null) {
      this.departmentName = jobUser.getDepartment().getName();
    }

    if (user.getUserCompensation() != null) {
      this.compensation = user.getUserCompensation().getWage();
    }

    if (user.getUserCompensation() != null
        && user.getUserCompensation().getCompensationFrequency() != null) {
      this.compensationFrequencyName = user.getUserCompensation()
          .getCompensationFrequency().getName();
    }

    Office office = jobUser.getOffice();
    if (office != null && office.getOfficeAddress() != null) {
      this.officeAddress = office.getOfficeAddress();
      this.officeName = office.getName();
    }

    this.startDate = jobUser.getStartDate();
    this.endDate = jobUser.getEndDate();
  }
}
