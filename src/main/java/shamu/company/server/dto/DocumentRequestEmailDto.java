package shamu.company.server.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
public class DocumentRequestEmailDto {

  private String senderId;

  private List<String> recipientUserIds;

  private String documentTitle;

  private String message;

  private DocumentRequestType type;

  private String documentEmailUrl;

  private Timestamp expiredAt;

  public enum DocumentRequestType {
    SIGN, ACKNOWLEDGE, VIEW,
  }
}
