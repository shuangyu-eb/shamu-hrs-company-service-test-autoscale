package shamu.company.utils;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserNameUtil {

  public static String getUserName(String firstName, String middleName, String lastName) {
    List<String> nameDetails = new ArrayList<>();
    if (!StringUtils.isEmpty(firstName)) {
      nameDetails.add(firstName);
    }
    if (!StringUtils.isEmpty(middleName)) {
      nameDetails.add(middleName);
    }
    if (!StringUtils.isEmpty(lastName)) {
      nameDetails.add(lastName);
    }
    String userName = String.join(" ", nameDetails.toArray(new String[0]));
    return userName;
  }
}
