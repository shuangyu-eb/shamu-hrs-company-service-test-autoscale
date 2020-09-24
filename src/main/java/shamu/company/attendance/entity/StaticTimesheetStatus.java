package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "static_timesheet_status")
public class StaticTimesheetStatus extends BaseEntity {
  private static final long serialVersionUID = -6216548043764446335L;
  private String name;

  public enum TimeSheetStatus {
    ACTIVE("ACTIVE"),
    SUBMITTED("SUBMITTED"),
    APPROVED("APPROVED"),
    NOT_YET_START("NOT YET START"),
    COMPLETE("COMPLETE"),
    SEND_TO_PAYROLL("SEND TO PAYROLL");
    private final String value;

    TimeSheetStatus(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
