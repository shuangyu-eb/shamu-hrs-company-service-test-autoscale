package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "timesheets")
public class Timesheet extends BaseEntity {
  private static final long serialVersionUID = 8705148089462995423L;

  @ManyToOne private User employee;

  @ManyToOne private StaticTimesheetStatus status;

  @ManyToOne private User approverEmployee;

  private Timestamp approvedTimestamp;

  @ManyToOne private UserCompensation userCompensation;

  @ManyToOne private TimePeriod timePeriod;

  private Timestamp removedAt;
}
