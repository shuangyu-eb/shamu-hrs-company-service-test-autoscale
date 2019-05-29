package shamu.company.timeoff.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicy;

@Data
public class TimeOffPolicyPojo {
  private Integer accrualHours;

  private Boolean isLimited;

  private String policyName;

  private Integer startDate;

  @HashidsFormat
  private Long timeOffAccrualFrequency;

  @JSONField(serialize = false)
  public TimeOffPolicy getTimeOffPolicy(Company company) {
    return new TimeOffPolicy(company, this.policyName, this.getIsLimited());
  }
}
