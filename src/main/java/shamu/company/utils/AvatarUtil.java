package shamu.company.utils;

import io.micrometer.core.instrument.util.StringUtils;

public abstract class AvatarUtil {

  static double tranColor(final String name, final double min, final double max) {
    if (StringUtils.isBlank(name)) {
      return 0;
    }

    double num = 0;
    final char[] charArr = name.toCharArray();
    for (int index = 0, len = charArr.length; index < len; index++) {
      num += Math.pow(Character.digit(charArr[index], 10) + 0.3, index + 2);
    }

    num = (num % 1000) / 1000;
    return Math.floor(num * (max - min + 1) + min);
  }

  public static String getAvatarBackground(final String name) {

    final double firstColorNum = tranColor(name, 100, 360);
    final double secondColorNum = tranColor(name, 55, 65);
    final double thirdColorNum = tranColor(name, 20, 70);

    return String.format("hsl(%s, %s%%, %s%%)", firstColorNum, secondColorNum, thirdColorNum);
  }
}
