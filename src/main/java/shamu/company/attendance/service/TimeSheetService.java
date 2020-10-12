package shamu.company.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.repository.TimesheetRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.UserCompensation;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeSheetService {

  private final TimesheetRepository timesheetRepository;
  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;
  private final TimePeriodService timePeriodService;

  public TimeSheetService(
      final TimesheetRepository timesheetRepository,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository,
      final TimePeriodService timePeriodService) {
    this.timesheetRepository = timesheetRepository;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
    this.timePeriodService = timePeriodService;
  }

  public boolean existByUser(final String userId) {
    return timesheetRepository.existsByEmployeeId(userId);
  }

  public Timesheet findTimeSheetById(final String timeSheetId) {
    return timesheetRepository
        .findById(timeSheetId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Time sheet with id %s not found!", timeSheetId),
                    timeSheetId,
                    "time sheet"));
  }

  public List<Timesheet> saveAll(final List<Timesheet> timesheets) {
    return timesheetRepository.saveAll(timesheets);
  }

  public List<Timesheet> findAll() {
    return timesheetRepository.findAll();
  }

  public Page<Timesheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final List<String> timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timesheetRepository.findTeamTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus, userId, pageable);
  }

  public Page<Timesheet> findCompanyTimeSheetsByIdAndStatus(
      final String timePeriodId, final List<String> timeSheetStatus, final Pageable pageable) {
    return timesheetRepository.findCompanyTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus, pageable);
  }

  public List<Timesheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId, final String timePeriodId, final List<String> timeSheetStatus) {
    return timesheetRepository.findTeamTimeSheetsByIdAndStatus(
        timePeriodId, timeSheetStatus, userId);
  }

  public List<Timesheet> findCompanyTimeSheetsByIdAndStatus(
      final String timePeriodId, final List<String> timeSheetStatus) {
    return timesheetRepository.findCompanyTimeSheetsByIdAndStatus(timePeriodId, timeSheetStatus);
  }

  public void updateTimesheetStatus(final String statusId, final String timesheetId) {
    timesheetRepository.updateTimesheetStatus(statusId, timesheetId);
  }

  public void updateTimesheetStatusByPeriodId(
      final String fromStatus, final String toStatus, final String periodId) {
    timesheetRepository.updateTimesheetStatusByPeriodId(fromStatus, toStatus, periodId);
  }

  public void updateTimesheetStatusByPeriodIdAndManagerId(
      final String fromStatus,
      final String toStatus,
      final String periodId,
      final String managerId) {
    timesheetRepository.updateTimesheetStatusByPeriodIdAndManagerId(
        fromStatus, toStatus, periodId, managerId);
  }

  public List<Timesheet> findActiveByPeriodId(final String periodId) {
    return timesheetRepository.findAllByTimePeriodIdAndRemovedAtIsNull(periodId);
  }

  private void updateAllTimesheetStatus(final List<Timesheet> timesheets, final String status) {
    final StaticTimesheetStatus submitStatus = staticTimesheetStatusRepository.findByName(status);
    for (final Timesheet timesheet : timesheets) {
      timesheet.setStatus(submitStatus);
    }
    timesheetRepository.saveAll(timesheets);
  }

  public void updateCompanyTimeSheetsStatus(
      final String fromStatus, final String toStatus, final String timePeriodId) {
    final TimePeriod timePeriod = timePeriodService.findById(timePeriodId);
    final List<Timesheet> timesheets =
        findActiveByPeriodId(timePeriod.getId()).stream()
            .filter(timesheet -> (timesheet.getStatus().getName().equals(fromStatus)))
            .collect(Collectors.toList());

    updateAllTimesheetStatus(timesheets, toStatus);
  }

  public List<Timesheet> findAllById(final Iterable<String> iterable) {
    return timesheetRepository.findAllById(iterable);
  }

  public Timesheet findActiveByPeriodAndUser(final String periodId, final String userId) {
    return timesheetRepository.findByTimePeriodIdAndEmployeeIdAndRemovedAtIsNull(periodId, userId);
  }

  public Optional<Timesheet> findByPeriodAndUser(final String periodId, final String userId) {
    return timesheetRepository.findByTimePeriodIdAndEmployeeId(periodId, userId);
  }

  public void removeUserFromAttendance(final List<String> userIds) {
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod();
    final Timestamp currentTime = DateUtil.getCurrentTime();
    final List<Timesheet> timesheets =
        timesheetRepository.findAllByTimePeriodIdAndEmployeeId(timePeriod.getId(), userIds);
    timesheets.forEach(timesheet -> timesheet.setRemovedAt(currentTime));
    timesheetRepository.saveAll(timesheets);
  }

  public int findTeamHoursPendingCount(final String userId, final String periodId) {
    return timesheetRepository.findTeamHoursPendingCount(
        periodId, TimeSheetStatus.SUBMITTED.name(), userId);
  }

  public int findCompanyHoursPendingCount(final String periodId) {
    return timesheetRepository.findCompanyHoursPendingCount(
        periodId, TimeSheetStatus.SUBMITTED.name());
  }

  public Timesheet findCurrentByUseCompensation(final UserCompensation userCompensation) {
    return timesheetRepository.findCurrentRecordByUserCompensationId(userCompensation.getId());
  }

  @Transactional
  public void updateCurrentOvertimePolicyByUser(final UserCompensation compensation) {
    final Timesheet timesheet = getCurrentTimesheet(compensation.getUserId());
    timesheet.setUserCompensation(compensation);
  }

  private Timesheet getCurrentTimesheet(final String userId) {
    return timesheetRepository.findCurrentTimesheetByUser(userId);
  }
}
