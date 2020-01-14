package shamu.company.company.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeSizeDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.CompanyBenefitsSettingMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.job.entity.Job;
import shamu.company.job.service.JobService;
import shamu.company.utils.ReflectionUtil;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;

  private final DepartmentService departmentService;

  private final EmploymentTypeService employmentTypeService;

  private final JobService jobService;

  private final OfficeService officeService;

  private final OfficeAddressMapper officeAddressMapper;

  private final CompanySizeService companySizeService;

  private final StateProvinceService stateProvinceService;

  private final CompanyBenefitsSettingMapper companyBenefitsSettingMapper;

  private final CompanyBenefitsSettingService companyBenefitsSettingService;

  @Autowired
  public CompanyService(final CompanyRepository companyRepository,
      final DepartmentService departmentService,
      final EmploymentTypeService employmentTypeService,
      final JobService jobService,
      final OfficeService officeService,
      final OfficeAddressMapper officeAddressMapper,
      final CompanySizeService companySizeService,
      final StateProvinceService stateProvinceService,
      final CompanyBenefitsSettingMapper companyBenefitsSettingMapper,
      final CompanyBenefitsSettingService companyBenefitsSettingService) {
    this.companyRepository = companyRepository;
    this.departmentService = departmentService;
    this.employmentTypeService = employmentTypeService;
    this.jobService = jobService;
    this.officeService = officeService;
    this.officeAddressMapper = officeAddressMapper;
    this.companySizeService = companySizeService;
    this.stateProvinceService = stateProvinceService;
    this.companyBenefitsSettingMapper = companyBenefitsSettingMapper;
    this.companyBenefitsSettingService = companyBenefitsSettingService;
  }


  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  public List<SelectFieldSizeDto> findDepartmentsByCompanyId(final String companyId) {
    final List<Department> departments = departmentService.findAllByCompanyId(companyId);

    return departments.stream().map(department -> {
      final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      final Integer size = departmentService.findCountByDepartment(department.getId());
      selectFieldSizeDto.setId(department.getId());
      selectFieldSizeDto.setName(department.getName());
      selectFieldSizeDto.setSize(size);
      return selectFieldSizeDto;
    }).collect(Collectors.toList());
  }

  public Department findDepartmentsById(final String id) {
    return departmentService.findById(id);
  }

  public Job findJobsById(final String id) {
    return jobService.findById(id);
  }

  public EmploymentType findEmploymentTypeById(final String id) {
    return employmentTypeService.findById(id);
  }

  public Office findOfficeById(final String id) {
    return officeService.findById(id);
  }

  public Department saveDepartmentsByCompany(final String name, final String companyId) {
    final Department department = new Department();
    department.setName(name);
    department.setCompany(new Company(companyId));
    return departmentService.save(department);
  }

  public Job saveJobsByDepartmentId(final String departmentId, final String name) {
    final Job job = new Job();
    final Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobService.save(job);
  }

  public List<OfficeSizeDto> findOfficesByCompany(final String companyId) {
    final List<Office> offices = officeService.findByCompanyId(companyId);
    return offices.stream().map(office -> {
      final OfficeSizeDto officeSizeDto = new OfficeSizeDto();
      final Integer size = officeService.findCountByOffice(office.getId());
      officeSizeDto.setId(office.getId());
      officeSizeDto.setName(office.getName());
      officeSizeDto.setOfficeAddress(
              officeAddressMapper.convertToOfficeAddressDto(office.getOfficeAddress()));
      officeSizeDto.setSize(size);
      return officeSizeDto;
    }).collect(Collectors.toList());
  }

  public Office saveOffice(final Office office) {
    final OfficeAddress officeAddress = office.getOfficeAddress();
    final StateProvince stateProvince = officeAddress.getStateProvince();
    if (stateProvince != null && stateProvince.getId() != null) {
      final StateProvince stateProvinceReturned = stateProvinceService
              .findById(stateProvince.getId());
      officeAddress.setStateProvince(stateProvinceReturned);
    }

    office.setOfficeAddress(officeAddress);

    return officeService.save(office);
  }

  public List<SelectFieldSizeDto> findEmploymentTypesByCompanyId(final String companyId) {
    final List<EmploymentType> types = employmentTypeService.findAllByCompanyId(companyId);
    return types.stream().map(type -> {
      final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      final Integer size = employmentTypeService.findCountByType(type.getId());
      selectFieldSizeDto.setId(type.getId());
      selectFieldSizeDto.setName(type.getName());
      selectFieldSizeDto.setSize(size);
      return selectFieldSizeDto;
    }).collect(Collectors.toList());
  }

  public EmploymentType saveEmploymentType(final String employmentTypeName,
      final String companyId) {
    final EmploymentType employmentType = EmploymentType.builder().name(employmentTypeName).build();
    employmentType.setCompany(new Company(companyId));

    return employmentTypeService.save(employmentType);
  }

  public Company findById(final String companyId) {
    return companyRepository
        .findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("No such Company"));
  }

  public List<CommonDictionaryDto> getCompanySizes() {
    final List<CompanySize> companySizes = companySizeService.findAll();
    return ReflectionUtil.convertTo(companySizes, CommonDictionaryDto.class);
  }

  public Company save(final Company company) {
    return companyRepository.save(company);
  }

  public CompanyBenefitsSettingDto findCompanyBenefitsSetting(final String companyId) {
    CompanyBenefitsSetting benefitsSetting = companyBenefitsSettingService
        .findByCompanyId(companyId);
    return companyBenefitsSettingMapper.convertCompanyBenefitsSettingDto(benefitsSetting);
  }

  public void updateBenefitSettingAutomaticRollover(
      final String companyId, final Boolean isTurnOn) {
    CompanyBenefitsSetting benefitsSetting = companyBenefitsSettingService
        .findByCompanyId(companyId);
    benefitsSetting.setIsAutomaticRollover(isTurnOn);
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public void updateEnrollmentPeriod(
      final String companyId, final CompanyBenefitsSettingDto companyBenefitsSettingDto) {
    CompanyBenefitsSetting benefitsSetting = companyBenefitsSettingService
        .findByCompanyId(companyId);
    benefitsSetting.setStartDate(companyBenefitsSettingDto.getStartDate());
    benefitsSetting.setEndDate(companyBenefitsSettingDto.getEndDate());
    companyBenefitsSettingService.save(benefitsSetting);
  }
}
