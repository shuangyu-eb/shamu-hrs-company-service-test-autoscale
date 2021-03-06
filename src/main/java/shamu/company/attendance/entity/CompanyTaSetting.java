package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_ta_settings")
public class CompanyTaSetting extends BaseEntity {

  private static final long serialVersionUID = -8716988981910506553L;

  @ManyToOne private StaticTimezone timeZone;

  private int approvalDaysBeforePayroll;

  private String startOfWeek;

  private int messagingOn;

  private int overtimeAlert;

  public enum MessagingON {
    ON(1),
    OFF(0);

    int value;

    private MessagingON(final int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }
}
