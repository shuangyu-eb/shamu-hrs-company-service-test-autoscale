package shamu.company.job.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
public class JobUserDto {

  private String id;

  private String userId;

  private String imageUrl;

  private String firstName;

  private String preferredName;

  private String lastName;

  private String email;

  private String jobTitle;

  private String cityName;

  private String phoneNumber;

  private String department;

  private String employmentType;

  public JobUserDto(final User user, final JobUser userWithJob) {
    this.setId(user.getId());
    if (user.getUserPersonalInformation() != null) {
      this.setFirstName(user.getUserPersonalInformation().getFirstName());
      this.setLastName(user.getUserPersonalInformation().getLastName());
      this.setPreferredName(user.getUserPersonalInformation().getPreferredName());
    }

    if (user.getUserContactInformation() != null) {
      this.setPhoneNumber(user.getUserContactInformation().getPhoneWork());
      this.setEmail(user.getUserContactInformation().getEmailWork());
    }

    this.setImageUrl(user.getImageUrl());
    if (userWithJob != null) {
      final Job job = userWithJob.getJob();
      this.setJobTitle(job.getTitle());
      this.setDepartment(job.getDepartment().getName());
      this.employmentType =
          userWithJob.getEmploymentType() == null
              ? null
              : userWithJob.getEmploymentType().getName();
    }
  }
}
