package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_off_accrual_frequencies")
@NoArgsConstructor
public class TimeOffAccrualFrequency extends BaseEntity {

  private String name;

  public TimeOffAccrualFrequency(Long id) {
    this.setId(id);
  }
}
