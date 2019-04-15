package shamu.company.employee;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "employment_types")
@Where(clause = "deleted_at IS NULL")
public class EmploymentType extends BaseEntity {

  private String name;
}
