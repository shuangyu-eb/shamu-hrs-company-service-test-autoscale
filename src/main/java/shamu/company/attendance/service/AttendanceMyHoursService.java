package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@Service
@Transactional
public class AttendanceMyHoursService {
  private static final int CONVERT_MIN_TO_MS = 60 * 1000;
  private final EmployeeTimeEntryRepository employeeTimeEntryRepository;

  private final EmployeeTimeLogRepository employeeTimeLogRepository;

  private final StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  private final UserService userService;

  public AttendanceMyHoursService(
      EmployeeTimeEntryRepository employeeTimeEntryRepository,
      EmployeeTimeLogRepository employeeTimeLogRepository,
      StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository,
      UserService userService) {
    this.employeeTimeEntryRepository = employeeTimeEntryRepository;
    this.employeeTimeLogRepository = employeeTimeLogRepository;
    this.staticEmployeesTaTimeTypeRepository = staticEmployeesTaTimeTypeRepository;
    this.userService = userService;
  }

  public void saveTimeEntry(final String userId, final TimeEntryDto timeEntryDto) {
    User user = userService.findById(userId);
    EmployeeTimeEntry employeeTimeEntry =
        EmployeeTimeEntry.builder().employee(user).comment(timeEntryDto.getComment()).build();
    EmployeeTimeEntry savedEntry = employeeTimeEntryRepository.save(employeeTimeEntry);
    saveTimeLogs(timeEntryDto, savedEntry);
  }

  private void saveTimeLogs(TimeEntryDto timeEntryDto, EmployeeTimeEntry savedEntry) {

    List<BreakTimeLogDto> breakTimeLogDtos = timeEntryDto.getBreakTimeLogs();

    StaticEmployeesTaTimeType breakType =
        staticEmployeesTaTimeTypeRepository.findByName(
            StaticEmployeesTaTimeType.TimeType.BREAK.name());
    StaticEmployeesTaTimeType workType =
        staticEmployeesTaTimeTypeRepository.findByName(
            StaticEmployeesTaTimeType.TimeType.WORK.name());

    List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();

    if (!breakTimeLogDtos.isEmpty()) {

      breakTimeLogDtos.sort(
          (o1, o2) -> (int) (o1.getBreakStart().getTime() - o2.getBreakStart().getTime()));
      // break time
      for (BreakTimeLogDto breakTimeLogDto : breakTimeLogDtos) {
        EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
        employeeTimeLog.setStart(breakTimeLogDto.getBreakStart());
        employeeTimeLog.setTimeType(breakType);
        employeeTimeLog.setDurationMin(breakTimeLogDto.getBreakMin());
        employeeTimeLog.setEntry(savedEntry);
        employeeTimeLogs.add(employeeTimeLog);
      }

      // work time between break
      for (int i = 0; i < breakTimeLogDtos.size() - 1; i++) {
        EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
        long workStartTimeMs = breakTimeLogDtos.get(i).getBreakStart().getTime()
                    + breakTimeLogDtos.get(i).getBreakMin() * CONVERT_MIN_TO_MS;
        Timestamp workStartTime = DateUtil.longToTimestamp(workStartTimeMs);
        long durationMin =
            (breakTimeLogDtos.get(i + 1).getBreakStart().getTime() - workStartTime.getTime())
                / (CONVERT_MIN_TO_MS);
        employeeTimeLog.setStart(workStartTime);
        employeeTimeLog.setDurationMin((int) durationMin);
        employeeTimeLog.setTimeType(workType);
        employeeTimeLog.setEntry(savedEntry);
        employeeTimeLogs.add(employeeTimeLog);
      }

      // work time outside breaks

      //first work start time to first break time
      EmployeeTimeLog startTimeLog = new EmployeeTimeLog();
      long startDurationMin =
          (breakTimeLogDtos.get(0).getBreakStart().getTime()
                  - timeEntryDto.getStartTime().getTime())
              / (CONVERT_MIN_TO_MS);
      startTimeLog.setStart(timeEntryDto.getStartTime());
      startTimeLog.setDurationMin((int) startDurationMin);
      startTimeLog.setTimeType(workType);
      startTimeLog.setEntry(savedEntry);
      employeeTimeLogs.add(startTimeLog);

      //last work start time to end time
      EmployeeTimeLog endTimeLog = new EmployeeTimeLog();
      Timestamp lastBreakTime = breakTimeLogDtos.get(breakTimeLogDtos.size() - 1).getBreakStart();
      int lastBreakDurationMin = breakTimeLogDtos.get(breakTimeLogDtos.size() - 1).getBreakMin();
      long lastWorkStartTimeMs = lastBreakTime.getTime() + lastBreakDurationMin * CONVERT_MIN_TO_MS;
      Timestamp lastWorkStartTime = DateUtil.longToTimestamp(lastWorkStartTimeMs);
      long endDurationMin =
          (timeEntryDto.getEndTime().getTime() - lastWorkStartTime.getTime()) / (CONVERT_MIN_TO_MS);
      endTimeLog.setStart(lastWorkStartTime);
      endTimeLog.setDurationMin((int) endDurationMin);
      endTimeLog.setTimeType(workType);
      endTimeLog.setEntry(savedEntry);
      employeeTimeLogs.add(endTimeLog);
    }

    // do not have break
    if (breakTimeLogDtos.isEmpty()) {
      EmployeeTimeLog timeLog = new EmployeeTimeLog();
      int durationMin = timeEntryDto.getHoursWorked() * 60 + timeEntryDto.getMinutesWorked();
      timeLog.setStart(timeEntryDto.getStartTime());
      timeLog.setDurationMin(durationMin);
      timeLog.setEntry(savedEntry);
      timeLog.setTimeType(workType);
      employeeTimeLogs.add(timeLog);
    }

    employeeTimeLogRepository.saveAll(employeeTimeLogs);
  }
}
