package shamu.company.authorization;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Collections;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.company.entity.Company;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

class CompanyPermissionUtilTests {

  @Mock private UserService userService;
  @Mock private AuthUserCacheManager authUserCacheManager;

  @InjectMocks private CompanyPermissionUtils companyPermissionUtils;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    Whitebox.setInternalState(companyPermissionUtils, "authUserCacheManager", authUserCacheManager);
    final Jwt jwt = JwtUtil.getJwt();
    final Authentication authentication =
        new DefaultJwtAuthenticationToken(
            jwt, RandomStringUtils.randomAlphabetic(16), Collections.emptyList(), new AuthUser());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  class IsMember {

    private AuthUser authUser;

    @BeforeEach
    void init() {
      authUser = new AuthUser();
      authUser.setCompanyId(RandomStringUtils.randomAlphabetic(16));
      Mockito.when(authUserCacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
    }

    @Test
    void whenIsNotUserType_thenShouldReturnFalse() {
      assertThat(
              companyPermissionUtils.isMember(
                  RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(2)))
          .isFalse();
    }

    @Test
    void whenCanNotFindUser_thenShouldReturnFalse() {
      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(null);
      assertThat(companyPermissionUtils.isMember(null, RandomStringUtils.randomAlphabetic(16)))
          .isFalse();
    }

    @Nested
    class UserIsMember {

      private User user;

      @BeforeEach
      void init() {
        final Company userCompany = new Company();
        user = User.builder().company(userCompany).build();
        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(user);
      }

      @Test
      void whenIsInSameCompany_thenShouldReturnTrue() {
        user.getCompany().setId(authUser.getCompanyId());
        assertThat(companyPermissionUtils.isMember(RandomStringUtils.randomAlphabetic(16)))
            .isTrue();
      }

      @Test
      void whenIsNotInSameCompany_thenShouldReturnFalse() {
        user.getCompany().setId(RandomStringUtils.randomAlphabetic(16));
        assertThat(companyPermissionUtils.isMember(RandomStringUtils.randomAlphabetic(16)))
            .isFalse();
      }
    }
  }
}
