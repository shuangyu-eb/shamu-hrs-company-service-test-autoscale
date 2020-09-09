package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Entity
@Data
@Table(name = "jobs")
public class Job extends BaseEntity {

  @Length(max = 100)
  private String title;

  @ManyToOne private Company company;
}
