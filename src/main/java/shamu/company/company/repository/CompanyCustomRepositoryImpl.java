package shamu.company.company.repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.repository.TenantRepository;

@Repository
public class CompanyCustomRepositoryImpl implements CompanyCustomRepository {

  private final TenantRepository tenantRepository;

  public CompanyCustomRepositoryImpl(final TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  public Boolean existsByName(final String companyName) {
    return tenantRepository.existsByName(companyName);
  }
}
