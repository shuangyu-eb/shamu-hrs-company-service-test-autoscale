package shamu.company.common;


import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.net.AuthRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import shamu.company.utils.Auth0Config;
import shamu.company.utils.Auth0Manager;

@PrepareForTest(value = {UrlJwkProvider.class, Algorithm.class})
class Auth0MangerTests {

  private Auth0Manager auth0Manager;

  @BeforeEach
  void setUp() {
    final String clientId = RandomStringUtils.randomAlphabetic(10);
    final String clientSecret = RandomStringUtils.randomAlphabetic(10);
    final String domain = "indeed.com";
    final String issuer = String.format("https://%s/", domain);
    final String managementIdentifier = String.format("https://%s/api/v2/", domain);
    final String jwks = String.format("https://%s/.well-known/jwks.json", domain);

    final Auth0Config auth0Config = Auth0Config.builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .domain(domain)
        .jwks(jwks)
        .issuer(issuer)
        .managementIdentifier(managementIdentifier)
        .build();

    auth0Manager = new Auth0Manager(auth0Config);
  }

  @Test
  void whenManagerIsNull_thenShouldReturnNew() throws Auth0Exception {
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
