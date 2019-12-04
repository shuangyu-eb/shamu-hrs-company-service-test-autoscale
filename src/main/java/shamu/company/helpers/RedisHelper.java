package shamu.company.helpers;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisHelper {
  private final RedisTemplate redisTemplate;
  private final HashOperations hashOperation;
  private final ValueOperations valueOperations;

  @Autowired
  RedisHelper(final RedisTemplate<Object, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    hashOperation = redisTemplate.opsForHash();
    valueOperations = redisTemplate.opsForValue();
  }

  public void putMap(final String redisKey, final Object key, final Object data) {
    hashOperation.put(redisKey, key, data);
  }

  public Object getMapAsSingleEntry(final String redisKey, final Object key) {
    return  hashOperation.get(redisKey,key);
  }

  public Map<Object, Object> getMapAsAll(final String redisKey) {
    return hashOperation.entries(redisKey);
  }

  public void putValue(final String key, final Object value) {
    valueOperations.set(key, value);
  }

  public void putValueWithExpireTime(
      final String key, final Object value, final long timeout, final TimeUnit unit) {
    valueOperations.set(key, value, timeout, unit);
  }

  public Object getValue(final String key) {
    return valueOperations.get(key);
  }

  public void setExpire(final String key, final long timeout, final TimeUnit unit) {
    redisTemplate.expire(key, timeout, unit);
  }
}
