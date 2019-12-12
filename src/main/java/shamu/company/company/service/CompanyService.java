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

  private final JobUserRepository jobUserRepository;

  private final OfficeAddressMapper officeAddressMapper;

  @Autowired
  public CompanyService(final CompanyRepository companyRepository,
      final DepartmentRepository departmentRepository,
      final EmploymentTypeRepository employmentTypeRepository,
      final JobRepository jobRepository, final OfficeRepository officeRepository,
      final StateProvinceRepository stateProvinceRepository,
      final OfficeAddressRepository officeAddressRepository,
      final JobUserRepository jobUserRepository,
      final OfficeAddressMapper officeAddressMapper) {
    this.companyRepository = companyRepository;
    this.departmentRepository = departmentRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.jobRepository = jobRepository;
    this.officeRepository = officeRepository;
    this.stateProvinceRepository = stateProvinceRepository;
    this.officeAddressRepository = officeAddressRepository;
    this.jobUserRepository = jobUserRepository;
    this.officeAddressMapper = officeAddressMapper;
  }


  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  public List<SelectFieldSizeDto> getDepartmentsByCompanyId(final String companyId) {
    final List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    final List<Department> departments = departmentRepository.findAllByCompanyId(companyId);

    for (final Department department: departments) {
      final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      final Integer size = departmentRepository.findCountByDepartment(department.getId());
      selectFieldSizeDto.setId(department.getId());
      selectFieldSizeDto.setName(department.getName());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public Department getDepartmentsById(final String id) {
    return departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No Department with id: " + id));
  }

  public Department saveDepartmentsByCompany(final String name, final String companyId) {
    final Department department = new Department();
    department.setName(name);
    department.setCompany(new Company(companyId));
    return departmentRepository.save(department);
  }

  public List<SelectFieldSizeDto> getJobsByDepartmentId(final String id) {
    final List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    final List<Job> jobs = jobRepository.findAllByDepartmentId(id);
    for (final Job job: jobs) {
      final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      final Integer size = jobUserRepository.getCountByJobId(job.getId());
      selectFieldSizeDto.setId(job.getId());
      selectFieldSizeDto.setName(job.getTitle());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public Job saveJobsByDepartmentId(final String departmentId, final String name) {
    final Job job = new Job();
    final Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobRepository.save(job);
  }

  public List<OfficeSizeDto> getOfficesByCompany(final String companyId) {
    final List<OfficeSizeDto> officeSizeDtoList = new ArrayList<>();
    final List<Office> offices = officeRepository.findByCompanyId(companyId);
    for (final Office office: offices) {
      final OfficeSizeDto officeSizeDto = new OfficeSizeDto();
      final Integer size = officeRepository.findCountByOffice(office.getId());
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

  public List<SelectFieldSizeDto> getEmploymentTypesByCompanyId(final String companyId) {
    final List<SelectFieldSizeDto> selectFieldSizeDtoList = new ArrayList<>();
    final List<EmploymentType> types = employmentTypeRepository.findAllByCompanyId(companyId);
    for (final EmploymentType type: types) {
      final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
      final Integer size = employmentTypeRepository.findCountByType(type.getId());
      selectFieldSizeDto.setId(type.getId());
      selectFieldSizeDto.setName(type.getName());
      selectFieldSizeDto.setSize(size);

      selectFieldSizeDtoList.add(selectFieldSizeDto);
    }
    return selectFieldSizeDtoList;
  }

  public EmploymentType saveEmploymentType(final String employmentTypeName,
      final String companyId) {
    final EmploymentType employmentType = EmploymentType.builder().name(employmentTypeName).build();
    employmentType.setCompany(new Company(companyId));

    return employmentTypeRepository.save(employmentType);
  }

  public Company findById(final String companyId) {
    return companyRepository
        .findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("No such Company"));
  }
}
