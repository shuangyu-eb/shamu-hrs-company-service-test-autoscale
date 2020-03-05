package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "time_off_request_dates")
public class TimeOffRequestDate extends BaseEntity {

  private static final long serialVersionUID = -215460639062476659L;
  @Column(name = "time_off_request_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String timeOffRequestId;

  private Timestamp date;

  private Integer hours;

  public TimeOffRequestDate(final Timestamp date) {
    this.date = date;
  }

  public TimeOffRequestDate() {
  }

  public enum Operator {
    MORE_THAN,
    LESS_THAN
  }
}
