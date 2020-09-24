package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceTeamHoursDto {

  private String id;

  private String userId;

  private String userName;

  private Integer workedMinutes;

  private Integer overTimeMinutes;

  private String overtimeStatus;
}
