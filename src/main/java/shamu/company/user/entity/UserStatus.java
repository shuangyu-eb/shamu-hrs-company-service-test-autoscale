package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "user_statuses")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserStatus extends BaseEntity {

  private String name;

  public UserStatus(String name) {
    this.name = name;
  }


  public Status getStatus() {
    return Status.valueOf(this.name);
  }

  public enum Status {
    ACTIVE,
    DISABLED,
    PENDING_VERIFICATION
  }
}
