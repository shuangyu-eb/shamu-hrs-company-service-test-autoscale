package shamu.company.benefit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentBreakdownDto {
  long number;

  String preferredName;

  String plan;

  String coverage;

  int dependents;

  BigDecimal companyCost;

  BigDecimal employeeCost;

  public EnrollmentBreakdownDto(
      final long number,
      final String firstName,
      final String lastName,
      final String plan,
      final String coverage,
      final int dependents,
      final BigDecimal companyCost,
      final BigDecimal employeeCost) {
    setNumber(number);
    setPreferredName(firstName.concat(" ").concat(lastName));
    setPlan(plan);
    setCoverage(coverage);
    setDependents(dependents);
    setCompanyCost(companyCost);
    setEmployeeCost(employeeCost);
  }
}
