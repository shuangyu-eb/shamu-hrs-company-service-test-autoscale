package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

import java.util.List;

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
}
