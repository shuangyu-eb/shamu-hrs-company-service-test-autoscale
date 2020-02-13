package shamu.company.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.springframework.data.annotation.Transient;
import org.thymeleaf.util.StringUtils;

public enum EmailStatus {
  PROCESSED(EmailEventType.DELIVERY, -5),
  DEFERRED(EmailEventType.DELIVERY, -4),
  BOUNCE(EmailEventType.DELIVERY, -3),
  DROPPED(EmailEventType.DELIVERY, -2),
  DELIVERED(EmailEventType.DELIVERY, -1),
  OPEN(EmailEventType.ENGAGEMENT, 1),
  CLICK(EmailEventType.ENGAGEMENT, 2),
  SPAMREPORT(EmailEventType.ENGAGEMENT, 3),
  UNSUBSCRIBE(EmailEventType.ENGAGEMENT, 4),
  GROUPUNSUBSCRIBE(EmailEventType.ENGAGEMENT, 5),
  GROUPRESUBSCRIBE(EmailEventType.ENGAGEMENT, 6);

  @Transient @Getter private final EmailEventType eventType;
  @Transient @Getter private final int priority;

  EmailStatus(final EmailEventType eventType, final int priority) {
    this.eventType = eventType;
    this.priority = priority;
  }

  @JsonCreator
  public static EmailStatus setValue(final String event) {
    for (final EmailStatus emailStatus : EmailStatus.values()) {
      if (StringUtils.equalsIgnoreCase(emailStatus.name(), event)) {
        return emailStatus;
      }
    }
    return null;
  }
}
