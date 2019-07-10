package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "time_off_policy_accrual_schedules")
@Where(clause = "deleted_at IS NULL")
public class TimeOffPolicyAccrualSchedule extends BaseEntity {

  @OneToOne
  private TimeOffPolicy timeOffPolicy;

  private Integer accrualHours;

  private Integer maxBalance;

  private Integer daysBeforeAccrualStarts;

  private Integer carryoverLimit;

  private Timestamp expiredAt;

  @ManyToOne
  private TimeOffAccrualFrequency timeOffAccrualFrequency;

  public TimeOffPolicyAccrualSchedule(TimeOffPolicy timeOffPolicy, Integer accrualHours,
      Integer maxBalance, Integer daysBeforeAccrualStarts,
      Integer carryoverLimit,
      TimeOffAccrualFrequency timeOffAccrualFrequency) {
    this.timeOffPolicy = timeOffPolicy;
    this.accrualHours = accrualHours;
    this.maxBalance = maxBalance;
    this.daysBeforeAccrualStarts = daysBeforeAccrualStarts;
    this.carryoverLimit = carryoverLimit;
    this.timeOffAccrualFrequency = timeOffAccrualFrequency;

  }
}
