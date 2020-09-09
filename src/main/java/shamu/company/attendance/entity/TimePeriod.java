package shamu.company.attendance.entity;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "time_period")
public class TimePeriod extends BaseEntity {
  private static final long serialVersionUID = 8265118088281827286L;
  private Timestamp startDate;

  private Timestamp endDate;

  @ManyToOne private Company company;

  public TimePeriod() {}

  public TimePeriod(final Date startDate, final Date endDate, final Company company) {
    this.startDate = new Timestamp(startDate.getTime());
    this.endDate = new Timestamp(endDate.getTime());
    this.company = company;
  }
}
