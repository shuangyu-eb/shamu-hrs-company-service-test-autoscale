package shamu.company.attendance.utils.overtime;

import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mshumaker
 */

public class NoOverTimePay implements OverTimePay{
    @Override
    public ArrayList<OvertimeDetailDto> getOvertimePay(List<LocalDateEntryDto> myHours) {
        return new ArrayList<>();
    }
}
