package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.user.entity.exception.SetManagerFailedException;
import shamu.company.utils.UuidUtil;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements Serializable {

  @Id
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String id;

  private Timestamp latestLogin;

  @ManyToOne private UserStatus userStatus;

  @ManyToOne private UserRole userRole;

  private String imageUrl;

  @OneToOne private DeactivationReasons deactivationReason;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date deactivatedAt;

  @ManyToOne @JsonIgnore @ToString.Exclude private User managerUser;

  @OneToOne(cascade = CascadeType.ALL)
  private UserPersonalInformation userPersonalInformation;

  @OneToOne(cascade = CascadeType.ALL)
  private UserContactInformation userContactInformation;

  @ManyToOne private StaticTimezone timeZone;

  private String invitationEmailToken;

  private Timestamp invitedAt;

  private Timestamp resetPasswordSentAt;

  private String resetPasswordToken;

  private String verificationToken;

  private Timestamp verifiedAt;

  private String changeWorkEmail;

  private String changeWorkEmailToken;

  private Timestamp verifyChangeWorkEmailAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  private Timestamp invitationCapabilityFrozenAt;

  private String salt;

  // When invite indeed employee, create new userSecret and update auth0 AppMetadata
  @Transient
  private String hash;

  private static final long serialVersionUID = 111073632285737978L;

  public User(final String id) {
    setId(id);
  }

  public User(final String id, final String imageUrl) {
    setId(id);
    setImageUrl(imageUrl);
  }

  public void setManagerUser(final User managerUser) {
    if (null == managerUser) {
      this.managerUser = null;
      return;
    }
    if (StringUtils.isEmpty(managerUser.getId())) {
      throw new SetManagerFailedException(
          "Please save this manager before setting this user's manager.");
    }
    if (managerUser.getId().equals(getId())) {
      throw new SetManagerFailedException("Users cannot set themselves to be their manager.");
    }
    this.managerUser = managerUser;
  }

  public Role getRole() {
    if (userRole == null) {
      return null;
    }
    return Role.valueOf(userRole.getName());
  }

  public void setSalt() {
    if (salt == null) {
      salt = UuidUtil.getUuidString();
    }
  }

  public String getSalt() {
    setSalt();
    return salt;
  }

  public enum Role {
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    EMPLOYEE("EMPLOYEE"),
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
