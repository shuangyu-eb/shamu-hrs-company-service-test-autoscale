package shamu.company.common.multitenant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.database.DataSourceConfig;

class TenantIdentifierResolverTests {

  @Mock private DataSourceConfig dataSourceConfig;

  private TenantIdentifierResolver tenantIdentifierResolver;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    tenantIdentifierResolver = new TenantIdentifierResolver(dataSourceConfig);
  }

  @Nested
  class ResolveCurrentTenantIdentifier {

    @Test
    void whenNotCurrentIdentifier_thenReturnDefault() {
      TenantContext.clear();
      tenantIdentifierResolver.resolveCurrentTenantIdentifier();
      Mockito.verify(dataSourceConfig, Mockito.times(1)).getDefaultSchema();
    }

    @Test
    void whenHaveCurrentIdentifier_thenReturnSpecified() {
      final String tenantIdentifier = "1";
      TenantContext.setCurrentTenant(tenantIdentifier);
      tenantIdentifierResolver.resolveCurrentTenantIdentifier();
      Mockito.verify(dataSourceConfig, Mockito.times(1)).getSchemaByCompanyId(tenantIdentifier);
    }
  }

  @Test
  void testValidateExistingCurrentSessions() {
    final boolean result = tenantIdentifierResolver.validateExistingCurrentSessions();
    Assertions.assertThat(result).isTrue();
  }
}
