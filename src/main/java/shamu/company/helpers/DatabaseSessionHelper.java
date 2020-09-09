package shamu.company.helpers;

import javax.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import shamu.company.common.config.DataSourceConfig;

@Component
public class DatabaseSessionHelper {

  private final DataSourceConfig dataSourceConfig;

  private final EntityManagerFactory entityManagerFactory;

  public DatabaseSessionHelper(
      final DataSourceConfig dataSourceConfig, final EntityManagerFactory entityManagerFactory) {
    this.dataSourceConfig = dataSourceConfig;
    this.entityManagerFactory = entityManagerFactory;
  }

  public Session getDefaultSession() {
    return getSessionBySchema(dataSourceConfig.getDefaultSchema());
  }

  public Session getSessionByCompanyId(final String companyId) {
    return getSessionBySchema(dataSourceConfig.getSchemaByCompanyId(companyId));
  }

  public Session getSessionBySchema(final String schema) {
    return entityManagerFactory
        .unwrap(SessionFactory.class)
        .withOptions()
        .tenantIdentifier(schema)
        .openSession();
  }
}
