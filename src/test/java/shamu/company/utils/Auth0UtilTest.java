package shamu.company.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import shamu.company.common.exception.GeneralException;

@RunWith(MockitoJUnitRunner.class)
class Auth0UtilTest {

  private Auth0Config auth0Config = new Auth0Config();

  Auth0Util auth0Util;

  @BeforeEach
  void setUp() {
    auth0Config.setClientId("clientId");
    auth0Config.setClientSecret("clientSecret");
    auth0Config.setDomain("test.auth0.com");
    auth0Util = new Auth0Util(auth0Config);
  }

  @Nested
  class Login {

    private final static String email = "test@teat.com";
    private final static String password = "Password^34";

    @Nested
    class whenFailed {

      @Test
      void thenThrowException() {
        Assertions.assertThrows(GeneralException.class, () -> auth0Util.login(email, password));
      }
    }
  }

}
