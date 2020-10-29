package shamu.company.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.attendance.dto.AttendanceDetailDto;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDetailDto {
  Boolean exists;

  AttendanceDetailDto payrollDetails;
}
