package shamu.company.common.config;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import shamu.company.server.dto.AuthUser;

public class DefaultJwtAuthenticationToken extends JwtAuthenticationToken {

  private static final long serialVersionUID = -4083333063347300287L;
  private final String userId;

  private final AuthUser authUser;

  private final String userEmail;

  public DefaultJwtAuthenticationToken(
      final Jwt jwt,
      final String userId,
      final Collection<? extends GrantedAuthority> authorities,
      final AuthUser authUser,
      final String userEmail) {
    super(jwt, authorities);
    this.userId = userId.toUpperCase();
    this.authUser = authUser;
    this.userEmail = userEmail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultJwtAuthenticationToken)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DefaultJwtAuthenticationToken that = (DefaultJwtAuthenticationToken) o;
    return userId.equals(that.userId) && authUser.equals(that.authUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), userId, authUser);
  }

  public String getUserId() {
    return userId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public AuthUser getAuthUser() {
    return authUser;
  }
}
