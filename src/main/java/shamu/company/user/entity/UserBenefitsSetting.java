package shamu.company.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "user_benefits_setting")
@AllArgsConstructor
@NoArgsConstructor
public class UserBenefitsSetting extends BaseEntity {

  @OneToOne
  private User user;

  @Column(name = "hidden_banner")
  private Boolean hiddenBanner;
}
