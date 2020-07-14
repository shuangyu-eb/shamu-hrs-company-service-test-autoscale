package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OverTimePayFactory;
import shamu.company.utils.DateUtil;

/** @author mshumaker */
@Service
public class OvertimeService {

  private static final int CONVERT_SECOND_TO_MS = 1000;

  private final GenericHoursService genericHoursService;

  public OvertimeService(final GenericHoursService genericHoursService) {
    this.genericHoursService = genericHoursService;
  }

  public List<LocalDateEntryDto> getLocalDateEntries(
      final TimeSheet timeSheet, final CompanyTaSetting companyTaSetting) {
    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    // start of 7th consecutive day
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final String userId = timeSheet.getEmployee().getId();
    // get hours from start of week through end of period
    final List<EmployeeTimeLog> allEmployeeEntries =
        genericHoursService.findEntriesBetweenDates(
            startOfTimesheetWeek * CONVERT_SECOND_TO_MS, timesheetEnd.getTime(), userId, true);
    // calculate the OT hours based off company's timezone
    return TimeEntryUtils.transformTimeLogsToLocalDate(
        allEmployeeEntries, companyTaSetting.getTimeZone());
  }

  public List<OvertimeDetailDto> getOvertimeEntries(
      final List<LocalDateEntryDto> localDateEntries,
      final TimeSheet timeSheet,
      final CompanyTaSetting companyTaSetting) {

    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final List<OvertimeDetailDto> overtimeDetailDtos =
        OverTimePayFactory.getOverTimePay(timeSheet.getUserCompensation())
            .getOvertimePay(localDateEntries);
    return filterOvertimeEntries(
        overtimeDetailDtos,
        timeSheetStart.getTime(),
        timesheetEnd.getTime(),
        companyTaSetting.getTimeZone());
  }

  private List<OvertimeDetailDto> filterOvertimeEntries(
      final List<OvertimeDetailDto> overtimeDetails,
      final long start,
      final long end,
      final StaticTimezone timezone) {
    for (final OvertimeDetailDto overtimeDetailDto : overtimeDetails) {
      final ArrayList<OverTimeMinutesDto> otMinDtos =
          overtimeDetailDto.getOverTimeMinutesDtos().stream()
              .filter(
                  overTimeMinutesDto -> {
                    long startOfOt =
                        overTimeMinutesDto
                            .getStartTime()
                            .atZone(ZoneId.of(timezone.getName()))
                            .toInstant()
                            .toEpochMilli();
                    return (startOfOt > start && startOfOt < end);
                  })
              .collect(Collectors.toCollection(ArrayList::new));
      final int otMin =
          otMinDtos.isEmpty()
              ? 0
              : otMinDtos.stream().mapToInt(OverTimeMinutesDto::getMinutes).sum();
      overtimeDetailDto.setTotalMinutes(otMin);
      overtimeDetailDto.setOverTimeMinutesDtos(otMinDtos);
    }
    return overtimeDetails.stream()
        .filter(overtimeDetailDto -> overtimeDetailDto.getTotalMinutes() > 0)
        .collect(Collectors.toList());
  }
}
