package shamu.company.user.entity;

import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

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

  @ManyToOne(cascade = CascadeType.PERSIST)
  private Gender gender;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private MaritalStatus maritalStatus;

  @ManyToOne
  private Ethnicity ethnicity;

  @ManyToOne
  private CitizenshipStatus citizenshipStatus;

}
