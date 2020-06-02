package shamu.company.attendance.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;

@Data
@Entity
@Table(name = "timesheets")
public class TimeSheet extends BaseEntity {
  private Timestamp startDate;

  private Timestamp endDate;

  @ManyToOne private User employee;

  @ManyToOne private StaticTimesheetStatus staticTimesheetStatus;

  @ManyToOne private User approverEmployee;

  private Timestamp approvedTimeStamp;

  @ManyToOne private UserCompensation userCompensation;
}
