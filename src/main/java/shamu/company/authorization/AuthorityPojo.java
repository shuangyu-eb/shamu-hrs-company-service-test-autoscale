package shamu.company.authorization;

import java.util.List;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class AuthorityPojo implements GrantedAuthority {

  Permission.Name name;

  List<Long> ids;

  @Override
  public String getAuthority() {
    return name.toString();
  }

  AuthorityPojo(Permission permission) {
    this.name = permission.getName();
  }
}
