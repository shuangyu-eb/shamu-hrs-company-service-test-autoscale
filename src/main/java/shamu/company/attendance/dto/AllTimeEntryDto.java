package shamu.company.attendance.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** @author mshumaker */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllTimeEntryDto {
  String entryId;

  String comments;

  List<AllTimeDto> allTimeLogs;

  public void addAllTimeLogs(final AllTimeDto dto) {
    allTimeLogs.add(dto);
  }
}
