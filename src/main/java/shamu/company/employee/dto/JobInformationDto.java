package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.job.entity.JobUser;


@Data
public class JobInformationDto extends BasicJobInformationDto {

  private UserCompensationDto userCompensation;

  public JobInformationDto(JobUser jobUser) {
    super(jobUser);
    this.userCompensation = new UserCompensationDto(jobUser.getUser().getUserCompensation());
  }
}
