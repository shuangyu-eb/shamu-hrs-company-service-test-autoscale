package shamu.company.user.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "compensation_overtime_statuses")
public class CompensationOvertimeStatus extends BaseEntity {

  private static final long serialVersionUID = -6658865223656741006L;
  private String name;

  public enum OvertimeStatus {
    NOT_ELIGIBLE("Not Eligible"),
    FEDERAL("Federal");

    private final String value;

    OvertimeStatus(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
