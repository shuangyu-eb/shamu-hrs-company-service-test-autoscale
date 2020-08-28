package shamu.company.bucket;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import java.time.Duration;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.stereotype.Component;

@Component
public class BucketManager {

  private final CacheManager manager;

  private final Configuration<String, GridBucketState> config;

  public BucketManager(final Config redissonConfig) {
    manager = Caching.getCachingProvider().getCacheManager();
    final MutableConfiguration<String, GridBucketState> jcacheConfig = new MutableConfiguration<>();
    config = RedissonConfiguration.fromConfig(redissonConfig, jcacheConfig);
  }

  public ProxyManager<String> buildBuckets(final String bucketsName) {
    final Cache<String, GridBucketState> cache = manager.createCache(bucketsName, config);
    return Bucket4j.extension(JCache.class).proxyManagerForCache(cache);
  }

  public Bucket getBucketFromBuckets(
      final String key,
      final ProxyManager<String> buckets,
      final BucketConfiguration bucketConfiguration) {
    return buckets.getProxy(key, bucketConfiguration);
  }

  public BucketConfiguration buildBucketConfiguration(final Bandwidth bandwidth) {
    final ConfigurationBuilder configurationBuilder = Bucket4j.configurationBuilder();
    return configurationBuilder.addLimit(bandwidth).build();
  }


  public BucketConfiguration buildBucketConfiguration(final List<Bandwidth> bandwidths) {
    final ConfigurationBuilder configurationBuilder = Bucket4j.configurationBuilder();
    bandwidths.forEach(configurationBuilder::addLimit);
    return configurationBuilder.build();
  }

  public Bandwidth buildBandwidth(final long capacity, final Duration duration) {
    return Bandwidth.classic(capacity, Refill.intervally(capacity, duration));
  }
}
