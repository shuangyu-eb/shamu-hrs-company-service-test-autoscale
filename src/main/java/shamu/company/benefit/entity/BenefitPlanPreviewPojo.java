package shamu.company.benefit.entity;

import java.sql.Timestamp;

public interface BenefitPlanPreviewPojo {

  String getBenefitPlanId();

  String getBenefitPlanName();

  Timestamp getDeductionsBegin();

  Timestamp getDeductionsEnd();

  String getStatus();

  Number getEligibleNumber();

  Number getEnrolledNumber();
}
