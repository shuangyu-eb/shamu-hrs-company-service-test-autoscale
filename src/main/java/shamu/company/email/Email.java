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

  private String to;

  private String subject;

  private String content;

  @ManyToOne private User user;

  private Timestamp sendDate;

  @Nullable
  private Timestamp sentAt;

  public Email(String from, String to, String subject, String content, User user,
      Timestamp sendDate) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.content = content;
    this.user = user;
    this.sendDate = sendDate;
  }
}