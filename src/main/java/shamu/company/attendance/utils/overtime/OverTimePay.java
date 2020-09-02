package shamu.company.attendance.utils.overtime;

import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;

import java.util.ArrayList;
import java.util.List;

public interface OverTimePay {

  ArrayList<OvertimeDetailDto> getOvertimePay(List<LocalDateEntryDto> myHours);
}
