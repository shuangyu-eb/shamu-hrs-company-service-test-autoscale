package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Data
public class TimeOffPolicyAccrualScheduleDto {

  @HashidsFormat
  private Long id;

  private Integer accrualHours;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private Integer daysBeforeAccrualStarts;

  private TimeOffPolicyDto timeOffPolicyDto;

  @HashidsFormat
  private Long timeOffAccrualFrequencyId;

  @JSONField(serialize = false)
  public TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualSchedule(TimeOffPolicy timeOffPolicy,
      Long timeOffAccrualFrequencyId) {
    return new TimeOffPolicyAccrualSchedule(timeOffPolicy, this.getAccrualHours(),
        this.getMaxBalance(),
        this.getDaysBeforeAccrualStarts(), this.getCarryoverLimit(),
        new TimeOffAccrualFrequency(timeOffAccrualFrequencyId));
  }
}
