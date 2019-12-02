package shamu.company.common;

import java.util.Collections;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;
import shamu.company.tests.utils.JwtUtil;

class BaseRestControllerTests {

  @Mock private AuthUserCacheManager mockedCacheManager;

  @BeforeAll
  static void setUp() {
    final Jwt jwt = JwtUtil.getJwt();
    final AuthUser authUser = new AuthUser();
    authUser.setId("1");
    authUser.setCompanyId("1");
    authUser.setUserId(RandomStringUtils.randomAlphabetic(10));

    final String userId = "1";
    final Authentication authentication = new DefaultJwtAuthenticationToken(jwt, userId,
        Collections.emptyList(), authUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockedCacheManager.getCachedUser(Mockito.anyString())).thenReturn(new AuthUser());
  }

  @Test
  void testGetUser() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    Whitebox.setInternalState(baseRestController,
        "authUserCacheManager", mockedCacheManager);

    final AuthUser user = baseRestController.getAuthUser();
    Assertions.assertNotNull(user);
  }

  @Test
  void testGetCompanyId() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    Whitebox.setInternalState(baseRestController,
        "authUserCacheManager", mockedCacheManager);
    final String companyId = baseRestController.getCompanyId();
    Assertions.assertNotNull(companyId);
  }

  @Test
  void testGetUserId() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    Whitebox.setInternalState(baseRestController,
        "authUserCacheManager", mockedCacheManager);
    final String userId = baseRestController.getUserId();
    Assertions.assertNotNull(userId);
  }
}
