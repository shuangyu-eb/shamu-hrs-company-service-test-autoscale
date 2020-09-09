package shamu.company.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.apache.commons.lang.RandomStringUtils;
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
import shamu.company.common.multitenant.TenantContext;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;

class BaseRestControllerTests {

  @Mock private AuthUserCacheManager mockedCacheManager;

  @BeforeAll
  static void setUp() {
    final Jwt jwt = JwtUtil.getJwt();
    final AuthUser authUser = new AuthUser();
    authUser.setId("1");
    authUser.setCompanyId("1");
    authUser.setId(RandomStringUtils.randomAlphabetic(10));

    final String userId = "1";
    final Authentication authentication =
        new DefaultJwtAuthenticationToken(jwt, userId, Collections.emptyList(), authUser);
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
    Whitebox.setInternalState(baseRestController, "authUserCacheManager", mockedCacheManager);

    assertThat(baseRestController.findAuthUser()).isNotNull();
  }

  @Test
  void testGetCompanyId() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    Whitebox.setInternalState(baseRestController, "authUserCacheManager", mockedCacheManager);

    TenantContext.setCurrentTenant("123");
    assertThat(baseRestController.findCompanyId()).isNotNull();
    TenantContext.clear();
  }

  @Test
  void testGetUserId() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    Whitebox.setInternalState(baseRestController, "authUserCacheManager", mockedCacheManager);

    assertThat(baseRestController.findUserId()).isNotNull();
  }
}
