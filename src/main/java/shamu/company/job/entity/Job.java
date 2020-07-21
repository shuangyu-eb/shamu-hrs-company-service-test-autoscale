package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "jobs")
public class Job extends BaseEntity {

  private static final long serialVersionUID = -1094302624791269383L;
  @Length(max = 100)
  private String title;
}
