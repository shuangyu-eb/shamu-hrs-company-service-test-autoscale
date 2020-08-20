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

  public List<TimeSheet> listByCompany(final String companyId) {
    return timeSheetRepository.listByCompanyId(companyId);
  }

  public Page<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final String companyId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timeSheetRepository.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, companyId, timeSheetStatus.getValue(), userId, pageable);
  }

  public Page<TimeSheet> findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final String companyId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timeSheetRepository.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, companyId, timeSheetStatus.getValue(), userId, pageable);
  }

  public List<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId,
      final String timePeriodId,
      final String companyId,
      final List<String> timeSheetStatus) {
    return timeSheetRepository.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, companyId, timeSheetStatus, userId);
  }

  public List<TimeSheet> findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId,
      final String timePeriodId,
      final String companyId,
      final List<String> timeSheetStatus) {
    return timeSheetRepository.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, companyId, timeSheetStatus, userId);
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

  public void updateCompanyLastPeriodTimeSheetsStatus(
      final String companyId, final String fromStatus, final String toStatus) {
    final TimePeriod lastTimePeriod = timePeriodService.findCompanyLastPeriod(companyId);
    final List<TimeSheet> timeSheets =
        findAllByPeriodId(lastTimePeriod.getId()).stream()
            .filter(timeSheet -> (timeSheet.getStatus().getName().equals(fromStatus)))
            .collect(Collectors.toList());

    updateAllTimesheetStatus(timeSheets, toStatus);
  }

  public List<TimeSheet> findAllById(final Iterable<String> iterable) {
    return timeSheetRepository.findAllById(iterable);
  }
}
