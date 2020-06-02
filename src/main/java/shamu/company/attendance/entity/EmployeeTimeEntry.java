package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "employee_time_entries")
public class EmployeeTimeEntry extends BaseEntity {

  private String comment;

  @ManyToOne User employee;

  @ManyToOne private TimeSheet timeSheet;
}
