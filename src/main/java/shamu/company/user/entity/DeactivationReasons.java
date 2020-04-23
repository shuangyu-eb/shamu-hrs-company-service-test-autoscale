package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "deactivation_reasons")
@NoArgsConstructor
public class DeactivationReasons extends BaseEntity {

  private String name;

  public DeactivationReasons(final String id) {
    setId(id);
  }
}
