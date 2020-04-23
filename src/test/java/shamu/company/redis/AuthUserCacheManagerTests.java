package shamu.company.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import shamu.company.helpers.RedisHelper;
import shamu.company.server.dto.AuthUser;

class AuthUserCacheManagerTests {

  RedisHelper redisHelper;
  private AuthUserCacheManager authUserCacheManager;

  @BeforeEach
  void setUp() {
    redisHelper = Mockito.mock(RedisHelper.class);
    authUserCacheManager = new AuthUserCacheManager(redisHelper);
    Whitebox.setInternalState(authUserCacheManager, "expiration", 36000L);
  }

  @Test
  void testCacheAuthUser() {
    AuthUser authUser = new AuthUser();
    authUser.setId("1");
    Assertions.assertDoesNotThrow(
        () -> {
          authUserCacheManager.cacheAuthUser("1", authUser);
        });
  }

  @Test
  void testGetCachedUser() {
    Assertions.assertDoesNotThrow(
        () -> {
          authUserCacheManager.getCachedUser("1");
        });
  }
}
