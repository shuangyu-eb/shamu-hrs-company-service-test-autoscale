package shamu.company.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.common.service.TenantService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
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
import shamu.company.server.dto.CompanyDtoProjection;

public class CompanyServiceTests {

  private final Department department = new Department();
  private final Office office = new Office();
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
  @Mock private CompanyMapper companyMapper;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private TenantService tenantService;
  @Mock private CompanyService companyService;
  @Mock private GoogleMapsHelper googleMapsHelper;
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
            eventPublisher,
            tenantService,
            companyBenefitsSettingService,
            googleMapsHelper);
    department.setId("1");
    department.setName("name");
    office.setId("1");
    office.setName("name");
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
    assertThatCode(() -> companyService.saveJob("name")).doesNotThrowAnyException();
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
    office.setOfficeAddress(officeAddress);
    Mockito.when(stateProvinceService.findById(Mockito.anyString())).thenReturn(stateProvince);
    Mockito.when(officeService.findByName(Mockito.anyString()))
        .thenReturn(Collections.EMPTY_LIST);
    Mockito.when(googleMapsHelper.findTimezoneByPostalCode("02114")).thenReturn("timezone");
    assertThatCode(() -> companyService.saveOffice(office)).doesNotThrowAnyException();
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

  @Nested
  class testFindById {

    @Test
    void whenIdExists_thenShouldNotThrowException() {
      final Optional<Company> optional = Optional.of(new Company());
      Mockito.when(companyRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatCode(() -> companyService.getCompany()).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShoudlThrowException() {
      final Optional optional = Optional.empty();
      Mockito.when(companyRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> companyService.getCompany());
    }
  }

  @Nested
  class testUpdateCompanyName {

    @Test
    void whenUpdateCompanyName_thenShouldNotThrowException() {
      final Company company = new Company();
      Mockito.when(companyService.existsByName(Mockito.anyString())).thenReturn(false);
      Mockito.when(companyRepository.findAll()).thenReturn(Collections.singletonList(company));
      Mockito.when(companyRepository.save(company)).thenReturn(company);
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
      final CompanyDtoProjection companyDtoProjection =
          new CompanyDtoProjection() {
            @Override
            public String getId() {
              return null;
            }

            @Override
            public String getName() {
              return null;
            }
          };
      Mockito.when(companyRepository.findCompanyDtoByUserId(Mockito.anyString()))
          .thenReturn(companyDtoProjection);
      assertThatCode(() -> companyService.findCompanyDtoByUserId("1")).doesNotThrowAnyException();
    }

    @Test
    void findAllById() {
      final List<Company> companies = new ArrayList<>();
      Mockito.when(companyRepository.findAllById(Mockito.anyList())).thenReturn(companies);
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
