package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_policies_users")
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffPolicyUser extends BaseEntity {

  @ManyToOne
  private User user;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  private Integer balance;
}
