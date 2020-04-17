package shamu.company.company;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.entity.*;
import shamu.company.company.entity.mapper.CompanyBenefitsSettingMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyBenefitsSettingService;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.job.service.JobService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyServiceTests {

  @Mock private CompanyRepository companyRepository;
  @Mock private DepartmentService departmentService;
  @Mock private EmploymentTypeService employmentTypeService;
  @Mock private JobService jobService;
  @Mock private OfficeService officeService;
  @Mock private OfficeAddressMapper officeAddressMapper;
  @Mock private StateProvinceService stateProvinceService;
  @Mock private CompanyBenefitsSettingMapper companyBenefitsSettingMapper;
  @Mock private CompanyBenefitsSettingService companyBenefitsSettingService;

  private CompanyService companyService;
  private final Department department = new Department();
  private final Office office = new Office();
  private final EmploymentType type = new EmploymentType();
  private final CompanyBenefitsSetting benefitsSetting = new CompanyBenefitsSetting();

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    companyService = new CompanyService(
        companyRepository,departmentService,
        employmentTypeService,jobService,
        officeService,officeAddressMapper,
        stateProvinceService,companyBenefitsSettingMapper,
        companyBenefitsSettingService
    );
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
    Assertions.assertTrue(() -> companyService.existsByName("1"));
  }

  @Test
  void testFindDepartmentsByCompanyId() {
    final List<Department> list = new ArrayList<>();
    list.add(department);
    Mockito.when(departmentService.findAllByCompanyId(Mockito.anyString())).thenReturn(list);
    Mockito.when(departmentService.findCountByDepartment(Mockito.anyString())).thenReturn(1);
    Assertions.assertDoesNotThrow(() -> companyService.findDepartmentsByCompanyId("1"));
  }

  @Test
  void testfindDepartmentsById(){
    Mockito.when(departmentService.findById(Mockito.anyString())).thenReturn(department);
    Assertions.assertDoesNotThrow(() -> companyService.findDepartmentsById("1"));
  }

  @Test
  void testFindJobsById() {
    Assertions.assertDoesNotThrow(() -> companyService.findJobsById("1"));
  }

  @Test
  void testFindEmploymentTypeById() {
    Assertions.assertDoesNotThrow(() -> companyService.findEmploymentTypeById("1"));
  }

  @Test
  void testFindOfficeById() {
    Assertions.assertDoesNotThrow(() -> companyService.findOfficeById("1"));
  }

  @Test
  void testSaveDepartmentsByCompany() {
    Assertions.assertDoesNotThrow(() -> companyService.saveDepartmentsByCompany("name","1"));
  }

  @Test
  void testSaveJobsByDepartmentId() {
    Assertions.assertDoesNotThrow(() -> companyService.saveJobsByDepartmentId("1","name"));
  }

  @Test
  void testFindOfficesByCompany() {
    final List<Office> list = new ArrayList<>();
    list.add(office);
    Mockito.when(officeService.findByCompanyId(Mockito.anyString())).thenReturn(list);
    Mockito.when(officeService.findCountByOffice(Mockito.anyString())).thenReturn(1);
    Assertions.assertDoesNotThrow(() -> companyService.findOfficesByCompany("1"));
  }

  @Test
  void testSaveOffice() {
    final OfficeAddress officeAddress = new OfficeAddress();
    final StateProvince stateProvince = new StateProvince();
    stateProvince.setId("1");
    officeAddress.setStateProvince(stateProvince);
    office.setOfficeAddress(officeAddress);
    Mockito.when(stateProvinceService.findById(Mockito.anyString())).thenReturn(stateProvince);
    Assertions.assertDoesNotThrow(() -> companyService.saveOffice(office));
  }

  @Test
  void testFindEmploymentTypesByCompanyId() {
    final List<EmploymentType> types = new ArrayList<>();
    types.add(type);
    Mockito.when(employmentTypeService.findAllByCompanyId(Mockito.anyString())).thenReturn(types);
    Mockito.when(employmentTypeService.findCountByType(Mockito.anyString())).thenReturn(1);
    Assertions.assertDoesNotThrow(() -> companyService.findEmploymentTypesByCompanyId("1"));
  }

  @Test
  void testSaveEmploymentType() {
    Assertions.assertDoesNotThrow(() -> companyService.saveEmploymentType("name","1"));
  }

  @Nested
  class testFindById {

    @Test
    void whenIdExists_thenShouldNotThrowException() {
      final Optional<Company> optional = Optional.of(new Company());
      Mockito.when(companyRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertDoesNotThrow(() -> companyService.findById("1"));
    }

    @Test
    void whenIdNotExists_thenShoudlThrowException() {
      final Optional optional = Optional.empty();
      Mockito.when(companyRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertThrows(
          ResourceNotFoundException.class,
          () -> companyService.findById("1")
      );
    }
  }

  @Test
  void testSave() {
    Assertions.assertDoesNotThrow(() -> companyService.save(new Company()));
  }

  @Test
  void testFindCompanyBenefitsSetting() {
    Mockito.when(companyBenefitsSettingService
        .findByCompanyId(Mockito.anyString())).thenReturn(benefitsSetting);
    Assertions.assertDoesNotThrow(() -> companyService.findCompanyBenefitsSetting("1"));
  }

  @Test
  void testUpdateBenefitSettingAutomaticRollover() {
    Mockito.when(companyBenefitsSettingService
        .findByCompanyId(Mockito.anyString())).thenReturn(benefitsSetting);
    Assertions.assertDoesNotThrow(() -> companyService.updateBenefitSettingAutomaticRollover("1",true));
  }

  @Test
  void testUpdateEnrollmentPeriod() {
    final CompanyBenefitsSettingDto companyBenefitsSettingDto = new CompanyBenefitsSettingDto();
    companyBenefitsSettingDto.setStartDate(new Date(36000));
    companyBenefitsSettingDto.setEndDate(new Date(360000));
    Mockito.when(companyBenefitsSettingService.findByCompanyId(Mockito.anyString())).thenReturn(benefitsSetting);
    Assertions.assertDoesNotThrow(() -> companyService.updateEnrollmentPeriod("1",companyBenefitsSettingDto));
  }
}
