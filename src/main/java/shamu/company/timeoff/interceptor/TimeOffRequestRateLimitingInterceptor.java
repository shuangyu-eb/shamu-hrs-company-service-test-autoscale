package shamu.company.timeoff.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.grid.ProxyManager;
import java.time.Duration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import shamu.company.bucket.BucketManager;
import shamu.company.common.exception.errormapping.TooManyRequestException;

@Component
public class TimeOffRequestRateLimitingInterceptor extends HandlerInterceptorAdapter {

  private final BucketManager bucketManager;

  private final Long maxPerHour;

  private final ProxyManager<String> buckets;

  public TimeOffRequestRateLimitingInterceptor(
      final BucketManager bucketManager,
      @Value("${timeOff.requestRateLimiting.maxPerHour}") final Long maxPerHour) {
    this.bucketManager = bucketManager;
    this.maxPerHour = maxPerHour;

    buckets = bucketManager.buildBuckets("timeOffRequestRateLimitingBuckets");
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
    if (isNotTargetRequest(request)) {
      return true;
    }

    final String userId = getUserIdFromRequest(request);

    final boolean result = tryConsumeFromUserBucket(userId);
    if (!result) {
      throw new TooManyRequestException("Too many requests");
    }

    return true;
  }

  private boolean isNotTargetRequest(final HttpServletRequest request) {
    return !request.getMethod().equals(HttpMethod.POST.name());
  }

  private String getUserIdFromRequest(final HttpServletRequest request) {
    return request.getRequestURI().split("/")[3].toUpperCase();
  }

  private boolean tryConsumeFromUserBucket(final String userId) {
    final Bucket bucket = buildBucketByUserId(userId);
    return bucket.tryConsume(1);
  }

  private Bucket buildBucketByUserId(final String userId) {
    final Bandwidth bandwidth = bucketManager.buildBandwidth(maxPerHour, Duration.ofHours(1));
    return bucketManager.getBucketFromBuckets(
        userId, buckets, bucketManager.buildBucketConfiguration(bandwidth));
  }

  @Override
  public void afterCompletion(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      @Nullable final Exception ex) {
    if (isNotTargetRequest(request)) {
      return;
    }
    final String userId = getUserIdFromRequest(request);
    if (response.getStatus() != HttpStatus.OK.value()) {
      giveBackToUserBucket(userId);
    }
  }

  private void giveBackToUserBucket(final String userId) {
    final Bucket bucket = buildBucketByUserId(userId);
    bucket.addTokens(1);
  }
}
