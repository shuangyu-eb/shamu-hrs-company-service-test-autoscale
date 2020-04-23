package shamu.company.utils;

import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AvatarUtilTests {

  @Test
  void testGetAvatarBackground() {

    String backgroundColor = AvatarUtil.getAvatarBackground("");
    String backgroundColor1 = AvatarUtil.getAvatarBackground("l");
    Assertions.assertThat(
            Pattern.matches("rgb\\(\\d{1,3}\\s*, \\d{1,3}\\s*, \\d{1,3}\\s*\\)", backgroundColor))
        .isTrue();
    Assertions.assertThat(
            Pattern.matches("rgb\\(\\d{1,3}\\s*, \\d{1,3}\\s*, \\d{1,3}\\s*\\)", backgroundColor1))
        .isTrue();
  }

  @Test
  void whenFirstNameIsEmpty_thenReturnEmpty() {
    final String shortName = AvatarUtil.getAvatarShortName("", "A");
    Assertions.assertThat(shortName).isEmpty();
  }

  @Test
  void whenLastNameIsEmpty_thenReturnEmpty() {
    final String shortName = AvatarUtil.getAvatarShortName("vd", "");
    Assertions.assertThat(shortName).isEmpty();
  }

  @Test
  void whenFirstNameAndLastNameValid_thenReturnShortName() {
    final String shortName = AvatarUtil.getAvatarShortName("vd", "max");
    Assertions.assertThat(shortName).isNotEmpty();
  }
}
