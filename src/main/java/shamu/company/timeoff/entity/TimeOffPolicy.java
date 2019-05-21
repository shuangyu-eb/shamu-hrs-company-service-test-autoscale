package shamu.company.timeoff.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_off_policies")
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffPolicy extends BaseEntity {

  @OneToOne(cascade = CascadeType.PERSIST)
  private TimeOffType timeOffType;

  private Boolean isLimited;
}
