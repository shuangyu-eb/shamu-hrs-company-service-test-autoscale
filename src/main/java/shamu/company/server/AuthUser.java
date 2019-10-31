package shamu.company.server;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.s3.PreSinged;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
public class AuthUser {

  private Long id;

  private String email;

  @PreSinged
  private String imageUrl;

  private Long companyId;

  private String userId;

  private List<String> permissions;

  AuthUser(final User user) {
    this.id = user.getId();
    this.imageUrl = user.getImageUrl();
    this.email = user.getUserContactInformation().getEmailWork();
    this.companyId = user.getCompany().getId();
    this.userId = user.getUserId();
  }
}
