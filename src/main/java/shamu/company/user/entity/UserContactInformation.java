package shamu.company.user.entity;

import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserContactInformation extends BaseEntity {

  @Length(max = 50)
  private String phoneWork;

  @Length(max = 50)
  private String phoneWorkExtension;

  @Length(max = 50)
  private String phoneMobile;

  @Length(max = 50)
  private String phoneHome;

  @Length(max = 255)
  private String emailWork;

  @Length(max = 255)
  private String emailHome;
}
