package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "deactivation_reasons")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
public class DeactivationReasons extends BaseEntity {

  private String name;

  public DeactivationReasons(Long id) {
    this.setId(id);
  }

}