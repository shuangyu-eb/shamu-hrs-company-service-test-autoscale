package shamu.company.utils.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaticTableDeleteTaskChangeTests {

  @Test
  void testGetConfirmationMessage() {
    final StaticTableDeleteTaskChange staticTableDeleteTaskChange =
        new StaticTableDeleteTaskChange();
    Assertions.assertDoesNotThrow(() -> staticTableDeleteTaskChange.getConfirmationMessage());
  }

  @Test
  void testSetUp() {
    final StaticTableDeleteTaskChange staticTableDeleteTaskChange =
        new StaticTableDeleteTaskChange();
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> staticTableDeleteTaskChange.setUp());
  }

  @Test
  void testSetFileOpener() {
    final StaticTableDeleteTaskChange staticTableDeleteTaskChange =
        new StaticTableDeleteTaskChange();
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> staticTableDeleteTaskChange.setFileOpener(null));
  }
}
