package shamu.company.attendance.repository;

import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.common.repository.BaseRepository;

public interface StaticEmployeesTaTimeTypeRepository extends BaseRepository<StaticEmployeesTaTimeType, String> {

    StaticEmployeesTaTimeType findByName(String timeTypeName);
}
