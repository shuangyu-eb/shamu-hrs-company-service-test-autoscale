package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "time_off_policies")
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffPolicy extends BaseEntity {

  @ManyToOne
  private Company company;

  private String name;

  private Boolean isLimited;
}
