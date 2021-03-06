package shamu.company.common.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Make 'spring.datasource' configuration dynamic in order to support schema switch to the relevant
 * tenant.
 */
@ConfigurationProperties(prefix = "spring.datasource")
@Configuration
@Data
public class DataSourceConfig {

  private String defaultSchema;

  private String tenantPrefix;

  private String templateUrl;

  private String username;

  private String password;

  private String driverClassName;

  private int maximumPoolSize;

  public DataSource getDataSource(final String schema) {
    final String url = getTemplateUrl().replace("{schema}", schema);
    final HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(getUsername());
    dataSource.setPassword(getPassword());
    dataSource.setDriverClassName(getDriverClassName());
    dataSource.setMaximumPoolSize(getMaximumPoolSize());
    return dataSource;
  }

  public String getSchemaByCompanyId(final String companyId) {
    return tenantPrefix + companyId;
  }
}
