package shamu.company.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

import java.util.List;

@Service
public class TimeSheetService {

  private final TimeSheetRepository timeSheetRepository;
  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  public TimeSheetService(
      final TimeSheetRepository timeSheetRepository,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository) {
    this.timeSheetRepository = timeSheetRepository;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
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

  public List<TimeSheet> findTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId,
      final String timePeriodId,
      final String companyId,
      final List<String> timeSheetStatus) {
    return timeSheetRepository.findTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, companyId, timeSheetStatus, userId);
  }

  public void updateTimesheetStatus(final String statusId, final String timesheetId) {
    timeSheetRepository.updateTimesheetStatus(statusId, timesheetId);
  }

  public List<TimeSheet> findAllByPeriodId(final String periodId) {
    return timeSheetRepository.findAllByTimePeriodId(periodId);
  }

  public void updateAllTimesheetStatus(final List<TimeSheet> timeSheets) {
    final StaticTimesheetStatus submitStatus =
        staticTimesheetStatusRepository.findByName(TimeSheetStatus.SUBMITTED.name());
    for (final TimeSheet timeSheet : timeSheets) {
      timeSheet.setStatus(submitStatus);
    }
    timeSheetRepository.saveAll(timeSheets);
  }
}
