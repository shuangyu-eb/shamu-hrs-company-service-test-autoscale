package shamu.company.helpers;

import javax.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.config.DataSourceConfig;
import shamu.company.utils.UuidUtil;

public class DatabaseSessionHelperTests {

  private DataSourceConfig dataSourceConfig;

  @Mock private EntityManagerFactory entityManagerFactory;

  private DatabaseSessionHelper databaseSessionHelper;

  @BeforeEach
  void init() {
    dataSourceConfig = new DataSourceConfig();
    dataSourceConfig.setTenantPrefix("tenant_");
    dataSourceConfig.setDefaultSchema("company");

    MockitoAnnotations.initMocks(this);
    databaseSessionHelper = new DatabaseSessionHelper(dataSourceConfig, entityManagerFactory);

    final SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
    Mockito.when(entityManagerFactory.unwrap(SessionFactory.class)).thenReturn(sessionFactory);

    final SessionBuilder sessionBuilder = Mockito.mock(SessionBuilder.class);
    Mockito.when(sessionFactory.withOptions()).thenReturn(sessionBuilder);

    Mockito.when(sessionBuilder.tenantIdentifier(Mockito.anyString())).thenReturn(sessionBuilder);

    final Session session = Mockito.mock(Session.class);
    Mockito.when(sessionBuilder.openSession()).thenReturn(session);
  }

  @Test
  void testGetSessionBySchema() {
    Assertions.assertThatCode(() -> databaseSessionHelper.getSessionBySchema("schema"))
        .doesNotThrowAnyException();
  }

  @Test
  void testGetDefaultSession() {
    Assertions.assertThatCode(() -> databaseSessionHelper.getDefaultSession())
        .doesNotThrowAnyException();
  }

  @Test
  void testGetSessionByCompanyId() {
    Assertions.assertThatCode(
            () -> databaseSessionHelper.getSessionByCompanyId(UuidUtil.getUuidString()))
        .doesNotThrowAnyException();
  }
}
