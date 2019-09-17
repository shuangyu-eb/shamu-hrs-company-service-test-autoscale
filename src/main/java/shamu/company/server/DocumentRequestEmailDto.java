package shamu.company.server;

import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
public class DocumentRequestEmailDto {

  private Long senderId;

  private List<Long> recipientUserIds;

  private String documentTitle;

  private String message;

  private DocumentRequestType type;

  private String documentEmailUrl;

  private Timestamp expiredAt;

  public enum DocumentRequestType {
    SIGN, ACKNOWLEDGE, VIEW,
  }
}
