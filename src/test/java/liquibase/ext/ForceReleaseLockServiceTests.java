package liquibase.ext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ForceReleaseLockServiceTests {

  private final ForceReleaseLockService forceReleaseLockService = new ForceReleaseLockService();

  @Test
  void testGetPriority() {
    Assertions.assertNotNull(forceReleaseLockService.getPriority());
  }

  @Test
  void testWaitForLock() {
    Assertions.assertThrows(
        NullPointerException.class, () -> forceReleaseLockService.waitForLock());
  }
}
