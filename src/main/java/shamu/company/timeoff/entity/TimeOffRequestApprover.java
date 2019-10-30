package shamu.company.timeoff.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "time_off_requests_approvers")
@Data
@IdClass(value = TimeOffRequestApprover.class)
public class TimeOffRequestApprover implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "time_off_request_id")
  private Long timeOffRequestId;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "approver_user_id")
  private Long approverUserId;
}
