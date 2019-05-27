package shamu.company.job.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.Job;
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

  private String employmentType;

  public JobUserDto(User user, JobUser userWithJob) {
    this.setId(user.getId());
    this.setFirstName(user.getUserPersonalInformation().getFirstName());
    this.setLastName(user.getUserPersonalInformation().getLastName());
    this.setPhoneNumber(user.getUserContactInformation().getPhoneWork());
    this.setEmail(user.getUserContactInformation().getEmailWork());
    this.setImageUrl(user.getImageUrl());
    if (userWithJob != null) {
      Job job = userWithJob.getJob();
      this.setJobTitle(job.getTitle());
      this.setDepartment(job.getDepartment().getName());
      this.setEmploymentType(userWithJob.getEmploymentType().getName());
    }
  }


}
