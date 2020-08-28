package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class UserAttendanceEnrollInfoDto {

  private Boolean isEnrolled;

  private Timestamp deactivatedAt;
}
