package shamu.company.attendance.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "employee_time_logs")
public class EmployeeTimeLog extends BaseEntity {

  private Timestamp start;

  private int durationMin;

  @OneToOne private StaticEmployeesTaTimeType staticEmployeesTaTimeType;

  @OneToOne private EmployeeTimeEntry employeeTimeEntry;
}
