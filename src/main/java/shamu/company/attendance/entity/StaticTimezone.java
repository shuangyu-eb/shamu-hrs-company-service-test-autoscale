package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "static_timezones")
public class StaticTimezone extends BaseEntity {
  private String name;

  private String abbreviation;
}
