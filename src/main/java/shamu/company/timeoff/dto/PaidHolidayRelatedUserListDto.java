package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.job.dto.JobUserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaidHolidayRelatedUserListDto {

  private List<JobUserDto> paidHolidaySelectedEmployees;

  private List<JobUserDto> paidHolidayUnselectedEmployees;

}
