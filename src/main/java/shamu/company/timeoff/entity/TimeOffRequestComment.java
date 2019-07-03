package shamu.company.timeoff.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Entity
@Data
@Table(name = "time_off_request_comments")
@Where(clause = "deleted_at is NULL")
@NoArgsConstructor
public class TimeOffRequestComment extends BaseEntity {

  @Column(name = "time_off_request_id")
  private Long timeOffRequestId;

  @ManyToOne
  private User user;

  @Column(columnDefinition = "TEXT")
  private String comment;

  public TimeOffRequestComment(User user, String comment) {
    this.user = user;
    this.comment = comment;
  }
}
