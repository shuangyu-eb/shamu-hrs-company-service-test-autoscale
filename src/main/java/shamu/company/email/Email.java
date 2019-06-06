package shamu.company.email;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Table(name = "emails")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Email extends BaseEntity {

  private String from;

  private String fromName;

  private String to;

  private String toName;

  private String subject;

  private String content;

  @ManyToOne
  private User user;

  private Timestamp sendDate;

  @Nullable
  private Timestamp sentAt;

  private Integer retryCount;

  public Email(String from, String to, String subject, String content, User user,
      Timestamp sendDate) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.content = content;
    this.user = user;
    this.sendDate = sendDate;
  }

  public Email(String from, String to, String subject, String content, Timestamp sendDate) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.content = content;
    this.sendDate = sendDate;
  }

  public Email(User from, User to, String subject) {
    this.from = from.getEmailWork();
    this.fromName = from.getUserPersonalInformation().getName();
    this.to = to.getEmailWork();
    this.toName = to.getUserPersonalInformation().getName();
    this.subject = subject;
  }
}
