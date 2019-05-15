package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;

@Data
@NoArgsConstructor
public class JobInformationDto extends BasicJobInformationDto {

  private CompensationDto compensation;

  private void setCompensation(UserCompensation userCompensation) {
    if (userCompensation != null) {
      this.compensation = new CompensationDto(userCompensation);
    }
  }

  public JobInformationDto(JobUser jobUser) {
    super(jobUser);
    User user = jobUser.getUser();
    this.setCompensation(user.getUserCompensation());
  }
}
