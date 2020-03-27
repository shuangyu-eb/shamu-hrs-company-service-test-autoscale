package shamu.company.benefit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentBreakdownDto {
  String planUserId;

  long number;

  String imageUrl;

  String fullName;

  String orderName;

  String plan;

  String coverage;

  BigDecimal companyCost;

  BigDecimal employeeCost;

  public EnrollmentBreakdownDto(
      final long number,
      final String imageUrl,
      final String firstName,
      final String lastName,
      final String plan,
      final String coverage,
      final BigDecimal companyCost,
      final BigDecimal employeeCost) {
    setNumber(number);
    setImageUrl(imageUrl);
    setFullName(firstName.concat(" ").concat(lastName));
    setPlan(plan);
    setCoverage(coverage);
    setCompanyCost(companyCost);
    setEmployeeCost(employeeCost);
  }
}
