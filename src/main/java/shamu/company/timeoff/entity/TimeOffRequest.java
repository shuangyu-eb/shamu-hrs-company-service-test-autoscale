package shamu.company.timeoff.entity;

import io.micrometer.core.instrument.util.StringUtils;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "time_off_requests")
public class TimeOffRequest extends BaseEntity {

  @ManyToOne
  private User requesterUser;

  @ManyToOne
  private User approverUser;

  private Timestamp approvedDate;

  private Integer balance;

  @ManyToOne
  private TimeOffPolicy timeOffPolicy;

  @ManyToOne
  private TimeOffRequestApprovalStatus timeOffRequestApprovalStatus;

  private Timestamp expiresAt;

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinColumn(name = "time_off_request_id")
  private Set<TimeOffRequestComment> comments = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinColumn(name = "time_off_request_id")
  private Set<TimeOffRequestDate> timeOffRequestDates = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(
      name = "time_off_requests_approvers",
      joinColumns = @JoinColumn(name = "time_off_request_id"),
      inverseJoinColumns = @JoinColumn(name = "approver_user_id"))
  private Set<User> approvers = new HashSet<>();

  public void setApprover(final User user) {
    approvers.add(user);
  }

  public void setComment(final TimeOffRequestComment comment) {
    comments.add(comment);
  }

  public String getRequsterComment() {
    final List<TimeOffRequestComment> requestComments =
        comments.stream()
            .filter(comment -> comment.getUser().getId().equals(requesterUser.getId()))
            .collect(Collectors.toList());
    if (!requestComments.isEmpty()) {
      return requestComments.get(0).getComment();
    }
    return null;
  }

  public List<TimeOffRequestComment> getApproverComments() {
    return comments.stream()
        .filter(comment -> !comment.getUser().getId().equals(requesterUser.getId()))
        .collect(Collectors.toList());
  }

  public Timestamp getStartDay() {
    if (checkEmpty()) {
      return null;
    }
    return timeOffRequestDates.stream()
        .map(TimeOffRequestDate::getDate)
        .min(Comparator.comparingLong(Timestamp::getTime))
        .get();
  }

  public Timestamp getEndDay() {
    if (checkEmpty()) {
      return null;
    }
    return timeOffRequestDates.stream()
        .map(TimeOffRequestDate::getDate)
        .max(Comparator.comparingLong(Timestamp::getTime))
        .get();
  }

  public Integer getHours() {
    if (checkEmpty()) {
      return null;
    }
    return timeOffRequestDates.stream().mapToInt(TimeOffRequestDate::getHours).sum();
  }

  private boolean checkEmpty() {
    return timeOffRequestDates.isEmpty();
  }

  public TimeOffApprovalStatus getApprovalStatus() {
    String name = this.timeOffRequestApprovalStatus.getName();
    return StringUtils.isEmpty(name) ? null : TimeOffApprovalStatus.valueOf(name);
  }
}
