package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendancePolicyAndDetailDto {

  private TimeAndAttendanceDetailsDto attendanceDetails;

  private List<NewOvertimePolicyDto> overtimePolicyDetails;
}
