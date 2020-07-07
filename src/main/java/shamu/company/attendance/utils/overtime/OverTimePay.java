package shamu.company.attendance.utils.overtime;

import java.util.ArrayList;
import java.util.List;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;

public interface OverTimePay {

  ArrayList<OvertimeDetailDto> getOvertimePay(List<LocalDateEntryDto> myHours);
}
