package shamu.company.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

@Component
public class UserNameUtil {

  public static String getUserName(final User user) {
    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    return getUserName(userPersonalInformation);
  }

  public static String getUserName(final UserPersonalInformation userPersonalInformation) {
    final String firstName = userPersonalInformation.getFirstName();
    final String preferredName = userPersonalInformation.getPreferredName();
    final String lastName = userPersonalInformation.getLastName();
    return StringUtils.isEmpty(preferredName)
            ? firstName.concat(" ").concat(lastName)
            : preferredName.concat(" ").concat(lastName);
  }
}
