package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.utils.TimeEntryUtils;

@Service
public class GenericHoursService {

  private static final int CONVERT_MIN_TO_MS = 60 * 1000;

  private final EmployeeTimeLogRepository employeeTimeLogRepository;

  public GenericHoursService(final EmployeeTimeLogRepository employeeTimeLogRepository) {
    this.employeeTimeLogRepository = employeeTimeLogRepository;
  }

  public List<EmployeeTimeLog> findEntriesBetweenDates(
      final long startDate, final long endDate, final String userId, final boolean isWorkEntries) {
    final List<EmployeeTimeLog> employeeTimeLogs =
        employeeTimeLogRepository.findEmployeeTimeLogByTime(startDate, endDate, userId);
    employeeTimeLogs.forEach(
        employeeTimeLog -> {
          final long timeStart = employeeTimeLog.getStart().getTime();
          final long timeEnd = timeStart + employeeTimeLog.getDurationMin() * CONVERT_MIN_TO_MS;
          final int durationMin = employeeTimeLog.getDurationMin();
          final long amountOfTimeBeforeStart = Math.max(startDate - timeStart, 0);
          final long amountOfTimeAfterEnd = Math.max(timeEnd - endDate, 0);
          employeeTimeLog.setStart(new Timestamp(timeStart + amountOfTimeBeforeStart));
          employeeTimeLog.setDurationMin(
              (int)
                  ((durationMin * CONVERT_MIN_TO_MS
                          - amountOfTimeAfterEnd
                          - amountOfTimeBeforeStart)
                      / (CONVERT_MIN_TO_MS)));
        });

    employeeTimeLogs.sort(TimeEntryUtils.compareByLogStartDate);

    if (isWorkEntries) {
      return employeeTimeLogs.stream()
          .filter(
              employeeTimeLog ->
                  employeeTimeLog
                      .getTimeType()
                      .getName()
                      .equals(StaticEmployeesTaTimeType.TimeType.WORK.name()))
          .collect(Collectors.toList());
    }

    return employeeTimeLogs;
  }
}
