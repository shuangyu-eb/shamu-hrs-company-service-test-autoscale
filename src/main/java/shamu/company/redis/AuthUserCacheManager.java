package shamu.company.redis;

import com.alibaba.fastjson.JSON;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import shamu.company.server.AuthUser;
import shamu.company.utils.RedisUtil;


@Configuration
public class AuthUserCacheManager {

  private static final TimeUnit EXPIRATION_UNIT = TimeUnit.SECONDS;

  @Value("${spring.redis.expiration}")
  private Long expiration;

  private RedisUtil redisUtil;

  @Autowired
  public AuthUserCacheManager(RedisUtil redisUtil) {
    this.redisUtil = redisUtil;
  }

  public void cacheAuthUser(String key, AuthUser authUser) {
    String authUserJson = JSON.toJSONString(authUser);
    redisUtil.putValueWithExpireTime(key, authUserJson, expiration, EXPIRATION_UNIT);

  }

  public AuthUser getCachedUser(String key) {
    String jsonAuthUser = (String) redisUtil.getValue(key);
    return jsonAuthUser == null ? null : JSON.parseObject(jsonAuthUser, AuthUser.class);
  }
}
