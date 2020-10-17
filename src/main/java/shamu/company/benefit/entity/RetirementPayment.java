package shamu.company.benefit.entity;

import java.math.BigInteger;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "retirement_payment")
@NoArgsConstructor
@AllArgsConstructor
public class RetirementPayment extends BaseEntity {

  private static final long serialVersionUID = -8660155521960933057L;

  @ManyToOne private BenefitPlan benefitPlan;

  private BigInteger companyMaximumContribution;

  private BigInteger companyContributionValue;

  @ManyToOne private RetirementPayTypes companyContribution;

  private BigInteger employeeDeductionValue;

  @ManyToOne private RetirementPayTypes employeeDeduction;

  @ManyToOne private User user;

  private Boolean limitStandard;
}
