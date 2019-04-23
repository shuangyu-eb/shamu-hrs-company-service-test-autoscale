package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.job.entity.CompensationFrequency;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserCompensation extends BaseEntity {

  private Integer wage;

  private Timestamp startDate;

  private Timestamp endDate;

  private String overtimeStatus;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @ToString.Exclude
  private User user;

  @OneToOne
  private CompensationType compensationType;

  @OneToOne
  private CompensationChangeReason compensationChangeReason;

  @OneToOne
  private CompensationFrequency compensationFrequency;

  private String comment;

  public UserCompensation(Integer wage, User user, CompensationFrequency compensationFrequency) {
    this.wage = wage;
    this.user = user;
    this.compensationFrequency = compensationFrequency;
  }

}
