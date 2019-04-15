package shamu.company.user.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.beans.BeanUtils;
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

  private Timestamp birthDate;

  private String ssn;

  @ManyToOne private Gender gender;

  @ManyToOne private MaritalStatus maritalStatus;

  @ManyToOne private Ethnicity ethnicity;

  @ManyToOne private CitizenshipStatus citizenshipStatus;

  public UserPersonalInformation(UserPersonalInformationDto userPersonalInformationDtO) {
    BeanUtils.copyProperties(userPersonalInformationDtO, this);
    this.setGender(new Gender(userPersonalInformationDtO.getGenderId()));
    this.setMaritalStatus(new MaritalStatus(userPersonalInformationDtO.getMaritalStatusId()));
  }
}
