package shamu.company.common.config;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.IMPLICIT_NAMING_STRATEGY;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER;
import static org.hibernate.cfg.AvailableSettings.PHYSICAL_NAMING_STRATEGY;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Set the MultiTenancyStrategy as SCHEMA and override the connection provider and Tenant Identifier
 * Resolver.
 *
 * <p>We're adapting the "Shared Database, Separate Schema" strategy mentioned here:
 * https://medium.com/swlh/multi-tenancy-implementation-using-spring-boot-hibernate-6a8e3ecb251a
 */
@Configuration
public class HibernateConfiguration {

  private final JpaProperties jpaProperties;

  @Autowired
  public HibernateConfiguration(final JpaProperties jpaProperties) {
    this.jpaProperties = jpaProperties;
  }

  @Bean
  JpaVendorAdapter jpaVendorAdapter() {
    return new HibernateJpaVendorAdapter();
  }

  @Bean
  LocalContainerEntityManagerFactoryBean entityManagerFactory(
      final DataSource dataSource,
      final MultiTenantConnectionProvider multiTenantConnectionProvider,
      final CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
    final Map<String, Object> newJpaProperties = new HashMap<>(jpaProperties.getProperties());
    newJpaProperties.put(MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
    newJpaProperties.put(MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
    newJpaProperties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
    newJpaProperties.put(IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
    newJpaProperties.put(PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
    newJpaProperties.put(DIALECT, MySQL57Dialect.class.getName());

    final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
        new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setDataSource(dataSource);
    entityManagerFactoryBean.setJpaPropertyMap(newJpaProperties);
    entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
    entityManagerFactoryBean.setPackagesToScan("shamu.company");
    entityManagerFactoryBean.setPersistenceUnitName("default");
    return entityManagerFactoryBean;
  }
}
