package shamu.company.employee.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.grid.ProxyManager;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import shamu.company.bucket.BucketManager;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.common.exception.errormapping.TooManyRequestException;
import shamu.company.employee.exception.UserInvitationCapabilityFrozenException;
import shamu.company.user.service.UserService;

@Component
public class UserInvitationRateLimitingInterceptor extends HandlerInterceptorAdapter {

  private final JwtDecoder decoder;

  private final String customNamespace;

  private final UserService userService;

  private final BucketManager bucketManager;

  private final Long maxPerMinute;

  private final Long maxPerDay;

  private final ProxyManager<String> buckets;

  private static final String AUTH_HEADER = "Authorization";

  public UserInvitationRateLimitingInterceptor(
      final JwtDecoder decoder,
      final @Value("${auth0.customNamespace}") String customNamespace,
      final UserService userService,
      final BucketManager bucketManager,
      final @Value("${user.invitationRateLimiting.maxPerMinute}") Long maxPerMinute,
      final @Value("${user.invitationRateLimiting.maxPerDay}") Long maxPerDay) {
    this.decoder = decoder;
    this.customNamespace = customNamespace;
    this.userService = userService;
    this.bucketManager = bucketManager;
    this.maxPerMinute = maxPerMinute;
    this.maxPerDay = maxPerDay;

    buckets = bucketManager.buildBuckets("userInvitationRateLimitingBuckets");
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
    if (isNotTargetRequest(request)) {
      return true;
    }
    final String bearerToken = getTokenFromRequest(request);

    final String userId = getUserIdFromToken(bearerToken);

    if (userService.isUserInvitationCapabilityFrozen(userId)) {
      throw new UserInvitationCapabilityFrozenException("User invitation ability have been frozen.");
    }

    final boolean result = tryConsumeFromUserBucket(userId);
    if (!result) {
      throw new TooManyRequestException("Too many requests");
    }
    return true;
  }

  private boolean isNotTargetRequest(final HttpServletRequest request) {
    return !request.getMethod().equals(HttpMethod.POST.name());
  }

  private String getTokenFromRequest(final HttpServletRequest request) {
    final String bearerToken = request.getHeader(AUTH_HEADER);
    if (StringUtils.isBlank(bearerToken)) {
      throw new ForbiddenException("Missing authorization token");
    }
    return bearerToken.replace("Bearer", "").replace(" ", "");
  }

  private String getUserIdFromToken(final String token) {
    final Jwt jwt = decoder.decode(token);
    final String userId = jwt.getClaimAsString(String.format("%sid", customNamespace));
    if (StringUtils.isBlank(userId)) {
      throw new ForbiddenException("UserId is empty");
    }
    return userId.toUpperCase();
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
    final String bearerToken = getTokenFromRequest(request);
    final String userId = getUserIdFromToken(bearerToken);
    if (response.getStatus() != HttpStatus.OK.value()) {
      giveBackToUserBucket(userId);
      userService.unfreezeUserInvitationCapability(userId);
      return;
    }
    if (isUserShouldBeLocked(userId)) {
      userService.freezeUserInvitationCapability(userId);
    }
  }

  private boolean tryConsumeFromUserBucket(final String userId) {
    final Bucket bucket = buildBucketByUserId(userId);
    return bucket.tryConsume(1);
  }

  private Bucket buildBucketByUserId(final String userId) {
    final List<Bandwidth> bandwidths =
        Arrays.asList(
            bucketManager.buildBandwidth(maxPerMinute, Duration.ofMinutes(1)),
            bucketManager.buildBandwidth(maxPerDay, Duration.ofDays(1)));

    return bucketManager.getBucketFromBuckets(
        userId, buckets, bucketManager.buildBucketConfiguration(bandwidths));
  }

  /**
   * we can't confirm the user should be locked because the token is consumed when a request
   * incoming, and user locking is executed when the request is handle done, so the user would be
   * locked if the bucket have no token.
   */
  private boolean isUserShouldBeLocked(final String userId) {
    final Bucket bucket = buildBucketByUserId(userId);
    return !bucket.estimateAbilityToConsume(1).canBeConsumed();
  }

  private void giveBackToUserBucket(final String userId) {
    final Bucket bucket = buildBucketByUserId(userId);
    bucket.addTokens(1);
  }
}
