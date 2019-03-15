package com.tardisone.companyservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


@ToString(exclude = "encryptedPassword")
@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String employeeNumber;

  @Basic(optional = false)
  @Column(unique = true)
  private String emailWork;

  @JsonIgnore
  private String password;

  private Timestamp latestLogin;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;

  private String invitationEmailToken;

  private Timestamp invitedAt;

  private Timestamp resetPasswordSentAt;

  private String resetPasswordToken;

  private String verificationToken;

  private Timestamp verifiedAt;

  @ManyToOne
  private UserStatuses userStatus;

  @Override
  public boolean equals(Object object) {
    if (object instanceof User) {
      Long id = ((User) object).getId();
      if (this.id == null && id == null) {
        return true;
      }
      if (this.id != null && id != null) {
        return this.id.equals(id);
      }
    }
    return false;
  }

  private static final long serialVersionUID = 832607623427217720L;
}
