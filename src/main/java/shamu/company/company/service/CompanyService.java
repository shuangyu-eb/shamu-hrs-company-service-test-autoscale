package shamu.company.company.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.SecretHashRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.dto.OfficeSizeDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;

  private final DepartmentRepository departmentRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final JobRepository jobRepository;

  private final OfficeRepository officeRepository;

  private final StateProvinceRepository stateProvinceRepository;

  private final OfficeAddressRepository officeAddressRepository;

  private final SecretHashRepository secretHashRepository;

  private final JobUserRepository jobUserRepository;

  private final OfficeAddressMapper officeAddressMapper;

  @Autowired
  public CompanyService(final CompanyRepository companyRepository,
      final DepartmentRepository departmentRepository,
      final EmploymentTypeRepository employmentTypeRepository,
      final JobRepository jobRepository, final OfficeRepository officeRepository,
      final StateProvinceRepository stateProvinceRepository,
      final OfficeAddressRepository officeAddressRepository,
      final SecretHashRepository secretHashRepository,
      final JobUserRepository jobUserRepository,
      final OfficeAddressMapper officeAddressMapper) {
    this.companyRepository = companyRepository;
    this.departmentRepository = departmentRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.jobRepository = jobRepository;
    this.officeRepository = officeRepository;
    this.stateProvinceRepository = stateProvinceRepository;
    this.officeAddressRepository = officeAddressRepository;
    this.secretHashRepository = secretHashRepository;
    this.jobUserRepository = jobUserRepository;
    this.officeAddressMapper = officeAddressMapper;
  }


  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  public List<SelectFieldSizeDto> getDepartmentsByCompanyId(final Long companyId) {
    List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    List<Department> departments = departmentRepository.findAllByCompanyId(companyId);

    for (Department department: departments) {
      SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      Integer size = departmentRepository.getCountByDepartment(department.getId());
      selectFieldSizeDto.setId(department.getId());
      selectFieldSizeDto.setName(department.getName());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public Department getDepartmentsById(final Long id) {
    return departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No Department with id: " + id));
  }

  public Department saveDepartmentsByCompany(final String name, final Long companyId) {
    final Department department = new Department();
    department.setName(name);
    department.setCompany(new Company(companyId));
    return departmentRepository.save(department);
  }

  public List<SelectFieldSizeDto> getJobsByDepartmentId(final Long id) {
    List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    List<Job> jobs = jobRepository.findAllByDepartmentId(id);
    for (Job job: jobs) {
      SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      Integer size = jobUserRepository.getCountByJobId(job.getId());
      selectFieldSizeDto.setId(job.getId());
      selectFieldSizeDto.setName(job.getTitle());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public Job saveJobsByDepartmentId(final Long departmentId, final String name) {
    final Job job = new Job();
    final Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobRepository.save(job);
  }

  public List<OfficeSizeDto> getOfficesByCompany(final Long companyId) {
    List<OfficeSizeDto> officeSizeDtoList = new ArrayList<>();
    List<Office> offices = officeRepository.findByCompanyId(companyId);
    for (Office office: offices) {
      OfficeSizeDto officeSizeDto = new OfficeSizeDto();
      Integer size = officeRepository.getCountByOffice(office.getId());
      officeSizeDto.setId(office.getId());
      officeSizeDto.setName(office.getName());
      officeSizeDto.setOfficeAddress(
              officeAddressMapper.convertToOfficeAddressDto(office.getOfficeAddress()));
      officeSizeDto.setSize(size);

      officeSizeDtoList.add(officeSizeDto);
    }
    return officeSizeDtoList;
  }

  public Office saveOffice(final Office office) {
    final OfficeAddress officeAddress = office.getOfficeAddress();
    final StateProvince stateProvince = officeAddress.getStateProvince();
    if (stateProvince != null && stateProvince.getId() != null) {
      final StateProvince stateProvinceReturned =
          stateProvinceRepository.findById(stateProvince.getId()).get();
      officeAddress.setStateProvince(stateProvinceReturned);
    }

    office.setOfficeAddress(officeAddress);

    return officeRepository.save(office);
  }

  public List<SelectFieldSizeDto> getEmploymentTypesByCompanyId(final Long companyId) {
    List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    List<EmploymentType> types = employmentTypeRepository.findAllByCompanyId(companyId);
    for (EmploymentType type: types) {
      SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      Integer size = employmentTypeRepository.getCountByType(type.getId());
      selectFieldSizeDto.setId(type.getId());
      selectFieldSizeDto.setName(type.getName());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public EmploymentType saveEmploymentType(final String employmentTypeName, final Long companyId) {
    final EmploymentType employmentType = new EmploymentType(employmentTypeName);
    employmentType.setCompany(new Company(companyId));

    return employmentTypeRepository.save(employmentType);
  }

  public Company findById(final Long companyId) {
    return companyRepository
        .findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("No such Company"));
  }

  public String getCompanySecretByCompanyId(final Long id) {
    return secretHashRepository.getCompanySecretByCompanyId(id);
  }
}
