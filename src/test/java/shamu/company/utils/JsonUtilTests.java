package shamu.company.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.user.entity.User;

public class JsonUtilTests {

  @Test
  void testFormatToString() {
    final User user = new User();
    Assertions.assertDoesNotThrow(() -> JsonUtil.formatToString(user));
    Assertions.assertThrows(
        ForbiddenException.class, () -> JsonUtil.formatToString(new innerClass(1)));
  }

  @Test
  void testDeserialize() {
    final User user = new User();
    final String userString = JsonUtil.formatToString(user);
    Assertions.assertDoesNotThrow(() -> JsonUtil.deserialize(userString, User.class));
    Assertions.assertThrows(ForbiddenException.class, () -> JsonUtil.deserialize("1", User.class));
  }

  private class innerClass {
    int i;

    innerClass(final int i) {
      this.i = i;
    }
  }
}
