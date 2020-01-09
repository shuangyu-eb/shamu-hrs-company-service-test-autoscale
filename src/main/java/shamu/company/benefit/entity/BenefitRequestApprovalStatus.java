package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_request_status")
public class BenefitRequestApprovalStatus extends BaseEntity {

  private String name;

  public enum BenefitRequestStatus {
    NO_ACTION,
    VIEWED,
    AWAITING_REVIEW,
    APPROVED,
    DENIED
  }
}
