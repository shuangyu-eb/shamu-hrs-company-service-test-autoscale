package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_adjustments")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TimeOffAdjustment extends BaseEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "shamu.company.common.DefaultUuidGenerator")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String id;

  @CreationTimestamp private Timestamp createdAt;

  @ManyToOne private User user;

  @ManyToOne private TimeOffPolicy timeOffPolicy;

  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String adjusterUserId;

  private Integer amount;

  private String comment;

  private static final long serialVersionUID = -4788343620604284956L;

  public TimeOffAdjustment(
      final TimeOffPolicyUser user, final TimeOffPolicy enrollPolicy, final User currentUser) {
    this.user = user.getUser();
    timeOffPolicy = enrollPolicy;
    adjusterUserId = currentUser.getId();
    amount = user.getInitialBalance();
  }
}
