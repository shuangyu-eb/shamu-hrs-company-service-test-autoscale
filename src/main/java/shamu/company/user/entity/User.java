package shamu.company.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

  private String employeeNumber;

  private String emailWork;

  private String password;

  private Timestamp latestLogin;

  @OneToOne private UserStatus userStatus;

  private String imageUrl;

  @ManyToOne private Company company;

  @ManyToOne
  @JSONField(serialize = false)
  private User managerUser;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserPersonalInformation userPersonalInformation;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserContactInformation userContactInformation;

  @OneToOne(cascade = CascadeType.PERSIST)
  private UserCompensation userCompensation;

  @OneToOne private UserRole userRole;

  private Boolean isAccountOwner;

  private String invitationEmailToken;

  private Timestamp invitedAt;

  private Timestamp resetPasswordSentAt;

  private String resetPasswordToken;

  private String verificationToken;

  private Timestamp verifiedAt;

  public User(Long id) {
    this.setId(id);
  }

  public User(Long id, String imageUrl) {
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
