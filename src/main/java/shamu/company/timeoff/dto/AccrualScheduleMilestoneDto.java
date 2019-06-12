package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Data
public class AccrualScheduleMilestoneDto {

  private Integer anniversaryYear;

  @HashidsFormat
  private Long timeOffPolicyAccrualScheduleId;

  private Integer accrualHours;

  private Integer accrualInterval;

  private Integer carryoverLimit;

  private Integer maxBalance;

  private String name;

  @JSONField(serialize = false)
  public AccrualScheduleMilestone getAccrualScheduleMilestone(Long timeOffPolicyAccrualScheduleId) {
    AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();

    accrualScheduleMilestone.setAccrualHours(this.getAccrualHours());
    accrualScheduleMilestone.setAccrualInterval(this.getAccrualInterval());
    accrualScheduleMilestone.setAnniversaryYear(this.getAnniversaryYear());
    accrualScheduleMilestone.setCarryoverLimit(this.getCarryoverLimit());
    accrualScheduleMilestone.setMaxBalance(this.getMaxBalance());
    accrualScheduleMilestone.setName(this.getName());
    accrualScheduleMilestone.setTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualScheduleId);

    return accrualScheduleMilestone;
  }

  public AccrualScheduleMilestone updateAccrualScheduleMilestone(
      AccrualScheduleMilestone origin,Long timeOffPolicyAccrualScheduleId) {

    origin.setAccrualHours(this.getAccrualHours());
    origin.setAccrualInterval(this.getAccrualInterval());
    origin.setAnniversaryYear(this.getAnniversaryYear());
    origin.setCarryoverLimit(this.getCarryoverLimit());
    origin.setMaxBalance(this.getMaxBalance());
    origin.setName(this.getName());
    origin.setDeletedAt(null);
    origin.setTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualScheduleId);

    return origin;
  }
}
