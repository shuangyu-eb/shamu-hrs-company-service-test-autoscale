package shamu.company.attendance.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;


public interface PayPeriodFrequencyRepository extends BaseRepository<StaticCompanyPayFrequencyType, String> {

    List<StaticCompanyPayFrequencyType> findAll();
}
