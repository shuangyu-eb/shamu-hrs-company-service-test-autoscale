package shamu.company.common.database;

import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import shamu.company.common.config.DataSourceConfig;
import shamu.company.common.entity.Tenant;
import shamu.company.common.service.TenantService;
import shamu.company.company.service.CompanyService;
import shamu.company.utils.UuidUtil;

public class LiquibaseManagerTests {

  private DataSourceConfig dataSourceConfig;

  @Mock private LiquibaseProperties tenantLiquibaseProperties;

  @Mock private LiquibaseProperties companyLiquibaseProperties;

  @Mock private ResourceLoader resourceLoader;

  @Mock private TenantService tenantService;

  @Mock private CompanyService companyService;

  @Mock private StatesProvincesInitializer statesProvincesInitializer;

  private LiquibaseManager liquibaseManager;

  private final String companyId = UuidUtil.getUuidString();

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    dataSourceConfig = new DataSourceConfig();
    dataSourceConfig.setUsername("root");
    dataSourceConfig.setPassword("password");
    dataSourceConfig.setDefaultSchema("company");
    dataSourceConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSourceConfig.setMaximumPoolSize(10);
    dataSourceConfig.setTemplateUrl(
        "jdbc:mysql://localhost:3306/{schema}?createDatabaseIfNotExist=true&jdbcCompliantTruncation=false");
    dataSourceConfig.setTenantPrefix("tenant_");
    liquibaseManager =
        new LiquibaseManager(
            dataSourceConfig,
            tenantLiquibaseProperties,
            companyLiquibaseProperties,
            resourceLoader,
            tenantService,
            statesProvincesInitializer,
            companyService);

    final Tenant tenant = Tenant.builder().companyId(companyId).build();
    Mockito.when(tenantService.findAll()).thenReturn(Collections.singletonList(tenant));
  }

  @Test
  void testSetUpSchemas() {
    Assertions.assertThatCode(() -> Whitebox.invokeMethod(liquibaseManager, "setUpSchemas"))
        .doesNotThrowAnyException();
  }

  @Test
  void testAddSchema() {
    final String companyName = "company";

    Assertions.assertThatCode(() -> liquibaseManager.addSchema(companyId, companyName))
        .doesNotThrowAnyException();
  }
}
