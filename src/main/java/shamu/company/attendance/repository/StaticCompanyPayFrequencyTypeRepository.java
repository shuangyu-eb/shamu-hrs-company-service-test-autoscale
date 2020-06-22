package shamu.company.attendance.repository;

import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.common.repository.BaseRepository;

public interface StaticCompanyPayFrequencyTypeRepository extends BaseRepository<StaticCompanyPayFrequencyType, String> {
    StaticCompanyPayFrequencyType findByName(String name);
}
