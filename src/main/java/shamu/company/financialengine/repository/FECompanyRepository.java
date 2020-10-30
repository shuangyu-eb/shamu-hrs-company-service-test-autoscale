package shamu.company.financialengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shamu.company.financialengine.entity.FECompany;

public interface FECompanyRepository extends JpaRepository<FECompany, String> {

  FECompany findByCompanyId(String companyId);
}
