package shamu.company.authorization;

import java.util.List;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
class AuthorityDto {

  @HashidsFormat
  private Long id;

  private List<AuthorityPojo> authorities;

  AuthorityDto(User user) {
    this.id = user.getId();
  }
}
