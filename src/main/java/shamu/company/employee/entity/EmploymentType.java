package shamu.company.employee.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "employment_types")
@Where(clause = "deleted_at IS NULL")
public class EmploymentType extends BaseEntity {

  private String name;

  @ManyToOne
  @JsonIgnore
  private Company company;
}