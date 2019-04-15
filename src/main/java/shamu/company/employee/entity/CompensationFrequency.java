package shamu.company.employee.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
public class CompensationFrequency extends BaseEntity {

  @ManyToOne
  private Company company;

  private String name;

}
