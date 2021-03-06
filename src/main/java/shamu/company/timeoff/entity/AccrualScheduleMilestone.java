package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_off_policy_accrual_schedule_milestones")
@NoArgsConstructor
public class AccrualScheduleMilestone extends BaseEntity {

  private Integer anniversaryYear;

  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String timeOffPolicyAccrualScheduleId;

  private Integer accrualHours;

  private Integer accrualInterval;

  private Integer carryoverLimit;

  private Integer maxBalance;

  private String name;

  private Timestamp expiredAt;
}
