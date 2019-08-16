package shamu.company.server;

import lombok.Data;
import shamu.company.user.entity.User;

@Data
public class CompanyUser {

  private Long id;

  private String name;

  private String imageUrl;

  CompanyUser(User user) {
    this.id = user.getId();
    this.name = user.getUserPersonalInformation().getName();
    this.imageUrl = user.getImageUrl();
  }
}