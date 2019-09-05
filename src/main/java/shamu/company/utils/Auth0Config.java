package shamu.company.utils;

import com.auth0.client.auth.AuthAPI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "auth0")
public class Auth0Config {

  private String jwks;

  private String domain;

  private String issuer;

  private String clientId;

  private String clientSecret;

  private String audience;

  private String database;

  private String customNamespace;

  private String managementIdentifier;

  AuthAPI getAuthApi() {
    return new AuthAPI(domain, this.clientId, this.clientSecret);
  }

}
