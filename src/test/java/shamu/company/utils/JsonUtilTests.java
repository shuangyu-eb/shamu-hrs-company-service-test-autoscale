package shamu.company.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import shamu.company.user.entity.User;
import shamu.company.utils.exception.DeserializeFailedException;
import shamu.company.utils.exception.SerializeFailedException;

public class JsonUtilTests {

  @Test
  void testFormatToString() {
    final User user = new User();
    assertThatCode(() -> JsonUtil.formatToString(user)).doesNotThrowAnyException();
    assertThatExceptionOfType(SerializeFailedException.class)
        .isThrownBy(() -> JsonUtil.formatToString(new innerClass(1)));
  }

  @Test
  void testDeserialize() {
    final User user = new User();
    final String userString = JsonUtil.formatToString(user);
    assertThatCode(() -> JsonUtil.deserialize(userString, User.class)).doesNotThrowAnyException();
    assertThatExceptionOfType(DeserializeFailedException.class)
        .isThrownBy(() -> JsonUtil.deserialize("1", User.class));
  }

  private class innerClass {
    int i;

    innerClass(final int i) {
      this.i = i;
    }
  }

  @Test
  void testDeserializeType() {
    final List<String> randomList = Collections.singletonList("1");
    final String jsonString = JsonUtil.formatToString(randomList);
    final List<String> result =
        JsonUtil.deserializeType(jsonString, new TypeReference<List<String>>() {});
    assertThat(result.get(0)).isEqualTo(randomList.get(0));
  }
}
