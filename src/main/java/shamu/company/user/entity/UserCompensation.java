package shamu.company.user.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.job.entity.CompensationFrequency;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
public class UserCompensation extends BaseEntity {
  private static final int max_Wage = 2147483647;

  @Max(max_Wage)
  private Integer wage;

  private Timestamp startDate;

  private Timestamp endDate;

  private String overtimeStatus;

  @Column(name = "user_id")
  private Long userId;

  @OneToOne
  private CompensationType compensationType;

  @OneToOne
  private CompensationChangeReason compensationChangeReason;

  @ManyToOne
  private CompensationFrequency compensationFrequency;

  private String comment;
}
