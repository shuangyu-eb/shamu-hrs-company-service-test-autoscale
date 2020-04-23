package shamu.company.common;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.net.AuthRequest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.helpers.auth0.Auth0Config;
import shamu.company.helpers.auth0.Auth0Manager;
import shamu.company.tests.utils.JwtUtil;

@PrepareForTest(value = {UrlJwkProvider.class, Algorithm.class})
class Auth0MangerTests {

  private static final String issuer = "https://simplyhired-test.auth0.com/";
  private static final String customNamespace = "https://interviewed.com/";
  private static PublicKey PUBLIC_KEY;
  private static PrivateKey PRIVATE_KEY;
  private Auth0Manager auth0Manager;
  private Auth0Config auth0Config;
  private AuthAPI mockAuthApi;
  private ManagementAPI managementAPI;

  @BeforeEach
  void setUp() {
    final String clientId = RandomStringUtils.randomAlphabetic(10);
    final String clientSecret = RandomStringUtils.randomAlphabetic(10);
    final String domain = "indeed.com";
    final String issuer = String.format("https://%s/", domain);
    final String managementIdentifier = String.format("https://%s/api/v2/", domain);
    final String jwks = String.format("https://%s/.well-known/jwks.json", domain);

    final Auth0Config auth0Config =
        Auth0Config.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .domain(domain)
            .jwks(jwks)
            .issuer(issuer)
            .managementIdentifier(managementIdentifier)
            .build();

    auth0Manager = new Auth0Manager(auth0Config);
    mockAuthApi = Mockito.mock(AuthAPI.class);
    managementAPI = Mockito.mock(ManagementAPI.class);
    Whitebox.setInternalState(auth0Manager, "authApi", mockAuthApi);
  }

  @Test
  void whenManagerIsNull_thenShouldReturnNew() throws Auth0Exception {
    final AuthRequest mockAuthRequest = Mockito.mock(AuthRequest.class);
    final TokenHolder mockTokenHolder = Mockito.mock(TokenHolder.class);
    Mockito.when(mockAuthApi.requestToken(Mockito.anyString())).thenReturn(mockAuthRequest);
    Mockito.when(mockAuthRequest.execute()).thenReturn(mockTokenHolder);
    Mockito.when(mockTokenHolder.getAccessToken())
        .thenReturn(RandomStringUtils.randomAlphabetic(10));

    final ManagementAPI manager = auth0Manager.getManagementApi();
    Assertions.assertNotNull(manager);
  }

  @Test
  void whenManagerIsNotNull_thenShouldReturnManager() throws Auth0Exception {
    final AuthRequest mockAuthRequest = Mockito.mock(AuthRequest.class);
    final TokenHolder mockTokenHolder = Mockito.mock(TokenHolder.class);
    Whitebox.setInternalState(auth0Manager, "manager", managementAPI);
    Mockito.when(mockAuthApi.requestToken(Mockito.anyString())).thenReturn(mockAuthRequest);
    Mockito.when(mockAuthRequest.execute()).thenReturn(mockTokenHolder);
    Mockito.when(mockTokenHolder.getAccessToken())
        .thenReturn(RandomStringUtils.randomAlphabetic(10));

    final ManagementAPI returnManager = auth0Manager.getManagementApi();
    Assertions.assertNotNull(returnManager);
  }

  @Test
  void whenManagerIsNotNullButExecuteFail_thenShouldThrow() throws Auth0Exception {
    final AuthRequest mockAuthRequest = Mockito.mock(AuthRequest.class);
    final TokenHolder mockTokenHolder = Mockito.mock(TokenHolder.class);
    final Auth0Exception generalAuth0Exception = Mockito.mock(Auth0Exception.class);
    Whitebox.setInternalState(auth0Manager, "manager", managementAPI);
    Mockito.when(mockAuthApi.requestToken(Mockito.anyString())).thenReturn(mockAuthRequest);
    Mockito.when(mockAuthRequest.execute()).thenThrow(generalAuth0Exception);
    Mockito.when(mockTokenHolder.getAccessToken())
        .thenReturn(RandomStringUtils.randomAlphabetic(10));

    Assertions.assertThrows(GeneralAuth0Exception.class, () -> auth0Manager.getManagementApi());
  }

  @Nested
  class TokenHolderVerifyToken {

    String token;

    @BeforeEach
    void initial() throws NoSuchAlgorithmException {
      token = JwtUtil.generateRsaToken();
    }

    @Test
    void whenTokenIsInvalid_thenShouldThrow() {
      final TokenHolder mockTokenHolder = Mockito.mock(TokenHolder.class);
      Whitebox.setInternalState(auth0Manager, "manager", managementAPI);
      Whitebox.setInternalState(auth0Manager, "tokenHolder", mockTokenHolder);
      Mockito.when(mockTokenHolder.getAccessToken()).thenReturn(token);

      Assertions.assertThrows(GeneralAuth0Exception.class, () -> auth0Manager.getManagementApi());
    }
  }
}
