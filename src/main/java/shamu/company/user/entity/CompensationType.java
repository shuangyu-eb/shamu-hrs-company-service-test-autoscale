package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "compensation_types")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CompensationType extends BaseEntity {

  @ManyToOne private Company company;

  private String name;

  public CompensationType(Long compensationId) {
    this.setId(compensationId);
  }
}
