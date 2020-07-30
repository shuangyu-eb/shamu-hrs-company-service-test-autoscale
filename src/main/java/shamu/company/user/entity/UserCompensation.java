package shamu.company.user.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.attendance.entity.Currency;
import shamu.company.common.entity.BaseEntity;
import shamu.company.job.entity.CompensationFrequency;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
public class UserCompensation extends BaseEntity {
  private static final long serialVersionUID = -7026686695745173051L;

  private BigInteger wageCents;

  private Timestamp startDate;

  private Timestamp endDate;

  @ManyToOne private CompensationOvertimeStatus overtimeStatus;

  @Column(name = "user_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String userId;

  @OneToOne private CompensationType compensationType;

  @OneToOne private CompensationChangeReason compensationChangeReason;

  @ManyToOne private CompensationFrequency compensationFrequency;

  private String comment;

  @OneToOne private Currency currency;

  public UserCompensation(String userId, BigInteger wageCents,
                   CompensationOvertimeStatus overtimeStatus, CompensationFrequency compensationFrequency) {
    this.userId = userId;
    this.wageCents = wageCents;
    this.overtimeStatus = overtimeStatus;
    this.compensationFrequency = compensationFrequency;
  }
}
