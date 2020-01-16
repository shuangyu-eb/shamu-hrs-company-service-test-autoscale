package shamu.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import shamu.company.authorization.PermissionUtils;
import shamu.company.helpers.auth0.Auth0Config;
import shamu.company.tests.utils.JwtUtil;

@TestConfiguration
public class BaseRestControllerConfiguration {

  @Autowired
  private PermissionUtils permissionUtils;

  @Bean
  Auth0Config auth0Config() {
    final Auth0Config auth0Config = new Auth0Config();
    auth0Config.setCustomNamespace("https://interviewed.com/");
    auth0Config.setJwks("https://simplyhired-test.auth0.com/.well-known/jwks.json");
    return auth0Config;
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return JwtUtil::decode;
  }

  @Bean(name = "permissionUtils")
  PermissionUtils permissionUtils() {
    return permissionUtils;
  }
}
