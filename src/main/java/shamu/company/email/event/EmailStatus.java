package shamu.company.email.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.springframework.data.annotation.Transient;
import org.thymeleaf.util.StringUtils;

public enum EmailStatus {
  INVALID(1),
  PROCESSED(2),
  DEFERRED(3),
  BOUNCE(4),
  DROPPED(5),
  DELIVERED(6);

  @Transient @Getter private final int priority;

  EmailStatus(final int priority) {
    this.priority = priority;
  }

  @JsonCreator
  public static EmailStatus setValue(final String event) {
    for (final EmailStatus emailStatus : EmailStatus.values()) {
      if (StringUtils.equalsIgnoreCase(emailStatus.name(), event)) {
        return emailStatus;
      }
    }
    return EmailStatus.INVALID;
  }
}
