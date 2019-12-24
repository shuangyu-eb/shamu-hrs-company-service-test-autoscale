package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_dependent_relationships")
@NoArgsConstructor
public class DependentRelationship extends BaseEntity {
  private String name;

  public DependentRelationship(final String id) {
    this.setId(id);
  }
}
