package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaidHolidayRelatedUserListMobileDto {

  private List<TimeOffPolicyRelatedUserDto> paidHolidaySelectedEmployees;

  private List<TimeOffPolicyRelatedUserDto> paidHolidayEmployees;
}
