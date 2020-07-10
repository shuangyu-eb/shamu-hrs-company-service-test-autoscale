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
    ACTIVE,
    SUBMITTED,
    APPROVED,
  }
}
