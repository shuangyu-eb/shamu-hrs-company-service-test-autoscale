package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class BasicUserPersonalInformationDto {
  @HashidsFormat private Long id;

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  public BasicUserPersonalInformationDto(UserPersonalInformation personalInformation) {
    this.id = personalInformation.getId();
    this.firstName = personalInformation.getFirstName();
    this.lastName = personalInformation.getLastName();
    this.middleName = personalInformation.getMiddleName();
    this.preferredName = personalInformation.getPreferredName();
    this.birthDate = personalInformation.getBirthDate();
  }
}
