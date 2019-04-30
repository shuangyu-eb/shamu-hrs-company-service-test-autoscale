package shamu.company.job;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
public class JobUserDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  private String imageUrl;

  private String firstName;

  private String lastName;

  private String email;

  private String jobTitle;

  private String cityName;

  private String phoneNumber;

  private String department;

  public JobUserDto(User user, JobUser reporterWithJob) {
    this.setFirstName(user.getUserPersonalInformation().getFirstName());
    this.setPhoneNumber(user.getUserContactInformation().getPhoneWork());
    this.setEmail(user.getUserContactInformation().getEmailWork());
    this.setImageUrl(user.getImageUrl());
    if (reporterWithJob != null) {
      this.setJobTitle(reporterWithJob.getJob().getTitle());
    }
  }
}
