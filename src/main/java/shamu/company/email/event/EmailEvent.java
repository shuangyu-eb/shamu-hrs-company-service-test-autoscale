package shamu.company.email.event;

import lombok.Data;

@Data
public class EmailEvent {
  private EmailStatus event;
  private String messageId;
  private String email;
  private String reason;
}
