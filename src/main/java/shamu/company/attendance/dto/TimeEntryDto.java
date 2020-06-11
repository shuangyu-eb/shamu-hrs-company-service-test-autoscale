package shamu.company.attendance.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeEntryDto {
    private Timestamp startTime;

    private Timestamp endTime;

    private Integer hoursWorked;

    private Integer minutesWorked;

    private String comment;

    List<BreakTimeLogDto> breakTimeLogs;
}
