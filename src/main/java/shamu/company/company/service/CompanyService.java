package shamu.company.company.service;

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
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;

  private final DepartmentRepository departmentRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final JobRepository jobRepository;

  private final OfficeRepository officeRepository;

  private final StateProvinceRepository stateProvinceRepository;

  private final OfficeAddressRepository officeAddressRepository;

  @Autowired
  public CompanyService(final CompanyRepository companyRepository,
      final DepartmentRepository departmentRepository,
      final EmploymentTypeRepository employmentTypeRepository,
      final JobRepository jobRepository, final OfficeRepository officeRepository,
      final StateProvinceRepository stateProvinceRepository,
      final OfficeAddressRepository officeAddressRepository) {
    this.companyRepository = companyRepository;
    this.departmentRepository = departmentRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.jobRepository = jobRepository;
    this.officeRepository = officeRepository;
    this.stateProvinceRepository = stateProvinceRepository;
    this.officeAddressRepository = officeAddressRepository;
  }


  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  public List<Department> getDepartmentsByCompanyId(final Long companyId) {
    return departmentRepository.findAllByCompanyId(companyId);
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

  public List<Job> getJobsByDepartmentId(final Long id) {
    return jobRepository.findAllByDepartmentId(id);
  }

  public Job saveJobsByDepartmentId(final Long departmentId, final String name) {
    final Job job = new Job();
    final Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobRepository.save(job);
  }

  public List<Office> getOfficesByCompany(final Long companyId) {
    return officeRepository.findByCompanyId(companyId);
  }

  public Office saveOffice(final Office office) {
    OfficeAddress officeAddress = office.getOfficeAddress();
    final StateProvince stateProvince = officeAddress.getStateProvince();
    if (stateProvince != null && stateProvince.getId() != null) {
      final StateProvince stateProvinceReturned =
          stateProvinceRepository.findById(stateProvince.getId()).get();
      officeAddress.setStateProvince(stateProvinceReturned);
    }

    officeAddress = officeAddressRepository.save(officeAddress);
    office.setOfficeAddress(officeAddress);

    return officeRepository.save(office);
  }

  public List<EmploymentType> getEmploymentTypesByCompanyId(final Long companyId) {
    return employmentTypeRepository.findAllByCompanyId(companyId);
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
}
