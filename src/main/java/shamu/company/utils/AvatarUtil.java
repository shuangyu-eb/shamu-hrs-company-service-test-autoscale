package shamu.company.utils;

import org.apache.commons.lang.StringUtils;

public abstract class AvatarUtil {

  private AvatarUtil() {}

  private static float tranColor(final String name, final double min, final double max) {
    if (StringUtils.isBlank(name)) {
      return 0;
    }

    double num = 0;
    final char[] charArr = name.toCharArray();
    for (int index = 0, len = charArr.length; index < len; index++) {

      final int charCode = Character.codePointAt(charArr, index);
      final double tmpValue = Math.pow(charCode + 0.3, index + 2d);
      num += tmpValue;
    }

    num = (num % 1000) / 1000;
    return (float) Math.floor(num * (max - min + 1) + min);
  }

  /**
   * Refer https://en.wikipedia.org/wiki/HSL_and_HSV
   *
   * @param hue [0, 360]
   * @param saturation [0, 1]
   * @param lightness [0, 1]
   * @return [red, green, blue]
   */
  private static float[] hslToRgb(final int hue, final float saturation, final float lightness) {
    final float chroma = (1 - Math.abs(2 * lightness - 1)) * saturation;
    final float huePart = hue / 60f;
    final float secondColor = chroma * (1 - Math.abs(huePart % 2 - 1));
    float baseRed = 0;
    float baseGreen = 0;
    float baseBlue = 0;
    final int ceilValue = (int) Math.ceil(huePart);
    switch (ceilValue) {
      case 1:
        baseRed = chroma;
        baseGreen = secondColor;
        break;
      case 2:
        baseRed = secondColor;
        baseGreen = chroma;
        break;
      case 3:
        baseGreen = chroma;
        baseBlue = secondColor;
        break;
      case 4:
        baseGreen = secondColor;
        baseBlue = chroma;
        break;
      case 5:
        baseRed = secondColor;
        baseBlue = chroma;
        break;
      case 6:
        baseRed = chroma;
        baseBlue = secondColor;
        break;
      default:
        break;
    }
    final float minorColor = lightness - chroma / 2;
    return new float[] {baseRed + minorColor, baseGreen + minorColor, baseBlue + minorColor};
  }

  public static String getAvatarBackground(final String name) {

    final float hue = tranColor(name, 100, 360);
    final float saturation = tranColor(name, 55, 65);
    final float lightness = tranColor(name, 20, 70);

    final float saturationRatio = saturation / 100f;
    final float lightnessRatio = lightness / 100f;
    final float[] rgb = hslToRgb((int) hue, saturationRatio, lightnessRatio);
    return String.format(
        "rgb(%s, %s, %s)",
        Math.round(rgb[0] * 255), Math.round(rgb[1] * 255), Math.round(rgb[2] * 255));
  }

  public static String getAvatarShortName(final String firstName, final String lastName) {
    if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
      return "";
    }

    return firstName.substring(0,1).toUpperCase() + lastName.substring(0,1).toUpperCase();
  }
}
