package shamu.company.utils;

import java.util.UUID;

public interface UuidUtil {

  static UUID fromBytes(final byte[] id) {
    long mostSigBits = 0;
    long leastSigBits = 0;
    for (int i = 0; i < 8; i++) {
      mostSigBits = (mostSigBits << 8) | (id[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      leastSigBits = (leastSigBits << 8) | (id[i] & 0xff);
    }
    return new UUID(mostSigBits, leastSigBits);
  }

  static String toJavaString(final String hexString) {
    String targetString = hexString;
    if (hexString.contains("-")) {
      targetString = hexString.replace("-", "");
    }

    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < targetString.length(); i += 2) {
      final String s = targetString.substring(i, (i + 2));
      final int decimal = Integer.parseInt(s, 16);
      result.append((char) decimal);
    }
    return result.toString();
  }

  static byte[] toBytes(final String hexString) {
    String targetString = hexString;
    if (hexString.contains("-")) {
      targetString = hexString.replace("-", "");
    }

    final int stringLen = targetString.length();
    final byte[] data = new byte[stringLen / 2];
    for (int i = 0; i < stringLen; i += 2) {
      data[i / 2] =
          (byte)
              ((Character.digit(targetString.charAt(i), 16) << 4)
                  + Character.digit(targetString.charAt(i + 1), 16));
    }
    return data;
  }

  static String toHexString(final String javaString) {

    final char[] chars = javaString.toCharArray();
    final StringBuilder result = new StringBuilder();
    for (final char ch : chars) {
      result.append(Integer.toHexString((int) ch));
    }
    return result.toString();
  }

  static String toHexString(final byte[] id) {
    return fromBytes(id).toString().toUpperCase().replace("-", "");
  }

  static String getUuidString() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
