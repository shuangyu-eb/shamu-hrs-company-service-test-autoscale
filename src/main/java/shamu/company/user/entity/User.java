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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.exception.GeneralException;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

  /**
   * @deprecated Please use email from table user_contact_information. Because we integrate Auth0,
   * we do a mapping with Auth0 account by property userId( or column user_id in table users). One
   * userId has one employee. Should reference an employee by user id.
   */
  @Deprecated
  @Email
  @Length(max = 255)
  private String emailWork;

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

  private String userId;

  public User(final Long id) {
    setId(id);
  }

  public User(final Long id, final String imageUrl) {
    setId(id);
    setImageUrl(imageUrl);
  }

  public Role getRole() {
    return Role.valueOf(userRole.getName());
  }

  public void setManagerUser(final User managerUser) {
    if (null == managerUser) {
      this.managerUser = null;
      return;
    }
    if (null == managerUser.getId()) {
      throw new GeneralException("Please save this manager before set this user's manager.");
    }
    if (managerUser.getId().equals(getId())) {
      throw new GeneralException("Users cannot set themselves to be their manager.");
    }
    this.managerUser = managerUser;
  }

  public enum Role {
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    NON_MANAGER("EMPLOYEE"),
    INACTIVATE("INACTIVATE");

    private final String value;

    Role(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
