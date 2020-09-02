package shamu.company.attendance.repository;


import shamu.company.attendance.entity.StaticOvertimeType;
import shamu.company.common.repository.BaseRepository;

public interface StaticOvertimeTypeRepository extends BaseRepository<StaticOvertimeType, String> {
    StaticOvertimeType findByName(String overtimeType);

}
