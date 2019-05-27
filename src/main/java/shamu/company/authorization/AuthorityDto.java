package shamu.company.authorization;

import java.util.List;
import lombok.Data;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;

@Data
class AuthorityDto {

  private Long id;

  private String imageUrl;

  private Role role;

  private List<AuthorityPojo> authorities;

  AuthorityDto(User user) {
    this.id = user.getId();
    this.imageUrl = user.getImageUrl();
    this.role = user.getRole();
  }
}
