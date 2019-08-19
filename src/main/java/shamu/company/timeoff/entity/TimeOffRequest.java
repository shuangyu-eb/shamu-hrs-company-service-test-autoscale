package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.Converter;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_requests")
@Where(clause = "deleted_at IS NULL")
public class TimeOffRequest extends BaseEntity {

  @ManyToOne
  private User requesterUser;

  @ManyToOne
  private User approverUser;

  private Timestamp approvedDate;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  @Column(name = "time_off_request_approval_status_id")
  @Convert(converter = Converter.class)
  private TimeOffRequestApprovalStatus timeOffApprovalStatus =
      TimeOffRequestApprovalStatus.NO_ACTION;

  private Timestamp expiresAt;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "time_off_request_id")
  private Set<TimeOffRequestComment> comments = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "time_off_request_id")
  private Set<TimeOffRequestDate> timeOffRequestDates = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(
      name = "time_off_requests_approvers",
      joinColumns = @JoinColumn(name = "time_off_request_id"),
      inverseJoinColumns = @JoinColumn(name = "approver_user_id"))
  private Set<User> approvers = new HashSet<>();

  public void setApprover(final User user) {
    this.approvers.add(user);
  }

  public void setComment(final TimeOffRequestComment comment) {
    this.comments.add(comment);
  }

  public String getRequsterComment() {
    final List<TimeOffRequestComment> requestComments =
        this.comments.stream()
            .filter(comment -> comment.getUser().getId().equals(this.requesterUser.getId()))
            .collect(Collectors.toList());
    if (!requestComments.isEmpty()) {
      return requestComments.get(0).getComment();
    }
    return null;
  }

  public List<TimeOffRequestComment> getApproverComments() {
    return this.comments.stream()
        .filter(comment -> !comment.getUser().getId().equals(this.requesterUser.getId()))
        .collect(Collectors.toList());
  }

  public Timestamp getStartDay() {
    if (this.checkEmpty()) {
      return null;
    }
    return this.timeOffRequestDates.stream()
        .map(TimeOffRequestDate::getDate)
        .min(Comparator.comparingLong(Timestamp::getTime))
        .get();
  }

  public Timestamp getEndDay() {
    if (this.checkEmpty()) {
      return null;
    }
    return this.timeOffRequestDates.stream()
        .map(TimeOffRequestDate::getDate)
        .max(Comparator.comparingLong(Timestamp::getTime))
        .get();
  }

  public Integer getHours() {
    if (this.checkEmpty()) {
      return null;
    }
    return this.timeOffRequestDates.stream().mapToInt(TimeOffRequestDate::getHours).sum();
  }

  private boolean checkEmpty() {
    return this.timeOffRequestDates.isEmpty();
  }
}
