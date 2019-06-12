package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeOffPolicyRelatedUserListDto {

  private Boolean isLimited;

  private List<TimeOffPolicyRelatedUserDto> unSelectedUserList;

  private List<TimeOffPolicyRelatedUserDto> selectedUserList;
}
