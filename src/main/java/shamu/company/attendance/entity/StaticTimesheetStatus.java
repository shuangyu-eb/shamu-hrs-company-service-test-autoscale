package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "static_timesheet_status")
public class StaticTimesheetStatus extends BaseEntity {
  private String name;
}
