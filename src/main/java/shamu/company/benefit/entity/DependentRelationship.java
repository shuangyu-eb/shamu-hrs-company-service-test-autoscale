package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_dependent_relationships")
@Where(clause = "deleted_at IS NULL")
public class DependentRelationship extends BaseEntity {
  private String name;
}
