package shamu.company.common.repository;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.employee.entity.EmploymentType;

public interface EmploymentTypeRepository extends BaseRepository<EmploymentType, Long> {

  List<EmploymentType> findAllByCompany(Company company);
}
