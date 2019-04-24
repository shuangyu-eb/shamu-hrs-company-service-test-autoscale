package shamu.company.company.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "departments")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

  @ManyToOne
  private Company company;

  private String name;

  public Department(Long departmentId) {
    this.setId(departmentId);
  }
}
