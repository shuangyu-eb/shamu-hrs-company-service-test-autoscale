package shamu.company.employee.event;

import com.auth0.json.mgmt.users.User;
import lombok.Getter;
import lombok.Setter;

public class Auth0UserCreatedEvent {

  @Setter @Getter private User user;

  public Auth0UserCreatedEvent(final User createdUser) {
    this.user = createdUser;
  }
}
