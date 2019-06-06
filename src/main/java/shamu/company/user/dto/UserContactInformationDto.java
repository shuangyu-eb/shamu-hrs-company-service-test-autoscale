package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserContactInformation;

@Data
@NoArgsConstructor
public class UserContactInformationDto extends BasicUserContactInformationDto {

  private String emailHome;

  public UserContactInformationDto(UserContactInformation userContactInformation) {
    super(userContactInformation);
    this.emailHome = userContactInformation.getEmailHome();
  }

  @JSONField(serialize = false)
  public UserContactInformation getUserContactInformation(UserContactInformation origin) {
    origin.setId(this.getId());
    origin.setPhoneWork(this.getPhoneWork());
    origin.setPhoneHome(this.getPhoneHome());
    origin.setEmailWork(this.getEmailWork());
    origin.setEmailHome(this.getEmailHome());
    return origin;
  }
}
