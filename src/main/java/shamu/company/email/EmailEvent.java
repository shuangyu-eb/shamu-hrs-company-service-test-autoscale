package shamu.company.email;

import lombok.Data;

@Data
public class EmailEvent {
  private EmailStatus event;
  private String messageId;
  private String email;
  private String reason;
}
