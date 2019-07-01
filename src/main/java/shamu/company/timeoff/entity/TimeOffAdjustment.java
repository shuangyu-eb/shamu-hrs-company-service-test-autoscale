package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_adjustments")
public class TimeOffAdjustment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @HashidsFormat
  private Long id;

  @CreationTimestamp
  private Timestamp createdAt;

  @ManyToOne
  private Company company;

  @ManyToOne
  private User user;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  private Long adjusterUserId;

  private Integer amount;

  private String comment;

  public TimeOffAdjustment(TimeOffPolicyUser user,TimeOffPolicy enrollPolicy,User currentUser) {
    this.company = user.getUser().getCompany();
    this.user = user.getUser();
    this.timeOffPolicy = enrollPolicy;
    this.adjusterUserId = currentUser.getId();
    this.amount = user.getBalance();
  }
}
