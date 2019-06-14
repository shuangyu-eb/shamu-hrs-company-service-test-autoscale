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
  public CompanyServiceImpl(CompanyRepository companyRepository,
      DepartmentRepository departmentRepository, EmploymentTypeRepository employmentTypeRepository,
      JobRepository jobRepository, OfficeRepository officeRepository,
      StateProvinceRepository stateProvinceRepository,
      OfficeAddressRepository officeAddressRepository) {
    this.companyRepository = companyRepository;
    this.departmentRepository = departmentRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.jobRepository = jobRepository;
    this.officeRepository = officeRepository;
    this.stateProvinceRepository = stateProvinceRepository;
    this.officeAddressRepository = officeAddressRepository;
  }


  @Override
  public Boolean existsByName(String companyName) {
    return companyRepository.existsByName(companyName);
  }

  @Override
  public Boolean existsBySubdomainName(String subDomainName) {
    return companyRepository.existsBySubdomainName(subDomainName);
  }

  @Override
  public List<Department> getDepartmentsByCompany(Company company) {
    return departmentRepository.findAllByCompany(company);
  }

  @Override
  public Department saveDepartmentsByCompany(String name, Company company) {
    Department department = new Department();
    department.setName(name);
    department.setCompany(company);
    return departmentRepository.save(department);
  }

  @Override
  public List<Job> getJobsByDepartmentId(Long id) {
    return jobRepository.findAllByDepartmentId(id);
  }

  @Override
  public Job saveJobsByDepartmentId(Long departmentId, String name) {
    Job job = new Job();
    Department department = new Department();
    department.setId(departmentId);

    job.setDepartment(department);
    job.setTitle(name);

    return jobRepository.save(job);
  }

  @Override
  public List<Office> getOfficesByCompany(Company company) {
    return officeRepository.findByCompany(company);
  }

  @Override
  public Office saveOffice(Office office) {
    OfficeAddress officeAddress = office.getOfficeAddress();
    StateProvince stateProvince = stateProvinceRepository
        .findById(officeAddress.getStateProvince().getId()).get();
    officeAddress.setStateProvince(stateProvince);
    
    officeAddress = officeAddressRepository.save(officeAddress);
    office.setOfficeAddress(officeAddress);

    return officeRepository.save(office);
  }

  @Override
  public List<EmploymentType> getEmploymentTypesByCompany(Company company) {
    return employmentTypeRepository.findAllByCompany(company);
  }

  @Override
  public EmploymentType saveEmploymentType(String employmentTypeName, Company company) {
    EmploymentType employmentType = new EmploymentType(employmentTypeName);
    employmentType.setCompany(company);

    return employmentTypeRepository.save(employmentType);
  }
}
