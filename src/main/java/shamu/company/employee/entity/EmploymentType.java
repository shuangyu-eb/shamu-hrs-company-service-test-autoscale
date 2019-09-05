package shamu.company.employee.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "employment_types")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentType extends BaseEntity {

  @Length(max = 30)
  private String name;

  @ManyToOne
  @JSONField(serialize = false)
  private Company company;

  public EmploymentType(Long employmentTypeId) {
    this.setId(employmentTypeId);
  }

  public EmploymentType(String name) {
    this.setName(name);
  }
}
