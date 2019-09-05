package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "time_off_policies")
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffPolicy extends BaseEntity {

  @ManyToOne private Company company;

  @Length(max = 50)
  private String name;

  private Boolean isLimited;

  public TimeOffPolicy(Long id) {
    this.setId(id);
  }
}
