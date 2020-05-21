package liquibase.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class ForceReleaseLockServiceTests {

  private final ForceReleaseLockService forceReleaseLockService = new ForceReleaseLockService();

  @Test
  void testGetPriority() {
    assertThat(forceReleaseLockService.getPriority()).isNotNull();
  }

  @Test
  void testWaitForLock() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> forceReleaseLockService.waitForLock());
  }
}
