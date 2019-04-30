package shamu.company.authorization;

import java.util.List;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import shamu.company.hashids.HashidsFormat;

@Data
public class AuthorityPojo implements GrantedAuthority {

  Permission.Name name;

  @HashidsFormat
  List<Long> ids;

  @Override
  public String getAuthority() {
    return name.toString();
  }

  AuthorityPojo(Permission permission) {
    this.name = permission.getName();
  }
}
