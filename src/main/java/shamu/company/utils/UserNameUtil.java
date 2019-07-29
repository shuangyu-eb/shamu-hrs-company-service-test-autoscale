package shamu.company.utils;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

@Component
public class UserNameUtil {

  public static String getUserName(final String firstName, final String middleName,
      final String lastName) {
    final List<String> nameDetails = new ArrayList<>();
    if (!StringUtils.isEmpty(firstName)) {
      nameDetails.add(firstName);
    }
    if (!StringUtils.isEmpty(middleName)) {
      nameDetails.add(middleName);
    }
    if (!StringUtils.isEmpty(lastName)) {
      nameDetails.add(lastName);
    }
    final String userName = String.join(" ", nameDetails.toArray(new String[0]));
    return userName;
  }

  public static String getUserName(final User user) {
    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final String firstName = userPersonalInformation.getFirstName();
    final String middleName = userPersonalInformation.getMiddleName();
    final String lastName = userPersonalInformation.getLastName();
    return getUserName(firstName, middleName, lastName);
  }
}
