package shamu.company.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.auth0.client.auth.AuthAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Auth0ConfigTest {
  @Nested
  class GetAuthApi{
    @Test
    void whenConfigParamsIsLack() {
      Auth0Config auth0Config = new Auth0Config();
      assertThrows(IllegalArgumentException.class, () -> auth0Config.getAuthApi());
    }
    @Test
    void whenConfigParamsIsNotLack() {
      Auth0Config auth0Config = new Auth0Config();
      auth0Config.setClientId("clientId");
      auth0Config.setClientSecret("clientSecret");
      auth0Config.setDomain("test.auth0.com");
      AuthAPI authAPI = auth0Config.getAuthApi();
      assertNotNull(authAPI);
    }
  }


}
