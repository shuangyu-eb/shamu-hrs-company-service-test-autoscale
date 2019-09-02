package shamu.company.utils;

import com.auth0.client.auth.AuthAPI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth0")
public class Auth0Config {

  private String jwks;

  private String domain;

  private String clientId;

  private String clientSecret;

  private String audience;

  private String database;

  private String customNamespace;

  AuthAPI getAuthApi() {
    return new AuthAPI(domain, this.clientId, this.clientSecret);
  }

}
