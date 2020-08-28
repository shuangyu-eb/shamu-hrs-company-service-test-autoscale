package shamu.company.employee;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.EstimationProbe;
import io.github.bucket4j.grid.ProxyManager;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import shamu.company.bucket.BucketManager;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.common.exception.errormapping.TooManyRequestException;
import shamu.company.employee.exception.UserInvitationCapabilityFrozenException;
import shamu.company.employee.interceptor.UserInvitationRateLimitingInterceptor;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

class UserInvitationRateLimitingInterceptorTests {

  @Mock private JwtDecoder decoder;

  @Mock private UserService userService;

  @Mock private BucketManager bucketManager;

  private final String customNamespace = "http://namespace/";

  private final Long maxPerMinute = 3L;

  private final Long maxPerDay = 200L;

  @Mock private ProxyManager<String> buckets;

  @Mock private Bucket bucket;

  private MockHttpServletRequest request;

  private BucketConfiguration configuration;

  private final String userId = UuidUtil.getUuidString().toUpperCase();

  private static final String AUTH_HEADER = "Authorization";

  private UserInvitationRateLimitingInterceptor interceptor;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(bucketManager.buildBuckets(Mockito.anyString())).thenReturn(buckets);
    interceptor =
        new UserInvitationRateLimitingInterceptor(
            decoder, customNamespace, userService, bucketManager, maxPerMinute, maxPerDay);

    buildMockForBucketManger();
    buildRequest();
  }

  private void buildMockForBucketManger() {
    configuration =
        Bucket4j.configurationBuilder()
            .addLimit(Bandwidth.simple(100, Duration.ofHours(1)))
            .build();

    Mockito.when(bucketManager.buildBucketConfiguration(Mockito.anyList()))
        .thenReturn(configuration);
    Mockito.when(bucketManager.getBucketFromBuckets(userId, buckets, configuration))
        .thenReturn(bucket);
  }

  private void buildRequest() {
    final String bearerToken = RandomStringUtils.randomAlphabetic(16);
    request = new MockHttpServletRequest();
    request.addHeader(AUTH_HEADER, "Bearer " + bearerToken);
    request.setMethod(HttpMethod.POST.name());

    final Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("alg", "RS256");
    final Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put(customNamespace + "id", userId);
    final Jwt jwt =
        new Jwt(bearerToken, Instant.now(), Instant.now().plusSeconds(30L), headerMap, bodyMap);
    Mockito.when(decoder.decode(bearerToken)).thenReturn(jwt);
  }

  @Nested
  class TestPreHandle {

    @Test
    void isNotPostRequest_thenReturnTrue() {
      request.setMethod(HttpMethod.GET.name());
      final boolean result =
          interceptor.preHandle(request, Mockito.mock(HttpServletResponse.class), new Object());
      Assertions.assertThat(result).isTrue();
    }

    @Test
    void whenNoBearerToken_thenShouldThrow() {
      request.removeHeader(AUTH_HEADER);
      Assertions.assertThatThrownBy(
              () ->
                  interceptor.preHandle(
                      request, Mockito.mock(HttpServletResponse.class), new Object()))
          .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void whenUserIsFrozen_thenShouldThrow() {
      Mockito.when(userService.isUserInvitationCapabilityFrozen(userId)).thenReturn(true);
      Assertions.assertThatThrownBy(
              () ->
                  interceptor.preHandle(
                      request, Mockito.mock(HttpServletResponse.class), new Object()))
          .isInstanceOf(UserInvitationCapabilityFrozenException.class);
    }

    @Test
    void whenTryConsumeFailed_thenShouldThrow() {
      Mockito.when(userService.isUserInvitationCapabilityFrozen(userId)).thenReturn(false);
      Mockito.when(bucket.tryConsume(1)).thenReturn(false);
      Assertions.assertThatThrownBy(
              () ->
                  interceptor.preHandle(
                      request, Mockito.mock(HttpServletResponse.class), new Object()))
          .isInstanceOf(TooManyRequestException.class);
    }

    @Test
    void whenTryConsumeSuccess_thenShouldReturnTrue() {
      Mockito.when(userService.isUserInvitationCapabilityFrozen(userId)).thenReturn(false);
      Mockito.when(bucket.tryConsume(1)).thenReturn(true);
      final boolean result =
          interceptor.preHandle(request, Mockito.mock(HttpServletResponse.class), new Object());
      Assertions.assertThat(result).isTrue();
    }
  }

  @Nested
  class TestAfterCompletion {

    private HttpServletResponse response;

    @BeforeEach
    void init() {
      response = new MockHttpServletResponse();
      response.setStatus(HttpStatus.OK.value());
    }

    @Test
    void whenResponseReturnFailed_thenShouldUnfreezeUser() {
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      interceptor.afterCompletion(request, response, new Object(), new Exception());
      Mockito.verify(userService, Mockito.times(1)).unfreezeUserInvitationCapability(userId);
    }

    @Test
    void whenResponseReturnSuccess_tokenRanOut_thenShouldFreezeUser() {
      final EstimationProbe estimationProbe = Mockito.mock(EstimationProbe.class);
      Mockito.when(estimationProbe.canBeConsumed()).thenReturn(false);
      Mockito.when(bucket.estimateAbilityToConsume(1)).thenReturn(estimationProbe);
      interceptor.afterCompletion(request, response, new Object(), new Exception());
      Mockito.verify(userService, Mockito.times(1)).freezeUserInvitationCapability(userId);
    }
  }
}
