package shamu.company.admin.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.admin.entity.SystemAnnouncement;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemAnnouncementDto {

  private String id;

  private String content;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  public SystemAnnouncementDto(SystemAnnouncement systemAnnouncement) {
    this.id = systemAnnouncement.getId();
    this.content = systemAnnouncement.getContent();
    this.createdAt = systemAnnouncement.getCreatedAt();
    this.updatedAt = systemAnnouncement.getUpdatedAt();
  }
}
