package shamu.company.attendance.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

@Service
public class TimeSheetService {

  private final TimeSheetRepository timeSheetRepository;

  public TimeSheetService(final TimeSheetRepository timeSheetRepository) {
    this.timeSheetRepository = timeSheetRepository;
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
      final String timesheetId,
      final String companyId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    return timeSheetRepository.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timesheetId, companyId, timeSheetStatus.getValue(), userId, pageable);
  }

  public List<TimeSheet> findTimeSheetsByIdAndCompanyIdAndStatus(
      final String userId,
      final String timesheetId,
      final String companyId,
      final List<String> timeSheetStatus) {
    return timeSheetRepository.findTimeSheetsByIdAndCompanyIdAndStatus(
        timesheetId, companyId, timeSheetStatus, userId);
  }

  public void updateTimesheetStatus(final String statusId, final String timesheetId) {
    timeSheetRepository.updateTimesheetStatus(statusId, timesheetId);
  }
}
