package shamu.company.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import shamu.company.helpers.RedisHelper;
import shamu.company.server.dto.AuthUser;
import shamu.company.utils.JsonUtil;

@Configuration
public class AuthUserCacheManager {

  private static final TimeUnit EXPIRATION_UNIT = TimeUnit.SECONDS;
  private final RedisHelper redisHelper;
  @Value("${user.cached.expiration}")
  private Long expiration;

  @Autowired
  public AuthUserCacheManager(final RedisHelper redisHelper) {
    this.redisHelper = redisHelper;
  }

  public void cacheAuthUser(final String key, final AuthUser authUser) {
    final String authUserJson = JsonUtil.formatToString(authUser);
    redisHelper.putValueWithExpireTime(key, authUserJson, expiration, EXPIRATION_UNIT);
  }

  public AuthUser getCachedUser(final String key) {
    final String jsonAuthUser = (String) redisHelper.getValue(key);
    return jsonAuthUser == null ? null : JsonUtil.deserialize(jsonAuthUser, AuthUser.class);
  }
}
