package shamu.company.company.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.entity.Tenant;
import shamu.company.common.events.TenantCreatedEvent;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.common.service.TenantService;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeSizeDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.CompanyBenefitsSettingMapper;
import shamu.company.company.entity.mapper.CompanyMapper;
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

  private final TenantService tenantService;

  private final OfficeAddressMapper officeAddressMapper;

  private final StateProvinceService stateProvinceService;

  private final CompanyBenefitsSettingMapper companyBenefitsSettingMapper;

  private final CompanyMapper companyMapper;

  private final CompanyBenefitsSettingService companyBenefitsSettingService;

  private final ApplicationEventPublisher eventPublisher;

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
      final CompanyMapper companyMapper,
      final ApplicationEventPublisher eventPublisher,
      final TenantService tenantService,
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
    this.companyMapper = companyMapper;
    this.eventPublisher = eventPublisher;
    this.tenantService = tenantService;
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

  public Company save(final Company company) {
    companyRepository.save(company);
    final Tenant tenant = companyMapper.convertToTenant(company);
    eventPublisher.publishEvent(new TenantCreatedEvent(tenant));
    return company;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
  @SuppressWarnings("unused")
  public void saveOrUpdateRecordForDefaultDatabase(final TenantCreatedEvent event) {
    tenantService.save(event.getTenant());
  }

  public CompanyBenefitsSettingDto findCompanyBenefitsSetting() {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    return companyBenefitsSettingMapper.convertCompanyBenefitsSettingDto(benefitsSetting);
  }

  public void updateBenefitSettingAutomaticRollover(final Boolean isTurnOn) {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    benefitsSetting.setIsAutomaticRollover(isTurnOn);
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public void updateEnrollmentPeriod(final CompanyBenefitsSettingDto companyBenefitsSettingDto) {
    final CompanyBenefitsSetting benefitsSetting =
        companyBenefitsSettingService.getCompanyBenefitsSetting();
    benefitsSetting.setStartDate(companyBenefitsSettingDto.getStartDate());
    benefitsSetting.setEndDate(companyBenefitsSettingDto.getEndDate());
    companyBenefitsSettingService.save(benefitsSetting);
  }

  public String updateCompanyName(final String companyName) {
    if (!existsByName(companyName)) {
      final Company company = getCompany();
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

  public void updateIsPaidHolidaysAutoEnrolled(final boolean isAutoEnrolled) {
    final Company company = getCompany();
    company.setIsPaidHolidaysAutoEnroll(isAutoEnrolled);
    companyRepository.save(company);
  }

  public Company getCompany() {
    return companyRepository.findAll().get(0);
  }
}
