package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
public class UserRoleAndStatusInfoDto {
  private String userRole;
  private String userStatus;

  public UserRoleAndStatusInfoDto(final User user) {
    this.userRole = user.getUserRole().getName();
    this.userStatus = user.getUserStatus().getName();
  }
}
