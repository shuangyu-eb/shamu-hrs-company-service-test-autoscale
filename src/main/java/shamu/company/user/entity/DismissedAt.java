package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "dismissed_at")
@AllArgsConstructor
@NoArgsConstructor
public class DismissedAt extends BaseEntity {

  private static final long serialVersionUID = 4809408386010041398L;
  @OneToOne
  private User user;

  @OneToOne
  private SystemAnnouncement systemAnnouncement;
}
