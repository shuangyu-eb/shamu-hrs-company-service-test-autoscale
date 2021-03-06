package shamu.company.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
  @Value("${spring.redis.host}")
  private String redisHostname;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Bean
  protected JedisConnectionFactory jedisConnectionFactory() {
    final RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration(redisHostname, redisPort);

    final JedisClientConfiguration jedisClientConfiguration =
        JedisClientConfiguration.builder().usePooling().build();

    final JedisConnectionFactory factory =
        new JedisConnectionFactory(configuration, jedisClientConfiguration);

    factory.afterPropertiesSet();
    return factory;
  }

  @Bean
  public RedisTemplate<Object, Object> redisTemplate() {
    final Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer(Object.class);
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
    final RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new GenericToStringSerializer<>(Object.class));
    redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
    redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
    redisTemplate.setConnectionFactory(jedisConnectionFactory());
    return redisTemplate;
  }

  @Bean
  public Config redissonConfig() {
    final Config config = new Config();
    config.useSingleServer().setAddress(String.format("redis://%s:%s", redisHostname, redisPort));
    return config;
  }
}
