package shamu.company.user.entity;

import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Where(clause = "deleted_at IS NULL")
public class UserContactInformation extends BaseEntity {

  private String phoneWork;

  private String phoneWorkExtension;

  private String phoneMobile;

  private String phoneHome;

  private String emailWork;

  private String emailHome;
}
