package shamu.company.job.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "compensation_frequency")
public class CompensationFrequency extends BaseEntity {

  private String name;
}
