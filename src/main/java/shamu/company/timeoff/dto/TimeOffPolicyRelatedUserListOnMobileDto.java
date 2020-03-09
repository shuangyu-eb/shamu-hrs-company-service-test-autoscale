package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeOffPolicyRelatedUserListOnMobileDto {

  private Boolean isLimited;

  private List<TimeOffPolicyRelatedUserDto> allEmployeesList;

  private List<TimeOffPolicyRelatedUserDto> selectedUserList;
}
