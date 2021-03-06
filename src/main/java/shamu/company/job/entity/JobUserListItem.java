package shamu.company.job.entity;

import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import shamu.company.user.entity.User.Role;

@Data
@NoArgsConstructor
public class JobUserListItem {

  private String id;

  private String imageUrl;

  private String firstName;

  private String preferredName;

  private String lastName;

  private String department;

  private String jobTitle;

  private String roleName;

  public JobUserListItem(
      final String id,
      final String imageUrl,
      final String firstName,
      final String lastName,
      final String department,
      final String jobTitle,
      final String roleName,
      final String preferredName) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.firstName = firstName;
    this.lastName = lastName;
    this.department = department;
    this.jobTitle = jobTitle;
    this.roleName = roleName;
    this.preferredName = preferredName;
  }

  public Optional<String> getRoleName() {

    if (StringUtils.equals(Role.ADMIN.getValue(), roleName)
        || StringUtils.equals(Role.INACTIVATE.getValue(), roleName)) {
      return Optional.of(roleName);
    }

    return Optional.empty();
  }
}
