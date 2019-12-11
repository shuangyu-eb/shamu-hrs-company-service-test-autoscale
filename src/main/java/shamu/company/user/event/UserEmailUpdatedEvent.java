package shamu.company.user.event;

import lombok.Data;

@Data
public class UserEmailUpdatedEvent {

  private final String userId;

  private final String email;

  public UserEmailUpdatedEvent(final String userId, final String email) {
    this.userId = userId;
    this.email = email;
  }
}
