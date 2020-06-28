package shamu.company.attendance.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_period")
public class TimePeriod extends BaseEntity {
  private static final long serialVersionUID = 8265118088281827286L;
  private Timestamp startDate;

  private Timestamp endDate;
}
