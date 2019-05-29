package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "time_off_request_dates")
@Where(clause = "deleted_at IS NULL")
public class TimeOffRequestDate extends BaseEntity {

  @Column(name = "time_off_request_id")
  private Long timeOffRequestId;

  private Timestamp date;

  private Integer hours;

}
