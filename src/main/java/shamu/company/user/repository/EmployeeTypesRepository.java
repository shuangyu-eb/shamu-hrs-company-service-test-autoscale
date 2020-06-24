package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.EmployeeType;

import java.util.List;

public interface EmployeeTypesRepository extends BaseRepository<EmployeeType, String> {
  @Override
  List<EmployeeType> findAll();
}
