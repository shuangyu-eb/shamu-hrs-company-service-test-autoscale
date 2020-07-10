package shamu.company.attendance.repository;

import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.common.repository.BaseRepository;

public interface StaticTimesheetStatusRepository
    extends BaseRepository<StaticTimesheetStatus, String> {
  StaticTimesheetStatus findByName(String name);
}
