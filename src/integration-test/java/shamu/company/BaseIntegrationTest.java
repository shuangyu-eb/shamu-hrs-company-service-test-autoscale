package shamu.company;

import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import shamu.company.BaseIntegrationTest.InnerConfiguration;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.config.UserInformationGenerator;
import shamu.company.helpers.EmailHelper;
import shamu.company.helpers.RedisHelper;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.auth0.Auth0Manager;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(classes = {CompanyServiceApplication.class, UserInformationGenerator.class})
@Import(InnerConfiguration.class)
public abstract class BaseIntegrationTest {

  @MockBean protected AwsHelper awsHelper;

  @MockBean protected RedisHelper redisHelper;

  @MockBean protected EmailHelper emailHelper;

  @MockBean protected Auth0Manager auth0Manager;

  @MockBean protected Auth0Helper auth0Helper;

  @MockBean protected AuthUserCacheManager authUserCacheManager;

  @BeforeEach
  void initAuthentication() {
    given(authUserCacheManager.getCachedUser(Mockito.any())).willReturn(getAuthUser());
  }

  protected AuthUser getAuthUser() {
    final DefaultJwtAuthenticationToken authentication =
        (DefaultJwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

    return authentication.getAuthUser();
  }

  protected void setPermissions(final List<String> permissions) {
    getAuthUser().setPermissions(permissions);
  }

  protected void setPermission(final String permission) {
    getAuthUser().setPermissions(Collections.singletonList(permission));
  }

  @TestConfiguration
  static class InnerConfiguration {

    @Bean
    JwtDecoder jwtDecoder() {
      return JwtUtil::decode;
    }
  }
}
