package shamu.company.timeoff.paidholiday;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaidHolidayPojo {

  @HashidsFormat
  private Long id;

  private Boolean isSelect;
}
