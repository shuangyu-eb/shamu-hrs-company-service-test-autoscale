package shamu.company.benefit.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Entity
@Data
@Table(name = "benefits-requests")
public class BenefitRequest extends BaseEntity {

  private static final long serialVersionUID = 203811335254706616L;
  @OneToOne private User requestUser;

  @OneToOne private User appoverUser;

  @ManyToOne private BenefitLifeEventType lifeEventType;

  private Timestamp lifeEventDate;

  @OneToOne private BenefitPlanUser prevEnrollment;

  @OneToOne private BenefitPlanUser nextEnrollment;

  private Timestamp effectiveDate;

  @ManyToOne private BenefitRequestApprovalStatus requestStatus;
}
