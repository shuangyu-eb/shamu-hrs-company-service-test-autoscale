package shamu.company.attendance.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamHoursPageInfoDto {

  List<AttendanceTeamHoursDto> content;

  Integer totalPages;

}
