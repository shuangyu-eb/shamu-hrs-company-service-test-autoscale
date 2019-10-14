package shamu.company.user.event;

import lombok.Data;
import shamu.company.user.entity.UserRole;

@Data
public class UserRoleUpdatedEvent {

  private String userId;

  private UserRole userRole;

  public UserRoleUpdatedEvent(final String userId, final UserRole userRole) {
    this.userId = userId;
    this.userRole = userRole;
  }
}
