package shamu.company.common.database;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.PostConstruct;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.CustomLiquibaseException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.service.TenantService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;

@Component
public class LiquibaseManager {

  private final DataSourceConfig dataSourceConfig;

  private final LiquibaseProperties tenantLiquibaseProperties;

  private final LiquibaseProperties companyLiquibaseProperties;

  private final ResourceLoader resourceLoader;

  private final TenantService tenantService;

  private final CompanyService companyService;

  private final StatesProvincesInitializer statesProvincesInitializer;

  public LiquibaseManager(
      final DataSourceConfig dataSourceConfig,
      final LiquibaseProperties tenantLiquibaseProperties,
      final LiquibaseProperties companyLiquibaseProperties,
      final ResourceLoader resourceLoader,
      final TenantService tenantService,
      final StatesProvincesInitializer statesProvincesInitializer,
      final CompanyService companyService) {
    this.dataSourceConfig = dataSourceConfig;
    this.tenantLiquibaseProperties = tenantLiquibaseProperties;
    this.companyLiquibaseProperties = companyLiquibaseProperties;
    this.resourceLoader = resourceLoader;
    this.tenantService = tenantService;
    this.statesProvincesInitializer = statesProvincesInitializer;
    this.companyService = companyService;
  }

  @PostConstruct
  private void setUpSchemas() {
    try {
      setUpDefaultSchema();
      setUpTenantsSchemas();
    } catch (final LiquibaseException e) {
      throw new CustomLiquibaseException("Error while running liquibase scripts.", e);
    } catch (final SQLException e) {
      throw new CustomLiquibaseException("Error while closing datasource.", e);
    }
  }

  private void setUpDefaultSchema() throws LiquibaseException, SQLException {
    final SpringLiquibase liquibase =
        getSpringLiquibase(dataSourceConfig.getDefaultSchema(), companyLiquibaseProperties);
    liquibase.afterPropertiesSet();
    liquibase.getDataSource().unwrap(HikariDataSource.class).close();
  }

  private void setUpTenantsSchemas() {
    final List<Tenant> tenants = tenantService.findAll();
    for (final Tenant tenant : tenants) {
      updateSchema(tenant);
    }
  }

  private void updateSchema(final Tenant tenant) {
    TenantContext.setCurrentTenant(tenant.getCompanyId());
    initSchema(tenant);
    TenantContext.clear();
  }

  private SpringLiquibase getSpringLiquibase(
      final String schema, final LiquibaseProperties liquibaseProperties) {
    final SpringLiquibase liquibase =
        LiquibaseUtils.constructLiquibase(
            dataSourceConfig.getDataSource(schema), schema, liquibaseProperties);
    liquibase.setResourceLoader(resourceLoader);
    return liquibase;
  }

  public void addSchema(final String companyId, final String companyName) {
    TenantContext.setCurrentTenant(companyId);
    final Company company = new Company(companyId);

    company.setName(companyName);
    initSchema(Tenant.builder().companyId(companyId).build());
    companyService.save(company);
    TenantContext.clear();
  }

  private void initSchema(final Tenant tenant) {
    final String companyId = tenant.getCompanyId();
    final String databaseName = dataSourceConfig.getSchemaByCompanyId(companyId);
    final SpringLiquibase liquibase = getSpringLiquibase(databaseName, tenantLiquibaseProperties);
    try {
      liquibase.afterPropertiesSet();
      liquibase.getDataSource().unwrap(HikariDataSource.class).close();
      statesProvincesInitializer.run();
    } catch (final LiquibaseException | SQLException e) {
      throw new CustomLiquibaseException("Error while running liquibase scripts.", e);
    }
  }
}
