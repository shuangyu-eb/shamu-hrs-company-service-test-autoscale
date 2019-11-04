package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "retirement_types")
@NoArgsConstructor
public class RetirementType extends BaseEntity {
  private String name;

  public RetirementType(final Long id) {
    setId(id);
  }
}
