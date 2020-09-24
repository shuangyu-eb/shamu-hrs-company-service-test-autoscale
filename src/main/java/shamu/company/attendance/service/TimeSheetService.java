package shamu.company.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.UserCompensation;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeSheetService {

  private final TimeSheetRepository timeSheetRepository;
  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;
  private final TimePeriodService timePeriodService;

  public TimeSheetService(
      final TimeSheetRepository timeSheetRepository,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository,
      final TimePeriodService timePeriodService) {
    this.timeSheetRepository = timeSheetRepository;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
    this.timePeriodService = timePeriodService;
  }

  public boolean existByUser(final String userId) {
    return timeSheetRepository.existsByEmployeeId(userId);
  }

  public TimeSheet findTimeSheetById(final String timeSheetId) {
    return timeSheetRepository
        .findById(timeSheetId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Time sheet with id %s not found!", timeSheetId),
                    timeSheetId,
                    "time sheet"));
  }

  public List<TimeSheet> saveAll(final List<TimeSheet> timeSheets) {
    return timeSheetRepository.saveAll(timeSheets);
  }

  public List<TimeSheet> findAll() {
    return timeSheetRepository.findAll();
  }

  public Page<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timeSheetRepository.findTeamTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus.getValue(), userId, pageable);
  }

  public Page<TimeSheet> findCompanyTimeSheetsByIdAndStatus(
      final String timePeriodId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timeSheetRepository.findCompanyTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus.getValue(), userId, pageable);
  }

  public List<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId, final String timePeriodId, final List<String> timeSheetStatus) {
    return timeSheetRepository.findTeamTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus, userId);
  }

  public List<TimeSheet> findCompanyTimeSheetsByIdAndStatus(
      final String userId, final String timePeriodId, final List<String> timeSheetStatus) {
    return timeSheetRepository.findCompanyTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus, userId);
  }

  public void updateTimesheetStatus(final String statusId, final String timesheetId) {
    timeSheetRepository.updateTimesheetStatus(statusId, timesheetId);
  }

  public List<TimeSheet> findAllByPeriodId(final String periodId) {
    return timeSheetRepository.findAllByTimePeriodId(periodId);
  }

  private void updateAllTimesheetStatus(final List<TimeSheet> timeSheets, final String status) {
    final StaticTimesheetStatus submitStatus = staticTimesheetStatusRepository.findByName(status);
    for (final TimeSheet timeSheet : timeSheets) {
      timeSheet.setStatus(submitStatus);
    }
    timeSheetRepository.saveAll(timeSheets);
  }

  public void updateCompanyTimeSheetsStatus(
      final String fromStatus, final String toStatus, final String timePeriodId) {
    final TimePeriod timePeriod = timePeriodService.findById(timePeriodId);
    final List<TimeSheet> timeSheets =
        findAllByPeriodId(timePeriod.getId()).stream()
            .filter(timeSheet -> (timeSheet.getStatus().getName().equals(fromStatus)))
            .collect(Collectors.toList());

    updateAllTimesheetStatus(timeSheets, toStatus);
  }

  public List<TimeSheet> findAllById(final Iterable<String> iterable) {
    return timeSheetRepository.findAllById(iterable);
  }

  public TimeSheet findTimeSheetByPeriodAndUser(final String periodId, final String userId) {
    return timeSheetRepository.findByTimePeriodIdAndEmployeeIdAndRemovedAtIsNull(periodId, userId);
  }

  public void removeUserFromAttendance(final List<String> userIds) {
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod();
    final Timestamp currentTime = DateUtil.getCurrentTime();
    final List<TimeSheet> timeSheets =
        timeSheetRepository.findAllByTimePeriodIdAndEmployeeId(timePeriod.getId(), userIds);
    timeSheets.forEach(timeSheet -> timeSheet.setRemovedAt(currentTime));
    timeSheetRepository.saveAll(timeSheets);
  }

  public int findTeamHoursPendingCount(final String userId, final String periodId) {
    return timeSheetRepository.findTeamHoursPendingCount(
        periodId, TimeSheetStatus.SUBMITTED.name(), userId);
  }

  public int findCompanyHoursPendingCount(final String periodId) {
    return timeSheetRepository.findCompanyHoursPendingCount(
        periodId, TimeSheetStatus.SUBMITTED.name());
  }

  public TimeSheet findByUseCompensation(final UserCompensation userCompensation) {
    return timeSheetRepository.findByUserCompensationId(userCompensation.getId());
  }
}
