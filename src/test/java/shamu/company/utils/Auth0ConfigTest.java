package shamu.company.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.client.auth.AuthAPI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Auth0ConfigTest {

  @Nested
  class GetAuthApi {

    @Test
    void whenConfigParamsIsLack() {
      final Auth0Config auth0Config = new Auth0Config();
      assertThrows(IllegalArgumentException.class, () -> auth0Config.getAuthApi());
    }

    @Test
    void whenConfigParamsIsNotLack() {
      final Auth0Config auth0Config = new Auth0Config();
      auth0Config.setClientId("clientId");
      auth0Config.setClientSecret("clientSecret");
      auth0Config.setDomain("test.auth0.com");
      final AuthAPI authAPI = auth0Config.getAuthApi();
      assertNotNull(authAPI);
    }
  }


}
