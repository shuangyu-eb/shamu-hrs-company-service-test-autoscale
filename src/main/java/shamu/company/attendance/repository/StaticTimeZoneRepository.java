package shamu.company.attendance.repository;

import java.util.List;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.repository.BaseRepository;

public interface StaticTimeZoneRepository extends BaseRepository<StaticTimezone, String> {
  @Override
  List<StaticTimezone> findAll();

}
