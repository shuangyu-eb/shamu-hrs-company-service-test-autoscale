package shamu.company.attendance.dto;

import java.util.ArrayList;
import lombok.Data;

/** @author mshumaker */
@Data
public class OvertimeDetailDto {
  private String timeLogId;

  private Integer totalMinutes;

  private ArrayList<OverTimeMinutesDto> overTimeMinutesDtos;

  public void addMinuteDto(final OverTimeMinutesDto dto) {
    overTimeMinutesDtos.add(dto);
  }
}
