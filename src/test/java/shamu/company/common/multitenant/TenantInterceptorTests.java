package shamu.company.common.multitenant;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import shamu.company.common.service.TenantService;
import shamu.company.utils.UuidUtil;

public class TenantInterceptorTests {

  @Mock private JwtDecoder jwtDecoder;

  @Mock private TenantService tenantService;

  private final String customNamespace = "http://namespace/";

  private TenantInterceptor tenantInterceptor;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    tenantInterceptor = new TenantInterceptor(jwtDecoder, customNamespace, tenantService);
  }

  @Nested
  class PreHandle {

    private MockHttpServletRequest request;

    private String userId;

    private String companyId;

    @BeforeEach
    void init() {
      TenantContext.clear();
      final String bearerToken = RandomStringUtils.randomAlphabetic(16);
      request = new MockHttpServletRequest();
      request.addHeader("Authorization", "Bearer " + bearerToken);

      userId = UuidUtil.getUuidString().toUpperCase();
      companyId = UuidUtil.getUuidString().toUpperCase();

      final Map<String, Object> headerMap = new HashMap<>();
      headerMap.put("alg", "RS256");
      final Map<String, Object> bodyMap = new HashMap<>();
      bodyMap.put(customNamespace + "id", userId);
      bodyMap.put(customNamespace + "companyId", companyId);
      final Jwt jwt =
          new Jwt(bearerToken, Instant.now(), Instant.now().plusSeconds(30L), headerMap, bodyMap);
      Mockito.when(jwtDecoder.decode(bearerToken)).thenReturn(jwt);
    }

    @Test
    void whenNoBearerToken_thenShouldReturnTrue() {
      final boolean result =
          tenantInterceptor.preHandle(
              Mockito.mock(HttpServletRequest.class),
              Mockito.mock(HttpServletResponse.class),
              new Object());
      Assertions.assertThat(result).isTrue();
    }

    @Test
    void whenIsNotMockRequest_thenShouldUseTokenCompanyId() {
      Mockito.when(tenantService.isCompanyExists(companyId)).thenReturn(true);
      final boolean result =
          tenantInterceptor.preHandle(
              request, Mockito.mock(HttpServletResponse.class), new Object());

      Assertions.assertThat(result).isTrue();
      Assertions.assertThat(TenantContext.getCurrentTenant()).isEqualTo(companyId);
    }

    @Test
    void whenIsMockRequest_thenShouldUseMockCompanyId() {
      final String mockCompanyId = UuidUtil.getUuidString().toUpperCase();
      request.addHeader("X-Mock-Company", mockCompanyId);
      Mockito.when(tenantService.isCompanyExists(mockCompanyId)).thenReturn(true);
      final boolean result =
          tenantInterceptor.preHandle(
              request, Mockito.mock(HttpServletResponse.class), new Object());

      Assertions.assertThat(result).isTrue();
      Assertions.assertThat(TenantContext.getCurrentTenant()).isEqualTo(mockCompanyId);
    }

    @Test
    void whenCompanyIsNotExist_thenShouldReturnTrue() {
      Mockito.when(tenantService.isCompanyExists(companyId)).thenReturn(false);
      final boolean result =
          tenantInterceptor.preHandle(
              request, Mockito.mock(HttpServletResponse.class), new Object());

      Assertions.assertThat(result).isFalse();
      Assertions.assertThat(TenantContext.getCurrentTenant()).isNull();
    }
  }
}
