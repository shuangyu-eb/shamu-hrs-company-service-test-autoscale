package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;

@Transactional
@Service
public class StaticTimesheetStatusService {

  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  public StaticTimesheetStatusService(
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository) {
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
  }

  public StaticTimesheetStatus findByName(final String statusName) {
    return staticTimesheetStatusRepository.findByName(statusName);
  }
}
