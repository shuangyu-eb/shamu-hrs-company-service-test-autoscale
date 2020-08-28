package shamu.company.timeoff;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.ProxyManager;
import java.time.Duration;
import javax.servlet.http.HttpServletResponse;
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
import shamu.company.bucket.BucketManager;
import shamu.company.common.exception.errormapping.TooManyRequestException;
import shamu.company.timeoff.interceptor.TimeOffRequestRateLimitingInterceptor;
import shamu.company.utils.UuidUtil;

public class TimeOffRequestRateLimitingInterceptorTests {

  @Mock private BucketManager bucketManager;

  private final Long maxPerHour = 30L;

  @Mock private ProxyManager<String> buckets;

  @Mock private Bucket bucket;

  private MockHttpServletRequest request;

  private final String userId = UuidUtil.getUuidString().toUpperCase();

  private TimeOffRequestRateLimitingInterceptor interceptor;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    buildMockForBucketManger();

    interceptor = new TimeOffRequestRateLimitingInterceptor(bucketManager, maxPerHour);
    request = new MockHttpServletRequest();
    request.setRequestURI(String.format("/company/users/%s/time-off-requests", userId));
    request.setMethod(HttpMethod.POST.name());
  }

  private void buildMockForBucketManger() {
    final BucketConfiguration configuration =
        Bucket4j.configurationBuilder()
            .addLimit(Bandwidth.simple(maxPerHour, Duration.ofHours(1)))
            .build();
    Mockito.when(bucketManager.buildBuckets(Mockito.anyString())).thenReturn(buckets);

    final Bandwidth bandwidth = Mockito.mock(Bandwidth.class);
    Mockito.when(bucketManager.buildBandwidth(Mockito.anyLong(), Mockito.any()))
        .thenReturn(bandwidth);

    Mockito.when(bucketManager.buildBucketConfiguration(Mockito.any(Bandwidth.class)))
        .thenReturn(configuration);
    Mockito.when(bucketManager.getBucketFromBuckets(userId, buckets, configuration))
        .thenReturn(bucket);
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
    void whenTryConsumeFailed_thenShouldThrow() {
      Mockito.when(bucket.tryConsume(1)).thenReturn(false);
      Assertions.assertThatThrownBy(
              () ->
                  interceptor.preHandle(
                      request, Mockito.mock(HttpServletResponse.class), new Object()))
          .isInstanceOf(TooManyRequestException.class);
    }

    @Test
    void whenTryConsumeSuccess_thenShouldReturnTrue() {
      Mockito.when(bucket.tryConsume(1)).thenReturn(true);
      final boolean result =
          interceptor.preHandle(request, Mockito.mock(HttpServletResponse.class), new Object());
      Assertions.assertThat(result).isTrue();
    }
  }

  @Nested
  class TestAfterCompletion {
    @Test
    void whenResponseFailed_thenReturnTrue() {
      final HttpServletResponse response = new MockHttpServletResponse();
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      interceptor.afterCompletion(request, response, new Object(), new Exception());
      Mockito.verify(bucket, Mockito.times(1)).addTokens(1);
    }
  }
}
