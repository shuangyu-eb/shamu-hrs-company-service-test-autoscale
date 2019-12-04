package shamu.company.redis;

import com.alibaba.fastjson.JSON;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import shamu.company.helpers.RedisHelper;
import shamu.company.server.AuthUser;


@Configuration
public class AuthUserCacheManager {

  private static final TimeUnit EXPIRATION_UNIT = TimeUnit.SECONDS;

  @Value("${spring.redis.expiration}")
  private Long expiration;

  private final RedisHelper redisHelper;

  @Autowired
  public AuthUserCacheManager(final RedisHelper redisHelper) {
    this.redisHelper = redisHelper;
  }

  public void cacheAuthUser(final String key, final AuthUser authUser) {
    final String authUserJson = JSON.toJSONString(authUser);
    redisHelper.putValueWithExpireTime(key, authUserJson, expiration, EXPIRATION_UNIT);

  }

  public AuthUser getCachedUser(final String key) {
    final String jsonAuthUser = (String) redisHelper.getValue(key);
    return jsonAuthUser == null ? null : JSON.parseObject(jsonAuthUser, AuthUser.class);
  }
}
