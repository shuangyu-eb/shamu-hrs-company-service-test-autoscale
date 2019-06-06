package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.dto.OfficeDto;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UserNameUtil;

@Data
@NoArgsConstructor
public class BasicJobInformationDto {

  @HashidsFormat
  private Long jobUserId;

  private User.Role userRole;

  private SelectFieldInformationDto job;

  private SelectFieldInformationDto employmentType;

  private Timestamp startDate;

  private Timestamp endDate;

  private SelectFieldInformationDto manager;

  private SelectFieldInformationDto department;

  private OfficeDto office;

  public BasicJobInformationDto(JobUser jobUser) {
    User user = jobUser.getUser();
    Job job = jobUser.getJob();

    this.setJob(job);
    this.setDepartment(job.getDepartment());
    this.setManager(user.getManagerUser());
    this.setEmploymentType(jobUser.getEmploymentType());
    this.setOffice(jobUser.getOffice());
    this.jobUserId = jobUser.getId();
    this.startDate = jobUser.getStartDate();
    this.endDate = jobUser.getEndDate();
    this.userRole = user.getRole();
  }

  public BasicJobInformationDto(User user) {
    this.userRole = user.getRole();
  }

  private void setJob(Job job) {
    this.job = new SelectFieldInformationDto(job.getId(), job.getTitle());
  }

  private void setDepartment(Department department) {
    if (department != null) {
      this.department = new SelectFieldInformationDto(department);
    }
  }

  private void setEmploymentType(EmploymentType employmentType) {
    if (employmentType != null) {
      this.employmentType = new SelectFieldInformationDto(employmentType);
    }
  }

  private void setManager(User manager) {
    if (manager != null) {
      UserPersonalInformation information = manager.getUserPersonalInformation();
      String firstName = information.getFirstName();
      String middleName = information.getMiddleName();
      String lastName = information.getLastName();
      this.manager = new SelectFieldInformationDto(manager.getId(),
          UserNameUtil.getUserName(firstName, middleName, lastName));
    }
  }

  private void setOffice(Office office) {
    if (office != null) {
      this.office = new OfficeDto(office);
    }
  }
}
