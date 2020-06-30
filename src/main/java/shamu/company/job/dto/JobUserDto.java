package shamu.company.job.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Department;
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

  private Timestamp startDate;

  public JobUserDto(final User user, final JobUser userWithJob) {
    setId(user.getId());
    if (user.getUserPersonalInformation() != null) {
      setFirstName(user.getUserPersonalInformation().getFirstName());
      setLastName(user.getUserPersonalInformation().getLastName());
      setPreferredName(user.getUserPersonalInformation().getPreferredName());
    }

    if (user.getUserContactInformation() != null) {
      setPhoneNumber(user.getUserContactInformation().getPhoneWork());
      setEmail(user.getUserContactInformation().getEmailWork());
    }

    setImageUrl(user.getImageUrl());
    if (userWithJob != null) {
      final Job job = userWithJob.getJob();
      final Department userWithJobDepartment = userWithJob.getDepartment();

      if (job != null) {
        setJobTitle(job.getTitle());
      }
      if (userWithJobDepartment != null) {
        setDepartment(userWithJobDepartment.getName());
      }

      employmentType =
          userWithJob.getEmploymentType() == null
              ? null
              : userWithJob.getEmploymentType().getName();
      setStartDate(userWithJob.getStartDate());
    }
  }
}
