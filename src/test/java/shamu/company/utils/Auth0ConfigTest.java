package shamu.company.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.client.auth.AuthAPI;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Auth0ConfigTest {

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
      final AuthAPI authAPI = auth0Config.getAuthApi();
      assertNotNull(authAPI);
    }
  }

  @Test
  void addTest() throws DecoderException, UnsupportedEncodingException {
//    byte[] a = "hello world".getBytes();
//    System.out.println(a);

    String randomUUIID = UUID.randomUUID().toString().replaceAll("-", "");
    byte[] bytes = Hex.decodeHex(randomUUIID.toCharArray());
    String convertedString = new String(bytes, "UTF-8");
    byte[] revertedBytes = convertedString.getBytes("UTF-8");
    String revertedHexString = Hex.encodeHexString(revertedBytes);
    String revertedHexString2 = Hex.encodeHexString(bytes);

    String randomUUID2 = UUID.randomUUID().toString().replaceAll("-", "");
    String str = "";
    for(int i=0;i<randomUUID2.length();i+=2)
    {
      String s = randomUUID2.substring(i, (i + 2));
      int decimal = Integer.parseInt(s, 16);
      str = str + (char) decimal;
    }
    String convertedString2 = str;

    char[] chars = convertedString2.toCharArray();
    StringBuilder hex = new StringBuilder();
    for (char ch : chars) {
      hex.append(Integer.toHexString((int) ch));
    }

    String revertedString = hex.toString();
  }


}
