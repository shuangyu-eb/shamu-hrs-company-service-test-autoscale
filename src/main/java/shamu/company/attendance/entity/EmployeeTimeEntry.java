package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_time_entries")
public class EmployeeTimeEntry extends BaseEntity {

  private String comment;

  @ManyToOne User employee;

  @ManyToOne private TimeSheet timesheet;
}
