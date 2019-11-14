package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "time_off_request_approval_statuses")
public class TimeOffRequestApprovalStatus extends BaseEntity {

  private String name;

  public enum TimeOffApprovalStatus {
    NO_ACTION,
    VIEWED,
    APPROVED,
    DENIED
  }
}
