package shamu.company.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "system_announcements")
@AllArgsConstructor
@NoArgsConstructor
public class SystemAnnouncement extends BaseEntity {

  private static final long serialVersionUID = 6277212440769472205L;

  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String userId;

  private String content;

  @Column(name = "is_past_announcement")
  private Boolean isPastAnnouncement = true;
}
