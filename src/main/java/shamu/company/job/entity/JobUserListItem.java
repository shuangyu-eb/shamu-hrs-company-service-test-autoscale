package shamu.company.job.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobUserListItem {

  private Long id;

  private String imageUrl;

  private String firstName;

  private String lastName;

  private String department;

  private String jobTitle;

  public JobUserListItem(Long id, String imageUrl, String firstName,
      String lastName, String department, String jobTitle) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.firstName = firstName;
    this.lastName = lastName;
    this.department = department;
    this.jobTitle = jobTitle;
  }
}
