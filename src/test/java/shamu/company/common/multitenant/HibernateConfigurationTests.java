package shamu.company.common.multitenant;

import java.lang.reflect.Field;
import java.util.Map;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.util.ReflectionUtils;

public class HibernateConfigurationTests {

  @Mock private JpaProperties jpaProperties;

  private HibernateConfiguration hibernateConfiguration;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    hibernateConfiguration = new HibernateConfiguration(jpaProperties);
  }

  @Test
  void testJpaVendorAdapter() {
    final JpaVendorAdapter jpaVendor = hibernateConfiguration.jpaVendorAdapter();
    Assertions.assertThat(jpaVendor).isNotNull();
  }

  @Test
  void testEntityManagerFactory() {
    final DataSource dataSource = Mockito.mock(DataSource.class);
    final MultiTenantConnectionProvider multiTenantConnectionProvider =
        Mockito.mock(MultiTenantConnectionProvider.class);
    final CurrentTenantIdentifierResolver currentTenantIdentifierResolver =
        Mockito.mock(CurrentTenantIdentifierResolver.class);

    final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
        hibernateConfiguration.entityManagerFactory(
            dataSource, multiTenantConnectionProvider, currentTenantIdentifierResolver);
    Assertions.assertThat(entityManagerFactoryBean).isNotNull();
    Assertions.assertThat(entityManagerFactoryBean.getDataSource()).isEqualTo(dataSource);

    final Map<String, Object> properties = entityManagerFactoryBean.getJpaPropertyMap();
    Assertions.assertThat(properties.get(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER))
        .isEqualTo(currentTenantIdentifierResolver);
    Assertions.assertThat(properties.get(Environment.MULTI_TENANT_CONNECTION_PROVIDER))
        .isEqualTo(multiTenantConnectionProvider);

    final Field unitManagerField =
        ReflectionUtils.findField(
            LocalContainerEntityManagerFactoryBean.class, "internalPersistenceUnitManager");
    Assertions.assertThat(unitManagerField).isNotNull();
    unitManagerField.setAccessible(true);
    final Object unitManager = ReflectionUtils.getField(unitManagerField, entityManagerFactoryBean);

    final Field packagesToScanField =
        ReflectionUtils.findField(DefaultPersistenceUnitManager.class, "packagesToScan");
    Assertions.assertThat(packagesToScanField).isNotNull();
    packagesToScanField.setAccessible(true);
    final Object packageToScan = ReflectionUtils.getField(packagesToScanField, unitManager);
    Assertions.assertThat(packageToScan instanceof String[]).isTrue();
  }
}
