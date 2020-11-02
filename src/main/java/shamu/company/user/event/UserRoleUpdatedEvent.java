package shamu.company.user.event;

import lombok.Data;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;

@Data
public class UserRoleUpdatedEvent {

  private User user;

  private UserRole userRole;

  public UserRoleUpdatedEvent(final User user, final UserRole userRole) {
    this.user = user;
    this.userRole = userRole;
  }
}
