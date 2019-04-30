package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.UserContactInformation;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContactInformationDto {

  @HashidsFormat
  private Long id;

  private String phoneWork;

  private String phoneHome;

  private String emailWork;

  private String emailHome;

  public UserContactInformationDto(UserContactInformation userContactInformation) {
    BeanUtils.copyProperties(userContactInformation, this);
  }

  @JSONField(serialize = false)
  public UserContactInformation getUserContactInformation() {
    UserContactInformation userContactInformation = new UserContactInformation();
    BeanUtils.copyProperties(this, userContactInformation);
    return userContactInformation;
  }

}
