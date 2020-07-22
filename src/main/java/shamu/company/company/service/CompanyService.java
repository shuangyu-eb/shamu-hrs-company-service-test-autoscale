package shamu.company.company.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeSizeDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.CompanyBenefitsSettingMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.entity.Job;
import shamu.company.common.exception.errormapping.ZipCodeNotExistException;
import shamu.company.job.service.JobService;
import shamu.company.server.dto.CompanyDtoProjection;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;

  private final DepartmentService departmentService;

  private final EmploymentTypeService employmentTypeService;

  private final JobService jobService;

  private final OfficeService officeService;

  private final OfficeAddressMapper officeAddressMapper;

  private final StateProvinceService stateProvinceService;

  private final CompanyBenefitsSettingMapper companyBenefitsSettingMapper;

  private final CompanyBenefitsSettingService companyBenefitsSettingService;

  private final GoogleMapsHelper googleMapsHelper;

  @Autowired
  public CompanyService(
      final CompanyRepository companyRepository,
      final DepartmentService departmentService,
      final EmploymentTypeService employmentTypeService,
      final JobService jobService,
      final OfficeService officeService,
      final OfficeAddressMapper officeAddressMapper,
      final StateProvinceService stateProvinceService,
      final CompanyBenefitsSettingMapper companyBenefitsSettingMapper,
      final CompanyBenefitsSettingService companyBenefitsSettingService,
      final GoogleMapsHelper googleMapsHelper) {
    this.companyRepository = companyRepository;
    this.departmentService = departmentService;
    this.employmentTypeService = employmentTypeService;
    this.jobService = jobService;
    this.officeService = officeService;
    this.officeAddressMapper = officeAddressMapper;
    this.stateProvinceService = stateProvinceService;
    this.companyBenefitsSettingMapper = companyBenefitsSettingMapper;
    this.companyBenefitsSettingService = companyBenefitsSettingService;
    this.googleMapsHelper = googleMapsHelper;
  }

  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  public List<SelectFieldSizeDto> findDepartments() {
    final List<Department> departments = departmentService.findAll();

    return departments.stream()
        .map(
            department -> {
              final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
              final Integer size = departmentService.findCountByDepartment(department.getId());
              selectFieldSizeDto.setId(department.getId());
              selectFieldSizeDto.setName(department.getName());
              selectFieldSizeDto.setSize(size);
              return selectFieldSizeDto;
            })
        .collect(Collectors.toList());
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

  public Department saveDepartment(final String name) {
    final List<Department> oldDepartments = departmentService.findByName(name);
    if (!oldDepartments.isEmpty()) {
      throw new AlreadyExistsException("Department already exists.", "department");
    }
    final Department department = new Department();
    department.setName(name);
    return departmentService.save(department);
  }

  public Job saveJob(final String name) {
    final List<Job> oldJob = jobService.findByTitle(name);
    if (!oldJob.isEmpty()) {
      throw new AlreadyExistsException("Job title already exists.", "job title");
    }

    final Job job = new Job();
    job.setTitle(name);
    return jobService.save(job);
  }

  public List<OfficeSizeDto> findOffices() {
    final List<Office> offices = officeService.findAll();
    return offices.stream()
        .map(
            office -> {
              final OfficeSizeDto officeSizeDto = new OfficeSizeDto();
              final Integer size = officeService.findCountByOffice(office.getId());
              officeSizeDto.setId(office.getId());
              officeSizeDto.setName(office.getName());
              officeSizeDto.setOfficeAddress(
                  officeAddressMapper.convertToOfficeAddressDto(office.getOfficeAddress()));
              officeSizeDto.setSize(size);
              return officeSizeDto;
            })
        .collect(Collectors.toList());
  }

  public Office saveOffice(final Office office) {
    final List<Office> oldOffices = officeService.findByName(office.getName());
    if (!oldOffices.isEmpty()) {
      throw new AlreadyExistsException("Office already exists.", "office");
    }
    final OfficeAddress officeAddress = office.getOfficeAddress();
    final String timezone = googleMapsHelper.findTimezoneByPostalCode(officeAddress.getPostalCode());
    if (timezone.isEmpty()) {
      throw new ZipCodeNotExistException("Office zipCode is error.");
    }
    final StateProvince stateProvince = officeAddress.getStateProvince();
    if (stateProvince != null && stateProvince.getId() != null) {
      final StateProvince stateProvinceReturned =
          stateProvinceService.findById(stateProvince.getId());
      officeAddress.setStateProvince(stateProvinceReturned);
    }

    office.setOfficeAddress(officeAddress);

    return officeService.save(office);
  }

  public Company findById(final String companyId) {
    return companyRepository
        .findById(companyId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Company with id %s not found!", companyId),
                    companyId,
                    "company"));
  }

  public Company save(final Company company) {
    return companyRepository.save(company);
  }

  public CompanyBenefitsSettingDto findCompanyBenefitsSetting(final String companyId) {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    return companyBenefitsSettingMapper.convertCompanyBenefitsSettingDto(benefitsSetting);
  }

  public void updateBenefitSettingAutomaticRollover(
      final String companyId, final Boolean isTurnOn) {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    benefitsSetting.setIsAutomaticRollover(isTurnOn);
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public void updateEnrollmentPeriod(
      final String companyId, final CompanyBenefitsSettingDto companyBenefitsSettingDto) {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    benefitsSetting.setStartDate(companyBenefitsSettingDto.getStartDate());
    benefitsSetting.setEndDate(companyBenefitsSettingDto.getEndDate());
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public String updateCompanyName(final String companyName, final String companyId) {
    if (!existsByName(companyName)) {
      final Company company = companyRepository.findCompanyById(companyId);
      company.setName(companyName);
      companyRepository.save(company);
    } else {
      throw new AlreadyExistsException("Company name already exists.", "company name");
    }
    return companyName;
  }

  public CompanyDtoProjection findCompanyDtoByUserId(final String id) {
    return companyRepository.findCompanyDtoByUserId(id);
  }

  public List<Company> findAllById(final List<String> ids) {
    return companyRepository.findAllById(ids);
  }

  public void updateIsPaidHolidaysAutoEnrolled(
      final String companyId, final boolean isAutoEnrolled) {
    final Company company = companyRepository.findCompanyById(companyId);
    company.setIsPaidHolidaysAutoEnroll(isAutoEnrolled);
    companyRepository.save(company);
  }
}
