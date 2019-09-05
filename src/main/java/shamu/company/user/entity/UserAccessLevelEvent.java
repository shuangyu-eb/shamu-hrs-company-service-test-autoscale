package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;


@Entity
@Table(name = "user_access_level_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessLevelEvent extends BaseEntity {

  @ManyToOne
  private User user;

  private String originalRole;
}
