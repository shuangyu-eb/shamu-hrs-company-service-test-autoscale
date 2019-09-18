package shamu.company.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Configuration
public class RedisUtil {
  private RedisTemplate redisTemplate;
  private HashOperations hashOperation;
  private ListOperations listOperation;
  private ValueOperations valueOperations;

  @Autowired
  RedisUtil(RedisTemplate<Object, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOperation = redisTemplate.opsForHash();
    this.listOperation = redisTemplate.opsForList();
    this.valueOperations = redisTemplate.opsForValue();
  }

  public void putMap(String redisKey,Object key,Object data) {
    hashOperation.put(redisKey, key, data);
  }

  public Object getMapAsSingleEntry(String redisKey,Object key) {
    return  hashOperation.get(redisKey,key);
  }

  public Map<Object, Object> getMapAsAll(String redisKey) {
    return hashOperation.entries(redisKey);
  }

  public void putValue(String key,Object value) {
    valueOperations.set(key, value);
  }

  public void putValueWithExpireTime(String key, Object value, long timeout, TimeUnit unit) {
    valueOperations.set(key, value, timeout, unit);
  }

  public Object getValue(String key) {
    return valueOperations.get(key);
  }

  public void setExpire(String key,long timeout,TimeUnit unit) {
    redisTemplate.expire(key, timeout, unit);
  }
}
