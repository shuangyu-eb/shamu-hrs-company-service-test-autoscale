package shamu.company.benefit.entity;

import java.math.BigDecimal;
import java.util.List;

public interface EnrollmentBreakdownPojo {
  String getPlanUserId();

  long getNumber();

  String getImageUrl();

  String getFullName();

  String getOrderName();

  String getPlan();

  String getCoverage();

  BigDecimal getCompanyCost();

  BigDecimal getEmployeeCost();

  List<String> dependentUsername();
}
