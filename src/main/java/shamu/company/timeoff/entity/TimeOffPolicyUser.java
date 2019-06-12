package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_policies_users")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffPolicyUser extends BaseEntity {

  @ManyToOne
  private User user;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  private Integer balance;
}
