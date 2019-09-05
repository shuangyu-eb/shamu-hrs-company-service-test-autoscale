package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Department;

@Entity
@Data
@Table(name = "jobs")
@Where(clause = "deleted_at IS NULL")
public class Job extends BaseEntity {

  @Length(max = 100)
  private String title;

  @ManyToOne
  private Department department;
}
