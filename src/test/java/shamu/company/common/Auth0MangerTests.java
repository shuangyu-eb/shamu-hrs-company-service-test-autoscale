package shamu.company.common;


import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.net.AuthRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import shamu.company.common.config.Auth0Manager;

@RunWith(SpringJUnit4ClassRunner.class)
@PrepareForTest(value = {UrlJwkProvider.class, Algorithm.class})
public class Auth0MangerTests {

  private Auth0Manager auth0Manager;

  @Before
  public void setUp() {
    final String clientId = RandomStringUtils.randomAlphabetic(10);
    final String clientSecret = RandomStringUtils.randomAlphabetic(10);
    final String domain = "indeed.com";
    final String jwks = String.format("https://%s/.well-known/jwks.json", domain);

    auth0Manager = new Auth0Manager(clientId, clientSecret, domain, jwks);
  }

  @Test
  public void whenManagerIsNull_thenShouldReturnNew() throws Auth0Exception {
    final AuthAPI mockAuthApi = Mockito.mock(AuthAPI.class);
    final AuthRequest mockAuthRequest = Mockito.mock(AuthRequest.class);

    final TokenHolder mockTokenHolder = Mockito.mock(TokenHolder.class);

    Mockito.when(mockAuthApi.requestToken(Mockito.anyString())).thenReturn(mockAuthRequest);
    Mockito.when(mockAuthRequest.execute()).thenReturn(mockTokenHolder);
    Mockito.when(mockTokenHolder.getAccessToken())
        .thenReturn(RandomStringUtils.randomAlphabetic(10));

    Whitebox.setInternalState(auth0Manager, "authApi", mockAuthApi);

    final ManagementAPI manager = auth0Manager.getManagementApi();
    Assertions.assertNotNull(manager);
  }
}
