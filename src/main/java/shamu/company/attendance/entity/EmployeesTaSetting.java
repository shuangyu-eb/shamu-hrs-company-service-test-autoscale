package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "employees_ta_settings")
public class EmployeesTaSetting extends BaseEntity {

  @OneToOne private User employee;

  @ManyToOne
  private StaticTimezone staticTimezone;

  private int messagingOn;
}
