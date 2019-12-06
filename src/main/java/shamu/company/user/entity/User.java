package shamu.company.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
import shamu.company.common.exception.GeneralException;
import shamu.company.company.entity.Company;
import shamu.company.utils.UuidUtil;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Id
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String id;

  private Timestamp latestLogin;
  
  @ManyToOne
  private UserStatus userStatus;
  
  @ManyToOne
  private UserRole userRole;

  private String imageUrl;

  @ManyToOne
  private Company company;

  @OneToOne
  private DeactivationReasons deactivationReason;

  @JSONField(format = "MM/dd/yyyy")
  private Date deactivatedAt;

  @ManyToOne
  @JSONField(serialize = false)
  @ToString.Exclude
  private User managerUser;

  @OneToOne(cascade = CascadeType.ALL)
  private UserPersonalInformation userPersonalInformation;

  @OneToOne(cascade = CascadeType.ALL)
  @NotNull
  private UserContactInformation userContactInformation;

  @OneToOne(cascade = CascadeType.ALL)
  private UserCompensation userCompensation;

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

  @UpdateTimestamp
  private Timestamp updatedAt;

  private String salt;

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
      throw new GeneralException("Please save this manager before setting this user's manager.");
    }
    if (managerUser.getId().equals(getId())) {
      throw new GeneralException("Users cannot set themselves to be their manager.");
    }
    this.managerUser = managerUser;
  }

  public Role getRole() {
    if (userRole == null) {
      return null;
    }
    return Role.valueOf(userRole.getName());
  }

  private void setSalt() {
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
