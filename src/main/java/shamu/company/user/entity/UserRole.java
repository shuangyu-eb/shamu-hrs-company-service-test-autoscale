package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Entity
@Table(name = "user_roles")
@Data
public class UserRole extends BaseEntity {

  private String name;
}
