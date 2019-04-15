package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "user_roles")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserRole extends BaseEntity {

  private String name;

  public UserRole(String name) {
    this.name = name;
  }
}
