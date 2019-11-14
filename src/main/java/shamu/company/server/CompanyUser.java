package shamu.company.server;

import lombok.Data;
import shamu.company.user.entity.User;

@Data
public class CompanyUser {

  private String id;

  private String email;

  private String name;

  private String imageUrl;

  CompanyUser(final User user) {
    this.id = user.getId();
    this.name = user.getUserPersonalInformation().getName();
    this.imageUrl = user.getImageUrl();
    this.email = user.getUserContactInformation().getEmailWork();
  }
}
