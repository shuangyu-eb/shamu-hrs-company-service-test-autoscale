package shamu.company.server;

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

  private List<String> permissions;

  private Role role;

  AuthUser(final User user) {
    this.id = user.getId();
    this.imageUrl = user.getImageUrl();
    this.email = user.getUserContactInformation().getEmailWork();
    this.companyId = user.getCompany().getId();
    this.userId = user.getId();
    this.role = user.getRole();
  }
}
