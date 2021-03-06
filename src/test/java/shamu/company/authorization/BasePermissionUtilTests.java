package shamu.company.authorization;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Collections;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;

class BasePermissionUtilTests {

  @Mock private AuthUserCacheManager authUserCacheManager;

  @InjectMocks private BasePermissionUtils basePermissionUtils;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    final Jwt jwt = JwtUtil.getJwt();
    final Authentication authentication =
        new DefaultJwtAuthenticationToken(
            jwt,
            RandomStringUtils.randomAlphabetic(16),
            Collections.emptyList(),
            new AuthUser(),
            "test@gmail.com");
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  class GetAuthUser {
    private AuthUser authUser;

    @BeforeEach
    void init() {
      authUser = new AuthUser();
      Mockito.when(authUserCacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
    }

    @Test
    void whenIsAnonymous_thenShouldThrow() {
      Mockito.when(authUserCacheManager.getCachedUser(Mockito.anyString())).thenReturn(null);
      assertThatExceptionOfType(UnAuthenticatedException.class)
          .isThrownBy(
              () -> {
                final AuthUser returnedAuthUser = basePermissionUtils.getAuthUser();
                Assertions.assertEquals(authUser, returnedAuthUser);
              });
      Assertions.assertThrows(
          UnAuthenticatedException.class,
          () -> {
            final AuthUser returnedAuthUser = basePermissionUtils.getAuthUser();
            Assertions.assertEquals(authUser, returnedAuthUser);
          });
    }

    @Test
    void whenIsNotAnonymous_thenUserShouldMatch() {
      final AuthUser returnedAuthUser = basePermissionUtils.getAuthUser();
      Assertions.assertEquals(returnedAuthUser, authUser);
      assertThat(returnedAuthUser).isEqualTo(authUser);
    }
  }

  @Nested
  class GetCompanyId {
    private AuthUser authUser;

    @BeforeEach
    void init() {
      authUser = new AuthUser();
      authUser.setCompanyId(RandomStringUtils.randomAlphabetic(16));
      Mockito.when(authUserCacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
    }

    @Test
    void testGetAuthUser() {
      final AuthUser returnedAuthUser = basePermissionUtils.getAuthUser();
      assertThat(returnedAuthUser.getCompanyId()).isEqualTo(authUser.getCompanyId());
    }
  }

  @Nested
  class GetUserId {
    private AuthUser authUser;

    @BeforeEach
    void init() {
      authUser = new AuthUser();
      authUser.setId(RandomStringUtils.randomAlphabetic(16));
      Mockito.when(authUserCacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
    }

    @Test
    void testGetAuthUser() {
      final AuthUser returnedAuthUser = basePermissionUtils.getAuthUser();
      assertThat(returnedAuthUser.getId()).isEqualTo(authUser.getId());
    }
  }
}
