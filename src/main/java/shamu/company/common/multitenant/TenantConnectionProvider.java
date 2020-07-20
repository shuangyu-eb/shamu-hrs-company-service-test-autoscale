package shamu.company.common.multitenant;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.common.database.DataSourceConfig;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider {

  private static final long serialVersionUID = 7629057995597724761L;

  private final transient DataSource defaultDataSource;

  private final transient DataSourceConfig dataSourceConfig;

  @Autowired
  public TenantConnectionProvider(
      final DataSource defaultDatasource, final DataSourceConfig dataSourceConfig) {
    defaultDataSource = defaultDatasource;
    this.dataSourceConfig = dataSourceConfig;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return defaultDataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(final Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(final String tenantIdentifier) throws SQLException {
    final Connection connection = getAnyConnection();
    connection.setCatalog(tenantIdentifier);
    connection.setSchema(tenantIdentifier);
    return connection;
  }

  @Override
  public void releaseConnection(final String tenantIdentifier, final Connection connection)
      throws SQLException {
    connection.setSchema(dataSourceConfig.getDefaultSchema());
    connection.setCatalog(dataSourceConfig.getDefaultSchema());
    releaseAnyConnection(connection);
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(final Class unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(final Class<T> unwrapType) {
    return null;
  }
}
