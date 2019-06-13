package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "retirement_types")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class RetirementType extends BaseEntity {
  private String name;

  public RetirementType(Long id) {
    this.setId(id);
  }
}
