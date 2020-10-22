package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class BenefitPlanUserDto {

  private String id;

  private String firstName;

  private String lastName;

  private String imageUrl;

  private String department;

  private String jobTitle;

  private String employmentType;

  private String coverageId;

  private RetirementUserDto retirementUserDto;
}
