package shamu.company.timeoff.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "time_off_requests_approvers")
@Data
@IdClass(value = TimeOffRequestApprover.class)
public class TimeOffRequestApprover implements Serializable {

  @Id
  @Column(name = "time_off_request_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String timeOffRequestId;

  @Id
  @Column(name = "approver_user_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String approverUserId;
}
