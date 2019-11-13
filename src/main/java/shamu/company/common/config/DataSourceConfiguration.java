package shamu.company.common.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
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

  @Bean(name = "secretDataSource")
  @Qualifier("secretDataSource")
  @ConfigurationProperties(prefix = "spring.secret.datasource")
  public DataSource secretDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "secretJdbcTemplate")
  public JdbcTemplate secretJdbcTemplate(
      @Qualifier("secretDataSource") final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
