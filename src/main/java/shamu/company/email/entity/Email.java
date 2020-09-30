package shamu.company.email.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import shamu.company.common.entity.BaseEntity;
import shamu.company.email.event.EmailStatus;
import shamu.company.user.entity.User;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Table(name = "emails")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email extends BaseEntity {

  private static final long serialVersionUID = -4792540556729036780L;
  private String from;

  private String fromName;

  private String to;

  private String toName;

  private String subject;

  private String content;

  @ManyToOne private User user;

  private Timestamp sendDate;

  @Nullable private Timestamp sentAt;

  private Integer retryCount;

  private String messageId;

  @Enumerated(value = EnumType.STRING)
  private EmailStatus status;

  public Email(final Email emailInfo) {
    from = emailInfo.from;
    fromName = emailInfo.fromName;
    toName = emailInfo.toName;
    subject = emailInfo.subject;
    content = emailInfo.content;
    user = emailInfo.user;
    sentAt = emailInfo.sentAt;
    retryCount = emailInfo.retryCount;
    setCreatedAt(emailInfo.getCreatedAt());
  }

  public Email(
      final String from,
      final String to,
      final String subject,
      final String content,
      final User user,
      final Timestamp sendDate) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.content = content;
    this.user = user;
    this.sendDate = sendDate;
  }

  public Email(
      final String from,
      final String to,
      final String subject,
      final String content,
      final Timestamp sendDate) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.content = content;
    this.sendDate = sendDate;
  }

  public Email(
      final String from,
      final String fromName,
      final String to,
      final String toName,
      final String subject) {
    this.from = from;
    this.fromName = fromName;
    this.to = to;
    this.toName = toName;
    this.subject = subject;
  }
}
