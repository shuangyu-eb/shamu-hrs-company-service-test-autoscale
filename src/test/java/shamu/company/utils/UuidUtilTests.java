package shamu.company.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UuidUtilTests {
  @Test
  void testFromBytes() {
    final byte[] bytes = new byte[16];
    Assertions.assertDoesNotThrow(() -> UuidUtil.fromBytes(bytes));
  }

  @Test
  void testToJavaString() {
    final String hexString = "aa-bb-cc";
    Assertions.assertDoesNotThrow(() -> UuidUtil.toJavaString(hexString));
    final String hexString1 = "abcabc";
    Assertions.assertDoesNotThrow(() -> UuidUtil.toJavaString(hexString1));
  }

  @Test
  void testToBytes() {
    final String hexString = "aa-bb-cc";
    Assertions.assertDoesNotThrow(() -> UuidUtil.toBytes(hexString));
  }

  @Test
  void testToHexStringFromString() {
    final String javaString = "123";
    Assertions.assertDoesNotThrow(() -> UuidUtil.toHexString(javaString));
  }

  @Test
  void testToHexStringFromBytes() {
    final byte[] bytes = new byte[16];
    Assertions.assertDoesNotThrow(() -> UuidUtil.toHexString(bytes));
  }

  @Test
  void testGetUuidString() {
    Assertions.assertDoesNotThrow(() -> UuidUtil.getUuidString());
  }
}
