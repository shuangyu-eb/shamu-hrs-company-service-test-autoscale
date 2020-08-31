package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "employees_ta_settings")
public class EmployeesTaSetting extends BaseEntity {

  private static final long serialVersionUID = 2830885308960185778L;
  @OneToOne private User employee;

  private Integer messagingOn;
}
