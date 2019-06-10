package shamu.company.user.entity;

import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.dto.UserContactInformationDto;

@Entity
@Data
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserContactInformation extends BaseEntity {

  private String phoneWork;

  private String phoneWorkExtension;

  private String phoneMobile;

  private String phoneHome;

  private String emailWork;

  private String emailHome;

  public UserContactInformation getUserContactInformation(
      UserContactInformationDto userContactInformationDto) {
    this.setEmailWork(userContactInformationDto.getEmailWork());
    this.setEmailHome(userContactInformationDto.getEmailHome());
    this.setPhoneWork(userContactInformationDto.getPhoneWork());
    this.setPhoneHome(userContactInformationDto.getPhoneHome());
    return this;
  }
}
