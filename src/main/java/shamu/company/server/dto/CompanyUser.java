package shamu.company.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
public class CompanyUser {

  private String id;

  private String email;

  private String name;

  private String imageUrl;

  public CompanyUser(final User user) {
    id = user.getId();
    name = user.getUserPersonalInformation().getName();
    imageUrl = user.getImageUrl();
    email = user.getUserContactInformation().getEmailWork();
  }
}
