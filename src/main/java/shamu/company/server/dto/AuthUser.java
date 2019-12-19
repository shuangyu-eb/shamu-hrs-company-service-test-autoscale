package shamu.company.server.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;

@Data
@NoArgsConstructor
public class AuthUser {

  private String id;

  private String email;

  private String imageUrl;

  private String companyId;

  private String userId;

  private String managerId;

  private List<String> permissions;

  private Role role;

  public AuthUser(final User user) {
    id = user.getId();
    imageUrl = user.getImageUrl();
    email = user.getUserContactInformation().getEmailWork();
    companyId = user.getCompany().getId();
    userId = user.getId();
    role = user.getRole();

    if (user.getManagerUser() != null) {
      managerId = user.getManagerUser().getId();
    }
  }
}
