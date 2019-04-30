package shamu.company.common.config;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shamu.company.authorization.AuthorityPojo;
import shamu.company.authorization.PermissionService;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.service.UserService;

@Component
public class JwtTokenProvider {

  @Value("${auth0.jwks}")
  private String jwks;
  @Value("${auth0.algorithm}")
  private String algorithm;
  @Value("${auth0.authDomain}")
  private String authDomain;

  private final UserService userService;
  private final PermissionService permissionService;

  @Autowired
  public JwtTokenProvider(UserService userService, PermissionService permissionService) {
    this.userService = userService;
    this.permissionService = permissionService;
  }

  private boolean isRightAlgorithm(String token) {
    DecodedJWT jwt = JWT.decode(token);
    return this.algorithm.equals(jwt.getAlgorithm());
  }

  private DecodedJWT verifySignatureAndGetDecodedJWT(String token) {
    DecodedJWT jwt = JWT.decode(token);
    JwkProvider provider = new UrlJwkProvider(jwks);
    Jwk jwk;
    try {
      jwk = provider.get(jwt.getKeyId());
      Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
      JWTVerifier verifier = JWT.require(algorithm).withIssuer(authDomain).build();
      return verifier.verify(token);
    } catch (JwkException | TokenExpiredException e) {
      throw new UnAuthenticatedException(e.getMessage());
    }
  }

  Authentication authenticate(String token) {
    if (!this.isRightAlgorithm(token)) {
      return null;
    }

    DecodedJWT decodedJWT = this.verifySignatureAndGetDecodedJWT(token);
    if (decodedJWT == null) {
      return null;
    }
    String email = decodedJWT.getClaim("email").asString();
    User user = userService.findUserByEmailAndStatus(email, Status.ACTIVE);
    List<AuthorityPojo> authorities = permissionService.getPermissionByUser(user);

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
