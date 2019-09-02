package shamu.company.common.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class DefaultJwtAuthenticationToken extends JwtAuthenticationToken {

  private String id;

  public DefaultJwtAuthenticationToken(Jwt jwt, String id,
      Collection<? extends GrantedAuthority> authorities) {
    super(jwt, authorities);
    this.id = id;
  }

  public String getId() {
    return this.id;
  }
}
