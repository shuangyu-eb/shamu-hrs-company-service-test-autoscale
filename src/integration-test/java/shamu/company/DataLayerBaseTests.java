package shamu.company;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import shamu.company.helpers.auth0.Auth0Helper;

@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@ActiveProfiles("unit")
public class DataLayerBaseTests {

  @MockBean
  protected Auth0Helper auth0Helper;
}
