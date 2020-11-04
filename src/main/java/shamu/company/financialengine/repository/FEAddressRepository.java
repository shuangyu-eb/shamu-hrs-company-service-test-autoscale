package shamu.company.financialengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.financialengine.entity.FEAddresses;
import shamu.company.financialengine.entity.FEAddresses.FeAddressType;

public interface FEAddressRepository extends JpaRepository<FEAddresses, String> {

  FEAddresses findByOfficeAddressAndType(OfficeAddress officeAddress, FeAddressType type);
}
