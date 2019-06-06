package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.PaidHoliday;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaidHolidayDto {

  private List<PaidHoliday> holidays;

  private Integer employeeNum;
}
