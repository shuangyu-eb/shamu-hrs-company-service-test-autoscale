package shamu.company.employee.dto;

import java.sql.Date;
import lombok.Data;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Data
public class BasicJobInformationDto {

  private Job job;

  private EmploymentType employmentType;

  private Date startData;

  private BasicUserPersonalInformationDto manager;

  private Office office;

  public BasicJobInformationDto(JobUser jobUser) {
    this.job = jobUser.getJob();
    this.employmentType = jobUser.getEmploymentType();
    User manager = jobUser.getUser().getManagerUser();
    if (manager != null) {
      this.manager = new BasicUserPersonalInformationDto(manager.getUserPersonalInformation());
    }
    this.startData = jobUser.getStartDate();
    this.office = jobUser.getOffice();
  }
}
