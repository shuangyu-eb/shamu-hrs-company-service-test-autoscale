package shamu.company.benefit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BenefitPlanRelatedUserListDto {

  private List<BenefitPlanUserDto> unSelectedUserList;

  private List<BenefitPlanUserDto> selectedUserList;
}
