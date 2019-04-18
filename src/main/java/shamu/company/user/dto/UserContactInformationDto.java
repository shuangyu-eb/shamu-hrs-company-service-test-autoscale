package shamu.company.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.user.entity.UserContactInformation;

@Data
@NoArgsConstructor
public class UserContactInformationDto {

  private Long id;

  private String phoneWork;

  private String phoneHome;

  private String emailWork;

  private String emailHome;

  public UserContactInformationDto(UserContactInformation userContactInformation) {
    BeanUtils.copyProperties(userContactInformation, this);
  }

  @JsonIgnore
  public UserContactInformation getUserContactInformation() {
    UserContactInformation userContactInformation = new UserContactInformation();
    BeanUtils.copyProperties(this,userContactInformation);
    return userContactInformation;
  }

}
