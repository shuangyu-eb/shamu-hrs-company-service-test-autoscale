package liquibase.ext;

import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ForceReleaseLockServiceTests {

  private final ForceReleaseLockService forceReleaseLockService = new ForceReleaseLockService();

  @Test
  void testGetPriority() {
    Assertions.assertNotNull(forceReleaseLockService.getPriority());
  }

  @Test
  void testWaitForLock() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> forceReleaseLockService.waitForLock()
    );
  }
}
