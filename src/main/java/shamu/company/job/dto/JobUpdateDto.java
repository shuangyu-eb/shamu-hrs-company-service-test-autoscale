package shamu.company.job.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.UserCompensation;

@Data
public class JobUpdateDto {

  @HashidsFormat
  private Long jobUserId;

  @HashidsFormat
  private Long userCompensationId;

  private Integer compensationWage;

  @HashidsFormat
  private Long compensationFrequencyId;

  @HashidsFormat
  // TODO remove it, when remove the department_id for table jobs_users
  private Long departmentId;

  @HashidsFormat
  private Long employmentTypeId;

  @HashidsFormat
  private Long jobId;

  @HashidsFormat
  private Long managerId;

  @HashidsFormat
  private Long officeId;

  private Timestamp startDate;

  public JobUser updateJobUser(JobUser jobUser) {

    jobUser.setId(jobUserId);

    Job job = new Job();
    job.setId(jobId);
    jobUser.setJob(job);

    Office office = new Office();
    office.setId(officeId);
    jobUser.setOffice(office);

    EmploymentType employmentType = new EmploymentType();
    employmentType.setId(employmentTypeId);
    jobUser.setEmploymentType(employmentType);

    jobUser.setStartDate(startDate);

    return jobUser;
  }

  public UserCompensation updateUserCompensation(UserCompensation userCompensation) {
    if (userCompensation == null) {
      userCompensation = new UserCompensation();
    }
    userCompensation.setId(userCompensationId);
    userCompensation.setWage(compensationWage);
    if (this.compensationFrequencyId != null) {
      CompensationFrequency compensationFrequency = new CompensationFrequency();
      compensationFrequency.setId(this.compensationFrequencyId);
      userCompensation.setCompensationFrequency(compensationFrequency);
    }

    return userCompensation;
  }
}
