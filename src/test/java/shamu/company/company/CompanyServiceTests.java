package shamu.company.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.common.service.TenantService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.CompanyBenefitsSettingMapper;
import shamu.company.company.entity.mapper.CompanyMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyBenefitsSettingService;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.service.JobService;

public class CompanyServiceTests {

  private final Department department = new Department();
  private final Office office = new Office();
  private final OfficeCreateDto officeCreateDto = new OfficeCreateDto();
  private final EmploymentType type = new EmploymentType();
  private final CompanyBenefitsSetting benefitsSetting = new CompanyBenefitsSetting();
  @Mock private CompanyRepository companyRepository;
  @Mock private DepartmentService departmentService;
  @Mock private EmploymentTypeService employmentTypeService;
  @Mock private JobService jobService;
  @Mock private OfficeService officeService;
  @Mock private OfficeAddressMapper officeAddressMapper;
  @Mock private StateProvinceService stateProvinceService;
  @Mock private CompanyBenefitsSettingMapper companyBenefitsSettingMapper;
  @Mock private CompanyBenefitsSettingService companyBenefitsSettingService;
  @Mock private TenantService tenantService;
  @Mock private CompanyMapper companyMapper;
  @Mock private GoogleMapsHelper googleMapsHelper;
  @Mock private StaticTimeZoneRepository staticTimeZoneRepository;
  private CompanyService companyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    companyService =
        new CompanyService(
            companyRepository,
            departmentService,
            employmentTypeService,
            jobService,
            officeService,
            officeAddressMapper,
            stateProvinceService,
            companyBenefitsSettingMapper,
            companyBenefitsSettingService,
            companyMapper,
            tenantService,
            googleMapsHelper,
            staticTimeZoneRepository);
    department.setId("1");
    department.setName("name");
    office.setId("1");
    office.setName("name");
    officeCreateDto.setPlaceId("placeId");
    officeCreateDto.setOfficeName("officeName");
    type.setId("1");
    type.setName("name");
  }

  @Test
  void testExistsByName() {
    Mockito.when(companyRepository.existsByName(Mockito.anyString())).thenReturn(true);
    assertThat(companyService.existsByName("1")).isTrue();
  }

  @Test
  void testFindDepartmentsByCompanyId() {
    final List<Department> list = new ArrayList<>();
    list.add(department);
    Mockito.when(departmentService.findAll()).thenReturn(list);
    Mockito.when(departmentService.findCountByDepartment(Mockito.anyString())).thenReturn(1);
    assertThatCode(() -> companyService.findDepartments()).doesNotThrowAnyException();
  }

  @Test
  void testfindDepartmentsById() {
    Mockito.when(departmentService.findById(Mockito.anyString())).thenReturn(department);
    assertThatCode(() -> companyService.findDepartmentsById("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindJobsById() {

    assertThatCode(() -> companyService.findJobsById("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindEmploymentTypeById() {
    assertThatCode(() -> companyService.findEmploymentTypeById("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindOfficeById() {
    assertThatCode(() -> companyService.findOfficeById("1")).doesNotThrowAnyException();
  }

  @Test
  void testSaveDepartmentsByCompany() {
    assertThatCode(() -> companyService.saveDepartment("name")).doesNotThrowAnyException();
  }

  @Test
  void testSaveJobsByDepartmentId() {
    assertThatCode(() -> companyService.saveJob("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindOfficesByCompany() {
    final List<Office> list = new ArrayList<>();
    list.add(office);
    Mockito.when(officeService.findAll()).thenReturn(list);
    Mockito.when(officeService.findCountByOffice(Mockito.anyString())).thenReturn(1);
    assertThatCode(() -> companyService.findOffices()).doesNotThrowAnyException();
  }

  @Test
  void testSaveOffice() {
    final OfficeAddress officeAddress = new OfficeAddress();
    final StateProvince stateProvince = new StateProvince();
    stateProvince.setId("1");
    officeAddress.setStateProvince(stateProvince);
    officeAddress.setPostalCode("02114");
    officeAddress.setCity("city");
    Mockito.when(stateProvinceService.findById(Mockito.anyString())).thenReturn(stateProvince);
    Mockito.when(officeAddressMapper.updateFromOfficeCreateDto(
        Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new OfficeAddress());
    Mockito.when(officeService.findByName(Mockito.anyString())).thenReturn(Collections.EMPTY_LIST);
    assertThatCode(() -> companyService.saveOffice(officeCreateDto)).doesNotThrowAnyException();
  }

  @Test
  void testSave() {
    assertThatCode(() -> companyService.save(new Company())).doesNotThrowAnyException();
  }

  @Test
  void testFindCompanyBenefitsSetting() {
    Mockito.when(companyBenefitsSettingService.getCompanyBenefitsSetting())
        .thenReturn(benefitsSetting);
    assertThatCode(() -> companyService.findCompanyBenefitsSetting()).doesNotThrowAnyException();
  }

  @Test
  void testUpdateBenefitSettingAutomaticRollover() {
    Mockito.when(companyBenefitsSettingService.getCompanyBenefitsSetting())
        .thenReturn(benefitsSetting);
    assertThatCode(() -> companyService.updateBenefitSettingAutomaticRollover(true))
        .doesNotThrowAnyException();
  }

  @Test
  void testUpdateEnrollmentPeriod() {
    final CompanyBenefitsSettingDto companyBenefitsSettingDto = new CompanyBenefitsSettingDto();
    companyBenefitsSettingDto.setStartDate(new Date(36000));
    companyBenefitsSettingDto.setEndDate(new Date(360000));
    Mockito.when(companyBenefitsSettingService.getCompanyBenefitsSetting())
        .thenReturn(benefitsSetting);
    assertThatCode(() -> companyService.updateEnrollmentPeriod(companyBenefitsSettingDto))
        .doesNotThrowAnyException();
  }

  @Test
  void testGetCompany() {
    Mockito.when(companyRepository.findAll()).thenReturn(Collections.singletonList(new Company()));
    assertThatCode(() -> companyService.getCompany()).doesNotThrowAnyException();
  }

  @Nested
  class testUpdateCompanyName {

    @Test
    void whenUpdateCompanyName_thenShouldNotThrowException() {
      Mockito.when(companyService.existsByName(Mockito.anyString())).thenReturn(false);
      Mockito.when(companyRepository.findAll())
          .thenReturn(Collections.singletonList(new Company()));
      assertThatCode(() -> companyService.updateCompanyName("example")).doesNotThrowAnyException();
      assertThat(companyService.updateCompanyName("example2")).isEqualTo("example2");
    }

    @Test
    void whenUpdateCompanyName_thenShouldThrowException() {
      Mockito.when(companyService.existsByName(Mockito.anyString())).thenReturn(true);
      assertThatExceptionOfType(AlreadyExistsException.class)
          .isThrownBy(() -> companyService.updateCompanyName("example"));
    }

    @Test
    void findCompanyDtoByUserId() {
      final Company companyDtoProjection = new Company();
      Mockito.when(companyRepository.findAll())
          .thenReturn(Collections.singletonList(companyDtoProjection));
      assertThatCode(() -> companyService.findCompanyDto()).doesNotThrowAnyException();
    }

    @Test
    void findAllById() {
      final List<Tenant> companies = new ArrayList<>();
      Mockito.when(tenantService.findAllByCompanyId(Mockito.anyList())).thenReturn(companies);
      assertThatCode(() -> companyService.findAllById(Collections.singletonList("1")))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void testUpdateIsPaidHolidaysAutoEnrolled() {
    final Company company = new Company();
    Mockito.when(companyRepository.findAll()).thenReturn(Collections.singletonList(company));
    assertThatCode(() -> companyService.updateIsPaidHolidaysAutoEnrolled(true))
        .doesNotThrowAnyException();
  }
}
