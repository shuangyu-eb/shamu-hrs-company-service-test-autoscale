package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "user_statuses")
@NoArgsConstructor
public class UserStatus extends BaseEntity {

  private String name;

  public UserStatus(final String name) {
    this.name = name;
  }


  public Status getStatus() {
    return Status.valueOf(name);
  }

  public enum Status {
    ACTIVE,
    DISABLED,
    PENDING_VERIFICATION,
    CHANGING_EMAIL_VERIFICATION
  }
}
