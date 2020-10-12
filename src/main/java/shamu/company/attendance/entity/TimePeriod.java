package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "time_periods")
public class TimePeriod extends BaseEntity {
  private static final long serialVersionUID = 8265118088281827286L;
  private Timestamp startDate;

  private Timestamp endDate;

  public TimePeriod() {}

  public TimePeriod(final Date startDate, final Date endDate) {
    this.startDate = new Timestamp(startDate.getTime());
    this.endDate = new Timestamp(endDate.getTime());
  }
}
