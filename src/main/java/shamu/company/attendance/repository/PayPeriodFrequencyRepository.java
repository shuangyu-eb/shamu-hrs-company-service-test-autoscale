package shamu.company.attendance.repository;

import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface PayPeriodFrequencyRepository
    extends BaseRepository<StaticCompanyPayFrequencyType, String> {

  @Override
  List<StaticCompanyPayFrequencyType> findAll();

  @Override
  Optional<StaticCompanyPayFrequencyType> findById(String id);

  StaticCompanyPayFrequencyType findByName(String name);
}
