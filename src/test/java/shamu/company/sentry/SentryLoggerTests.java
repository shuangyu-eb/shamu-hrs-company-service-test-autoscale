package shamu.company.sentry;

import com.auth0.json.mgmt.users.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import shamu.company.common.ApplicationConfig;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.email.service.EmailService;
import shamu.company.employee.event.Auth0UserCreatedEvent;
import shamu.company.user.entity.UserRole;
import shamu.company.user.event.UserRoleUpdatedEvent;

public class SentryLoggerTests {

  private Logger logger;
  private SentryLogger sentryLogger;

  @BeforeEach
  void init() {
    logger = Mockito.mock(Logger.class);
    sentryLogger = new SentryLogger(EmailService.class);
    Whitebox.setInternalState(sentryLogger,"log",logger);
  }

  @Test
  void testDebug() {
    Assertions.assertDoesNotThrow(() -> sentryLogger.debug("message"));
  }

  @Test
  void testDebugException() {
    Assertions.assertDoesNotThrow(() -> sentryLogger.debug("message",new Exception()));
  }

  @Test
  void testInfoException() {
    Assertions.assertDoesNotThrow(() -> sentryLogger.info("message", new Exception()));
  }

  @Test
  void testWarn() {
    Assertions.assertDoesNotThrow(() -> sentryLogger.warn("message"));
  }

  @Test
  void testWarnInfo() {
    Assertions.assertDoesNotThrow(() -> sentryLogger.warn("message", new Exception()));
  }

  @Test
  void testSomeDto() {
    final CommonDictionaryDto commonDictionaryDto = new CommonDictionaryDto();
    final ApplicationConfig config = new ApplicationConfig();
    final UserRoleUpdatedEvent userRoleUpdatedEvent = new UserRoleUpdatedEvent("userId",new UserRole());
    final Auth0UserCreatedEvent auth0UserCreatedEvent = new Auth0UserCreatedEvent(new User());
    commonDictionaryDto.setId("1");
    commonDictionaryDto.setName("name");
    config.setFrontEndAddress("front");
    config.setHelpUrl("helpUrl");
    config.setSystemEmailAddress("address");
    userRoleUpdatedEvent.getUserId();
    userRoleUpdatedEvent.getUserRole();
    auth0UserCreatedEvent.setUser(new User());
    Assertions.assertNotNull(config.toString());
    Assertions.assertNotNull(commonDictionaryDto.toString());
    Assertions.assertNotNull(auth0UserCreatedEvent.toString());
  }
}
