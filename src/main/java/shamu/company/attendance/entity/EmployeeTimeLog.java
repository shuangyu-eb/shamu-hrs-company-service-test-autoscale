package shamu.company.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_time_logs")
public class EmployeeTimeLog extends BaseEntity {

  private static final long serialVersionUID = 3098750609517123957L;
  private Timestamp start;

  private Integer durationMin;

  @ManyToOne private StaticEmployeesTaTimeType timeType;

  @ManyToOne private EmployeeTimeEntry entry;
}
