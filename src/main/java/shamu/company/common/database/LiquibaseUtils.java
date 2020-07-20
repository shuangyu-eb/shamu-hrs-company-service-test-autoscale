package shamu.company.common.database;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

public interface LiquibaseUtils {

  static SpringLiquibase constructLiquibase(
      final DataSource dataSource, final String schema, final LiquibaseProperties properties) {
    final SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setLiquibaseSchema(schema);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setShouldRun(properties.isEnabled());
    liquibase.setContexts(properties.getContexts());
    return liquibase;
  }
}
