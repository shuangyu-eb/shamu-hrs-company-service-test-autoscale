package shamu.company.helpers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.client.auth.AuthAPI;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shamu.company.helpers.auth0.Auth0Config;

class Auth0ConfigTest {

  @Test
  void addTest() throws DecoderException, UnsupportedEncodingException {
    //    byte[] a = "hello world".getBytes();
    //    System.out.println(a);

    final String randomUUIID = UUID.randomUUID().toString().replaceAll("-", "");
    final byte[] bytes = Hex.decodeHex(randomUUIID.toCharArray());
    final String convertedString = new String(bytes, "UTF-8");
    final byte[] revertedBytes = convertedString.getBytes("UTF-8");
    final String revertedHexString = Hex.encodeHexString(revertedBytes);
    final String revertedHexString2 = Hex.encodeHexString(bytes);

    final String randomUUID2 = UUID.randomUUID().toString().replaceAll("-", "");
    String str = "";
    for (int i = 0; i < randomUUID2.length(); i += 2) {
      final String s = randomUUID2.substring(i, (i + 2));
      final int decimal = Integer.parseInt(s, 16);
      str = str + (char) decimal;
    }
    final String convertedString2 = str;

    final char[] chars = convertedString2.toCharArray();
    final StringBuilder hex = new StringBuilder();
    for (final char ch : chars) {
      hex.append(Integer.toHexString((int) ch));
    }

    final String revertedString = hex.toString();
  }

  @Nested
  class GetAuthApi {

    @Test
    void whenConfigParamsIsLack() {
      final Auth0Config auth0Config = new Auth0Config();
      assertThrows(IllegalArgumentException.class, () -> auth0Config.getAuthApi());
    }

    @Test
    void whenConfigParamsIsNotLack() {
      final Auth0Config auth0Config = new Auth0Config();
      auth0Config.setClientId("clientId");
      auth0Config.setClientSecret("clientSecret");
      auth0Config.setDomain("test.auth0.com");
      auth0Config.setManagementIdentifier("verified");
      auth0Config.setJwks("https://indeed.com/");
      auth0Config.setDatabase("database");
      auth0Config.setIssuer("issuer");
      auth0Config.setAudience("audience");
      auth0Config.setCustomNamespace("customNamespace");
      final AuthAPI authAPI = auth0Config.getAuthApi();
      assertNotNull(authAPI);
      final String res = auth0Config.toString();
      assertNotNull(res);
      final boolean equal = auth0Config.equals(new Object());
      assertFalse(equal);
      assertNotEquals(auth0Config, new Auth0Config());
    }

    @Test
    void testBuilder() {
      String auth0Config =
          Auth0Config.builder()
              .clientId("clientId")
              .domain("domain")
              .clientSecret("clientSecret")
              .issuer("issuer")
              .jwks("jws")
              .audience("audience")
              .managementIdentifier("managementIdentifier")
              .customNamespace("customNamespace")
              .database("database")
              .build()
              .toString();
      assertNotNull(auth0Config);
    }
  }
}
