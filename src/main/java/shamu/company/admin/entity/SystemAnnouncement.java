package shamu.company.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "system_announcements")
@AllArgsConstructor
@NoArgsConstructor
public class SystemAnnouncement extends BaseEntity {

  @OneToOne private User user;

  private String content;

  @Column(name = "is_past_announcement")
  private Boolean isPastAnnouncement = true;
}
