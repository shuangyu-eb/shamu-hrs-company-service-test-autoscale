package shamu.company.employee.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;

public interface OfficeAddressRepository extends BaseRepository<OfficeAddress, Long> {
  OfficeAddress findOfficeAddressByOffice(Office office);
}
