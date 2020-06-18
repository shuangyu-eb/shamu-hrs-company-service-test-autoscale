package shamu.company.attendance.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
public class MyHoursEntryDto {
  private Timestamp date;

  private String comments;

  List<PayDetailDto> payDetailDtos;
}
