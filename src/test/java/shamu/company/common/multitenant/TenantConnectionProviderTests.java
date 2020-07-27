package shamu.company.common.multitenant;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.database.DataSourceConfig;

class TenantConnectionProviderTests {

  @Mock private DataSourceConfig dataSourceConfig;

  @Mock private DataSource dataSource;

  private TenantConnectionProvider tenantConnectionProvider;

  @BeforeEach
  void init() throws SQLException {
    MockitoAnnotations.initMocks(this);
    tenantConnectionProvider = new TenantConnectionProvider(dataSource, dataSourceConfig);
    Mockito.when(dataSource.getConnection()).thenReturn(Mockito.mock(Connection.class));
  }

  @Test
  void testGetConnection() throws SQLException {
    final String tenantIdentifier = "1";
    final Connection connection = tenantConnectionProvider.getConnection(tenantIdentifier);
    Mockito.verify(connection, Mockito.times(1)).setSchema(tenantIdentifier);
    Mockito.verify(connection, Mockito.times(1)).setCatalog(tenantIdentifier);
  }

  @Test
  void testReleaseConnection() throws SQLException {
    final String tenantIdentifier = "1";
    final Connection connection = Mockito.mock(Connection.class);
    tenantConnectionProvider.releaseConnection(tenantIdentifier, connection);
    Mockito.verify(connection, Mockito.times(1)).close();
  }

  @Test
  void testSupportsAggressiveRelease() {
    final boolean result = tenantConnectionProvider.supportsAggressiveRelease();
    Assertions.assertThat(result).isFalse();
  }

  @Test
  void testIsUnwrappableAs() {
    final boolean result = tenantConnectionProvider.isUnwrappableAs(Object.class);
    Assertions.assertThat(result).isFalse();
  }

  @Test
  void testUnwrap() {
    final Object result = tenantConnectionProvider.unwrap(Object.class);
    Assertions.assertThat(result).isNull();
  }
}
