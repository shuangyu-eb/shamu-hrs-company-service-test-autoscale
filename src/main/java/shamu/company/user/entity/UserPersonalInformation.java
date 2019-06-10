package shamu.company.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.dto.UserPersonalInformationDto;

@Entity
@Data
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserPersonalInformation extends BaseEntity {

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  private String ssn;

  @ManyToOne
  private Gender gender;

  @ManyToOne
  private MaritalStatus maritalStatus;

  @ManyToOne
  private Ethnicity ethnicity;

  @ManyToOne
  private CitizenshipStatus citizenshipStatus;

  public String getName() {
    return this.firstName.concat(" ").concat(this.lastName);
  }

  public UserPersonalInformation getPersonalInformation(
      UserPersonalInformationDto userPersonalInformationDto) {
    this.setSsn(userPersonalInformationDto.getSsn());
    this.setFirstName(userPersonalInformationDto.getFirstName());
    this.setMiddleName(userPersonalInformationDto.getMiddleName());
    this.setLastName(userPersonalInformationDto.getLastName());
    this.setPreferredName(userPersonalInformationDto.getPreferredName());
    this.setBirthDate(Date.valueOf(userPersonalInformationDto.getBirthDate()));
    Gender gender = new Gender();
    gender.setId(userPersonalInformationDto.getGenderId());
    this.setGender(gender);
    MaritalStatus maritalStatus = new MaritalStatus();
    maritalStatus.setId(userPersonalInformationDto.getMaritalStatusId());
    this.setMaritalStatus(maritalStatus);
    return this;
  }
}
