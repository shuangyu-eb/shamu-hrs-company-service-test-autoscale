package shamu.company.timeoff.paidholiday;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaidHolidayDto {

  private List<PaidHoliday> holidays;

  private Integer employeeNum;
}
