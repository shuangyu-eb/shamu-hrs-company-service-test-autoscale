package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "static_employees_ta_time_types")
public class StaticEmployeesTaTimeType extends BaseEntity {
  private String name;

  public enum TimeType {
    WORK,
    BREAK
  }
}
