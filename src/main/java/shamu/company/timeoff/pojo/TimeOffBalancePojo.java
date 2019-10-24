package shamu.company.timeoff.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffBalancePojo {

  private Integer balance;

  private Integer maxBalance;

  private Integer carryOverLimit;

  public TimeOffBalancePojo(Integer balance) {
    this.balance = balance;
  }

  public boolean reachMaxBalance() {
    return this.maxBalance != null
      && this.balance > this.maxBalance;
  }
}
