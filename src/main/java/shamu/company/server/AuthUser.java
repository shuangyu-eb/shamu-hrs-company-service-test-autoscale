package shamu.company.server;

import lombok.Data;
import shamu.company.user.entity.User;

@Data
public class AuthUser {

  private Long id;

  private String email;

  private String imageUrl;

  private Long companyId;

  AuthUser(User user) {
    this.id = user.getId();
    this.imageUrl = user.getImageUrl();
    this.email = user.getUserContactInformation().getEmailWork();
    this.companyId = user.getCompany().getId();
  }
}
