package shamu.company.benefit.entity.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.dto.RetirementDto;
import shamu.company.benefit.dto.RetirementUserDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPayTypes;
import shamu.company.benefit.entity.RetirementPayment;
import shamu.company.common.mapper.Config;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface RetirementPaymentMapper {

  @Mapping(target = "employeeDeductionValue", expression = "java(updateCompensationCents(benefitPlanCreateDto.getDeductionValue()))")
  @Mapping(target = "companyMaximumContribution", expression = "java(updateCompensationCents(benefitPlanCreateDto.getAnnualMaximum()))")
  @Mapping(target = "companyContributionValue", expression = "java(updateCompensationCents(benefitPlanCreateDto.getContributionValue()))")
  RetirementPayment convertToRetirementPayment(BenefitPlanCreateDto benefitPlanCreateDto);

  @Mapping(target = "employeeDeductionValue", expression = "java(updateCompensationCents(benefitPlanUserCreateDto.getDeductionValue()))")
  @Mapping(target = "companyMaximumContribution", expression = "java(updateCompensationCents(benefitPlanUserCreateDto.getAnnualMaximum()))")
  @Mapping(target = "companyContributionValue", expression = "java(updateCompensationCents(benefitPlanUserCreateDto.getContributionValue()))")
  @Mapping(target = "limitStandard", source = "benefitPlanUserCreateDto.isDeductionLimit")
  @Mapping(target = "companyContribution", source = "companyContribution")
  @Mapping(target = "employeeDeduction", source = "employeeDeduction")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "benefitPlan", source = "benefitPlan")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  RetirementPayment convertToRetirementPayment(BenefitPlanUserCreateDto benefitPlanUserCreateDto,
      RetirementPayTypes employeeDeduction, RetirementPayTypes companyContribution, User user, BenefitPlan benefitPlan);

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "retirementUserDto", source = "retirementUserDto")
  BenefitPlanUserDto convertToBenefitPlanUserDto(User user, RetirementUserDto retirementUserDto);

  @Mapping(target = "companyContribution", source = "companyContribution.id")
  @Mapping(target = "employeeDeduction", source = "employeeDeduction.id")
  @Mapping(target = "annualMaximum", expression = "java(updateCompensationDollar(retirementPayment.getCompanyMaximumContribution()))")
  @Mapping(target = "contributionValue", expression = "java(updateCompensationDollar(retirementPayment.getCompanyContributionValue()))")
  @Mapping(target = "deductionValue", expression = "java(updateCompensationDollar(retirementPayment.getEmployeeDeductionValue()))")
  @Mapping(target = "isDeductionLimit", source = "limitStandard")
  RetirementUserDto convertToRetirementPayment(RetirementPayment retirementPayment);

  @Mapping(target = "retirement", source = "retirement")
  @Mapping(target = "id", source = "benefitPlan.id")
  @Mapping(target = "annualMaximum", expression = "java(updateCompensationDollar(retirementPayment.getCompanyMaximumContribution()))")
  @Mapping(target = "contributionValue", expression = "java(updateCompensationDollar(retirementPayment.getCompanyContributionValue()))")
  @Mapping(target = "deductionValue", expression = "java(updateCompensationDollar(retirementPayment.getEmployeeDeductionValue()))")
  BenefitPlanDto convertToBenefitPlanByRetirement(
      BenefitPlan benefitPlan, RetirementPayment retirementPayment, RetirementDto retirement);

  default BigInteger updateCompensationCents(final Double compensationWage) {
    if (compensationWage != null) {
      final BigDecimal bd = BigDecimal.valueOf(compensationWage * 100);
      return bd.toBigIntegerExact();
    }
    return null;
  }

  default Double updateCompensationDollar(final BigInteger compensationDollar) {
    if (compensationDollar != null) {
      return compensationDollar.doubleValue() / 100;
    }
    return null;
  }
}
