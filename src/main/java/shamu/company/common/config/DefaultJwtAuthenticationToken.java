package shamu.company.common.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import shamu.company.server.AuthUser;

public class DefaultJwtAuthenticationToken extends JwtAuthenticationToken {

  private final String userId;

  private final AuthUser authUser;

  public DefaultJwtAuthenticationToken(final Jwt jwt, final String userId,
      final Collection<? extends GrantedAuthority> authorities, final AuthUser authUser) {
    super(jwt, authorities);
    this.userId = userId;
    this.authUser = authUser;
  }

  public String getUserId() {
    return this.userId;
  }

  public AuthUser getAuthUser() {
    return authUser;
  }
}
