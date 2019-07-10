package shamu.company.timeoff.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffBalancePojo {

  private Integer balance;

  private Integer appliedAccumulation;

  private Integer maxBalance;

  private Integer carryOverLimit;

  public TimeOffBalancePojo(Integer balance, Integer appliedAccumulation) {
    this.balance = balance;
    this.appliedAccumulation = appliedAccumulation;
  }

  private void reCalculateBalance() {
    if (this.getAppliedAccumulation() != null) {
      Integer newBalance = this.balance + this.appliedAccumulation;
      this.balance = newBalance;
    }
  }

  public void calculateLatestBalance() {
    reCalculateBalance();
    resetAppliedAccumulation();
  }

  public void resetAppliedAccumulation() {
    this.appliedAccumulation = 0;
  }

  public boolean reachMaxBalance(Boolean withAppliedAccumulation) {
    if (withAppliedAccumulation != null && withAppliedAccumulation) {
      return this.maxBalance != null
          && (this.balance + this.appliedAccumulation) > this.maxBalance;
    }

    return this.maxBalance != null
      && this.balance > this.maxBalance;
  }
}
