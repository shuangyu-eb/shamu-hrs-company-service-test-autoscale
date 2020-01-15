package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;

@Data
public class SelectedEnrollmentInfoDto {

  private String id;

  private String planId;

  private String otherTypePlanTitle;

  private String coverageOptionId;

  private String benefitPlanType;

  private List<BenefitPlanUserDto> selectedDependents;
}
