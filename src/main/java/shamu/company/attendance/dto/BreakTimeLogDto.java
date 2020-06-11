package shamu.company.attendance.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BreakTimeLogDto{
    private Timestamp breakStart;

    private Integer breakMin;

    private String timeType;
}
