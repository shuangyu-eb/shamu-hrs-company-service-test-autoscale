package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.Converter;
import shamu.company.user.entity.User;

@Entity
@Data
@Table(name = "time_off_requests")
@Where(clause = "deleted_at IS NULL")
public class TimeOffRequest extends BaseEntity {

  @ManyToOne
  private User requesterUser;

  @ManyToOne
  private User approverUser;

  private Timestamp approvedDate;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  @Column(name = "time_off_request_approval_status_id")
  @Convert(converter = Converter.class)
  private TimeOffRequestApprovalStatus timeOffApprovalStatus =
      TimeOffRequestApprovalStatus.NO_ACTION;

  private Timestamp expiresAt;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(columnDefinition = "TEXT")
  private String approverComment;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "time_off_request_id")
  private Set<TimeOffRequestDate> timeOffRequestDates;
}
