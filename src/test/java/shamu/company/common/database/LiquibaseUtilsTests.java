package shamu.company.common.database;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

public class LiquibaseUtilsTests {

  @Test
  void testConstructLiquibase() {
    final DataSource dataSource = Mockito.mock(DataSource.class);
    final String schema = "schema";
    final LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
    liquibaseProperties.setEnabled(true);
    final SpringLiquibase springLiquibase =
        LiquibaseUtils.constructLiquibase(dataSource, schema, liquibaseProperties);

    assertThat(springLiquibase).isNotNull();
    assertThat(springLiquibase.getDataSource()).isEqualTo(dataSource);
  }
}
