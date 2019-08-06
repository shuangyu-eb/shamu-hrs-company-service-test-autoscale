package shamu.company.company;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;

@Service
public class CompanyServiceImpl implements CompanyService {

  private final CompanyRepository companyRepository;

  private final DepartmentRepository departmentRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final JobRepository jobRepository;

  private final OfficeRepository officeRepository;

  private final StateProvinceRepository stateProvinceRepository;

  private final OfficeAddressRepository officeAddressRepository;

  @Autowired
  public CompanyServiceImpl(final CompanyRepository companyRepository,
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


  @Override
  public Boolean existsByName(final String companyName) {
    return companyRepository.existsByName(companyName);
  }

  @Override
  public Boolean existsBySubdomainName(final String subDomainName) {
    return companyRepository.existsBySubdomainName(subDomainName);
  }

  @Override
  public List<Department> getDepartmentsByCompany(final Company company) {
    return departmentRepository.findAllByCompany(company);
  }

  @Override
  public Department saveDepartmentsByCompany(final String name, final Company company) {
    final Department department = new Department();
    department.setName(name);
    department.setCompany(company);
    return departmentRepository.save(department);
  }

  @Override
  public List<Job> getJobsByDepartmentId(final Long id) {
    return jobRepository.findAllByDepartmentId(id);
  }

  @Override
  public Job saveJobsByDepartmentId(final Long departmentId, final String name) {
    final Job job = new Job();
    final Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobRepository.save(job);
  }

  @Override
  public List<Office> getOfficesByCompany(final Company company) {
    return officeRepository.findByCompany(company);
  }

  @Override
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

  @Override
  public List<EmploymentType> getEmploymentTypesByCompany(final Company company) {
    return employmentTypeRepository.findAllByCompany(company);
  }

  @Override
  public EmploymentType saveEmploymentType(final String employmentTypeName, final Company company) {
    final EmploymentType employmentType = new EmploymentType(employmentTypeName);
    employmentType.setCompany(company);

    return employmentTypeRepository.save(employmentType);
  }
}
