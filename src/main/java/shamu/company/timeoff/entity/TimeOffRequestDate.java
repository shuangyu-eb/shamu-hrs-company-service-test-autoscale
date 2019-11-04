package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "time_off_request_dates")
public class TimeOffRequestDate extends BaseEntity {

  @Column(name = "time_off_request_id")
  private Long timeOffRequestId;

  private Timestamp date;

  private Integer hours;

}
