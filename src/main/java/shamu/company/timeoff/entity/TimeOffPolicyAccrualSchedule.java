package shamu.company.timeoff.entity;

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

  @ManyToOne
  private TimeOffAccrualFrequency timeOffAccrualFrequency;
}
