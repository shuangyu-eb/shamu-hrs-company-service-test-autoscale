package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class UserPersonalInformationDto extends UserPersonalInformationForManagerDto {

  private String ssn;

  public UserPersonalInformationDto(UserPersonalInformation userPersonalInformation) {
    super(userPersonalInformation);
    this.ssn = userPersonalInformation.getSsn();
  }

  @JSONField(serialize = false)
  public UserPersonalInformation getUserPersonalInformation(UserPersonalInformation origin) {
    origin.setFirstName(this.getFirstName());
    origin.setMiddleName(this.getMiddleName());
    origin.setLastName(this.getLastName());
    origin.setPreferredName(this.getPreferredName());
    origin.setBirthDate(Date.valueOf(this.getBirthDate()));
    origin.setSsn(this.getSsn());

    if (this.getGenderId() != null) {
      origin.setGender(new Gender(this.getGenderId()));
    }

    if (this.getEthnicityId() != null) {
      origin.setEthnicity(new Ethnicity(this.getEthnicityId()));
    }

    if (this.getMaritalStatusId() != null) {
      origin.setMaritalStatus(new MaritalStatus(this.getMaritalStatusId()));
    }

    return origin;
  }
}
