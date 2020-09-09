package shamu.company.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.lang.StringUtils;

public interface Base64Utils {

  static String decode(final String str) {
    return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
  }

  static String decodeCompanyId(final String companyId) {
    return StringUtils.reverse(decode(companyId));
  }
}
