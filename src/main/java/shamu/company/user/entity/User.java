package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

  private String employeeNumber;

  private String emailWork;

  private String password;

  private Timestamp latestLogin;

  @OneToOne
  private UserStatus userStatus;

  private String imageUrl;

  @ManyToOne
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private User managerUser;

  @OneToOne
  private UserPersonalInformation userPersonalInformation;

  @OneToOne
  private UserContactInformation userContactInformation;

  @OneToOne
  private UserCompensation userCompensation;

  @OneToOne
  private UserRole userRole;

  private String invitationEmailToken;

  private Timestamp invitedAt;

  private Timestamp resetPasswordSentAt;

  private String resetPasswordToken;

  private String verificationToken;

  private Timestamp verifiedAt;

  public User(Long id) {
    this.setId(id);
  }
}
