package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Entity
@Data
@Table(name = "compensation_frequency")
@Where(clause = "deleted_at IS NULL")
public class CompensationFrequency extends BaseEntity {

  @ManyToOne
  private Company company;

  private String name;

}