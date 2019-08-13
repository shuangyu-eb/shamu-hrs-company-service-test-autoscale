package shamu.company.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

  private String employeeNumber;

  @Email
  private String emailWork;

  private String password;

  private Timestamp latestLogin;

  @OneToOne
  private UserStatus userStatus;

  private String imageUrl;

  @ManyToOne
  private Company company;


  @OneToOne
  private DeactivationReasons deactivationReason;

  @JSONField(format = "yyyy-MM-dd")
  private Date deactivatedAt;

  @ManyToOne
  @JSONField(serialize = false)
  @ToString.Exclude
  private User managerUser;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserPersonalInformation userPersonalInformation;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserContactInformation userContactInformation;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserCompensation userCompensation;

  @OneToOne
  private UserRole userRole;

  private String invitationEmailToken;

  private Timestamp invitedAt;

  private Timestamp resetPasswordSentAt;

  private String resetPasswordToken;

  private String verificationToken;

  private Timestamp verifiedAt;

  public User(final Long id) {
    this.setId(id);
  }

  public User(final Long id, final String imageUrl) {
    this.setId(id);
    this.setImageUrl(imageUrl);
  }

  public Role getRole() {
    return Role.valueOf(userRole.getName());
  }

  public enum Role {
    ADMIN,
    MANAGER,
    NON_MANAGER,
  }
}
