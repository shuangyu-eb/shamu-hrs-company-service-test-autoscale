package shamu.company.financialengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shamu.company.financialengine.entity.FEAddresses;

public interface FEAddressRepository extends JpaRepository<FEAddresses, String> {}
