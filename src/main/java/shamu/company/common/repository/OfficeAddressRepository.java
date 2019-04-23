package shamu.company.common.repository;

import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;

public interface OfficeAddressRepository extends BaseRepository<OfficeAddress, Long> {

  OfficeAddress findOfficeAddressByOffice(Office office);
}
