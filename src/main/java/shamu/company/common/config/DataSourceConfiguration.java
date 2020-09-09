package shamu.company.common.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfiguration {

  @Bean(name = "companyDataSource")
  @Qualifier("companyDataSource")
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource companyDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "companyJdbcTemplate")
  public JdbcTemplate companyJdbcTemplate(
      @Qualifier("companyDataSource") final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.liquibase")
  public LiquibaseProperties companyLiquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean(name = "liquibase")
  public SpringLiquibase companyLiquibase() {
    return getSpringLiquibase(companyDataSource(), companyLiquibaseProperties());
  }

  @Bean(name = "secretDataSource")
  @Qualifier("secretDataSource")
  @ConfigurationProperties(prefix = "spring.secret.datasource")
  @ConditionalOnProperty(
      prefix = "spring.secret.datasource",
      name = {"jdbc-url", "username", "password"})
  public DataSource secretDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "secretJdbcTemplate")
  @ConditionalOnProperty(
      prefix = "spring.secret.datasource",
      name = {"jdbc-url", "username", "password"})
  public JdbcTemplate secretJdbcTemplate(
      @Qualifier("secretDataSource") final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.secret.liquibase")
  @ConditionalOnBean(name = "secretDataSource")
  public LiquibaseProperties secretLiquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  @ConditionalOnBean(name = "secretDataSource")
  public SpringLiquibase secretLiquibase() {
    return getSpringLiquibase(secretDataSource(), secretLiquibaseProperties());
  }

  private SpringLiquibase getSpringLiquibase(
      final DataSource dataSource, final LiquibaseProperties properties) {
    final SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setContexts(properties.getContexts());
    liquibase.setDefaultSchema(properties.getDefaultSchema());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setShouldRun(properties.isEnabled());
    liquibase.setLabels(properties.getLabels());
    liquibase.setChangeLogParameters(properties.getParameters());
    liquibase.setRollbackFile(properties.getRollbackFile());
    return liquibase;
  }
}
