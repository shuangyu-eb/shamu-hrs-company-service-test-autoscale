package shamu.company.common.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.database.DataSourceConfig;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.TenantRepository;

@Service
@Transactional
public class TenantService {

  private final TenantRepository tenantRepository;

  private final DataSourceConfig dataSourceConfig;

  public TenantService(
      final TenantRepository tenantRepository, final DataSourceConfig dataSourceConfig) {
    this.tenantRepository = tenantRepository;
    this.dataSourceConfig = dataSourceConfig;
  }

  public void save(final Tenant tenant) {
    tenantRepository.save(tenant);
  }

  public boolean isCompanyExists(final String id) {
    return tenantRepository.isCompanyExists(id);
  }

  public Tenant findByCompanyId(final String id) {
    return tenantRepository
        .findByCompanyId(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Company record with id %s not fond: id", id), id, "company"));
  }

  public List<Tenant> findAll() {
    return tenantRepository.findAll();
  }

  public Set<String> findAllSchemaNames() {
    final List<Tenant> entities = tenantRepository.findAll();
    return entities.stream()
        .map(Tenant::getCompanyId)
        .map(dataSourceConfig::getSchemaByCompanyId)
        .collect(Collectors.toSet());
  }

  public List<Tenant> findAllByCompanyId(final List<String> ids) {
    return tenantRepository.findAllByCompanyId(ids);
  }

  public Tenant findTenantByUserEmailWork(final String emailWork) {
    return tenantRepository
        .findTenantByUserEmailWork(emailWork)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("User with id %s not found.", emailWork), emailWork, "job"));
  }

  public void deleteTenant(final String companyId) {
    tenantRepository.delete(companyId);
  }
}
