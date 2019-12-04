package shamu.company.helpers.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.net.AuthRequest;
import java.security.interfaces.RSAPublicKey;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.GeneralAuth0Exception;

@Component
public class Auth0Manager {

  private final AuthAPI authApi;

  private ManagementAPI manager;

  private TokenHolder tokenHolder;

  private final Auth0Config auth0Config;

  public Auth0Manager(final Auth0Config auth0Config) {
    authApi = auth0Config.getAuthApi();
    this.auth0Config = auth0Config;
  }

  private boolean verifyToken(final String token) {
    final DecodedJWT jwt = JWT.decode(token);
    final JwkProvider provider = new UrlJwkProvider(auth0Config.getJwks());
    final Jwk jwk;
    try {
      jwk = provider.get(jwt.getKeyId());
      final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
      final JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(auth0Config.getIssuer()).build();
      verifier.verify(token);
      return true;
    } catch (final JwkException e) {
      throw new GeneralAuth0Exception(
          "Invalid key info when validating.", e);
    } catch (final TokenExpiredException e) {
      return false;
    }
  }

  private TokenHolder getTokenHolder() {
    if (tokenHolder != null && verifyToken(tokenHolder.getAccessToken())) {
      return tokenHolder;
    }

    final AuthRequest authRequest = authApi.requestToken(auth0Config.getManagementIdentifier());
    try {
      tokenHolder = authRequest.execute();
      return tokenHolder;
    } catch (final Auth0Exception e) {
      throw new GeneralAuth0Exception(e.getMessage(), e);
    }
  }

  public ManagementAPI getManagementApi() {
    if (manager == null) {
      manager = new ManagementAPI(auth0Config.getDomain(), getTokenHolder().getAccessToken());
      return manager;
    }

    final String accessToken = getTokenHolder().getAccessToken();
    manager.setApiToken(accessToken);
    return manager;
  }
}
