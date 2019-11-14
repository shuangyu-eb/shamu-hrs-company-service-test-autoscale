package shamu.company.timeoff.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Entity
@Data
@Table(name = "time_off_request_comments")
@NoArgsConstructor
public class TimeOffRequestComment extends BaseEntity {

  @Column(name = "time_off_request_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String timeOffRequestId;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(columnDefinition = "TEXT")
  private String comment;

  public TimeOffRequestComment(final User user, final String comment) {
    this.user = user;
    this.comment = comment;
  }
}
