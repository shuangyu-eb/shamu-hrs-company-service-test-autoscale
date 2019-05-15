package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "compensation_frequency")
@Where(clause = "deleted_at IS NULL")
public class CompensationFrequency extends BaseEntity {

  private String name;

}
