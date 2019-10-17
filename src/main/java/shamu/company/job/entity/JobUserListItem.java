package shamu.company.job.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User.Role;

@Data
@NoArgsConstructor
public class JobUserListItem {

  @HashidsFormat
  private Long id;

  private String imageUrl;

  private String firstName;

  private String lastName;

  private String department;

  private String jobTitle;

  private String roleName;

  public JobUserListItem(final Long id, final String imageUrl, final String firstName,
      final String lastName, final String department, final String jobTitle,
      final String roleName) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.firstName = firstName;
    this.lastName = lastName;
    this.department = department;
    this.jobTitle = jobTitle;
    this.roleName = roleName;
  }

  public String getRoleName() {

    if (StringUtils.equals(Role.ADMIN.getValue(), roleName)
        || StringUtils.equals(Role.INACTIVATE.getValue(), roleName)) {
      return roleName;
    }

    return null;
  }
}
