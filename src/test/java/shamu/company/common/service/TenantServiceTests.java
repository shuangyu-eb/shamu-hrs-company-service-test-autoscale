package shamu.company.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.database.DataSourceConfig;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.TenantRepository;
import shamu.company.utils.UuidUtil;

public class TenantServiceTests {

  @Mock private TenantRepository tenantRepository;

  @Mock private DataSourceConfig dataSourceConfig;

  @Mock private TenantService tenantService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    tenantService = new TenantService(tenantRepository, dataSourceConfig);
  }

  @Test
  void testSave() {
    final Tenant tenant = new Tenant();
    assertThatCode(() -> tenantService.save(tenant)).doesNotThrowAnyException();
  }

  @Test
  void testIsCompanyExists() {
    assertThatCode(() -> tenantService.isCompanyExists(Mockito.anyString()))
        .doesNotThrowAnyException();
  }

  @Test
  void testFindAll() {
    assertThatCode(() -> tenantService.findAll()).doesNotThrowAnyException();
  }

  @Test
  void testFindAllSchemaNames() {
    final String companyId1 = UuidUtil.getUuidString();
    final String companyId2 = UuidUtil.getUuidString();
    final String prefix = "tenant_";
    final Tenant tenant1 = Tenant.builder().companyId(companyId1).build();
    final Tenant tenant2 = Tenant.builder().companyId(companyId2).build();
    final List<Tenant> tenants = Arrays.asList(tenant1, tenant2);

    Mockito.when(tenantRepository.findAll()).thenReturn(tenants);
    Mockito.when(dataSourceConfig.getSchemaByCompanyId(companyId1)).thenReturn(prefix + companyId1);
    Mockito.when(dataSourceConfig.getSchemaByCompanyId(companyId2)).thenReturn(prefix + companyId2);

    assertThat(tenantService.findAllSchemaNames().contains(prefix + companyId1)).isTrue();
    assertThat(tenantService.findAllSchemaNames().contains(prefix + companyId2)).isTrue();
  }

  @Test
  void testFindAllByCompanyId() {
    assertThatCode(() -> tenantService.findAllByCompanyId(Mockito.anyList()))
        .doesNotThrowAnyException();
  }

  @Test
  void testFindTenantByUserEmailWork() {
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> tenantService.findTenantByUserEmailWork(Mockito.anyString()));
  }

  @Test
  void testDeleteTenant() {
    assertThatCode(() -> tenantService.deleteTenant(Mockito.anyString()))
        .doesNotThrowAnyException();
  }
}
