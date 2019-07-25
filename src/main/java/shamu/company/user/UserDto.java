package shamu.company.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UserNameUtil;

@NoArgsConstructor
@Data
public class UserDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String email;

  private String avatar;

  public UserDto(final User user) {
    setAvatar(user.getImageUrl());
    setId(user.getId());
    setEmail(user.getEmailWork());

    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    if (null != userPersonalInformation) {
      final String firstName = userPersonalInformation.getFirstName();
      final String middleName = userPersonalInformation.getMiddleName();
      final String lastName = userPersonalInformation.getLastName();
      setName(UserNameUtil.getUserName(firstName, middleName, lastName));
    }
  }
}
