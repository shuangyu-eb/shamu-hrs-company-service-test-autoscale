package shamu.company.common.multitenant;

import org.apache.commons.lang.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import shamu.company.common.database.DataSourceConfig;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

  private final DataSourceConfig dataSourceConfig;

  public TenantIdentifierResolver(final DataSourceConfig dataSourceConfig) {
    this.dataSourceConfig = dataSourceConfig;
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    if (StringUtils.isBlank(TenantContext.getCurrentTenant())) {
      return dataSourceConfig.getDefaultSchema();
    }

    return dataSourceConfig.getSchemaByCompanyId(TenantContext.getCurrentTenant());
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
