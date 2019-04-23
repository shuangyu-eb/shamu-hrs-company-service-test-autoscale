package shamu.company.job.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.CompanyRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.pojo.JobInformationPojo;
import shamu.company.job.pojo.OfficeAddressPojo;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;

@Service
public class JobServiceImpl implements JobService {

  @Autowired
  JobRepository jobRepository;

  @Autowired
  OfficeAddressRepository officeAddressRepository;

  @Autowired
  UserCompensationRepository userCompensationRepository;

  @Autowired
  EmploymentTypeRepository employmentTypeRepository;

  @Autowired
  OfficeRepository officeRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  JobUserRepository jobUserRepository;

  @Autowired
  DepartmentRepository departmentRepository;

  @Autowired
  CompanyRepository companyRepository;

  @Autowired
  StateProvinceRepository stateProvinceRepository;

  @Autowired
  CompensationFrequencyRepository compensationFrequencyRepository;

  @Override
  public void saveEmploymentType(EmploymentType employmentType, Company company) {
    employmentType.setCompany(company);
    employmentTypeRepository.save(employmentType);
  }

  @Override
  public void saveDepartment(Department department, User user) {
    department.setCompany(user.getCompany());
    departmentRepository.save(department);
  }

  @Override
  public void saveOfficeAddress(OfficeAddressPojo addressPojo) {
    OfficeAddress officeAddress = new OfficeAddress(addressPojo);
    if (addressPojo.getState() != null) {
      StateProvince stateProvince = stateProvinceRepository.findById(addressPojo.getState()).get();
      officeAddress.setStateProvince(stateProvince);
    }
    officeAddress = officeAddressRepository.save(officeAddress);

    Long userId = addressPojo.getUserId();
    User user = userRepository.findById(userId).get();
    JobUser jobUser = jobUserRepository.findJobUserByUser(user);
    Office office = new Office(addressPojo.getOfficeName(), officeAddress, jobUser.getCompany());
    Office newOffice = officeRepository.save(office);
    officeAddress.setOffice(newOffice);
    officeAddressRepository.save(officeAddress);
  }

  @Override
  public void updateJobInfo(JobInformationPojo jobInformationPojo, User user) {
    JobUser jobUser = jobUserRepository.findJobUserByUser(user);
    Integer compensation = jobInformationPojo.getCompensation();
    Long compensationFrequencyId = jobInformationPojo.getCompensationFrequencyId();
    UserCompensation userCompensation = userCompensationRepository.findByUser(user);
    if (compensation != null && compensationFrequencyId != null) {
      CompensationFrequency compensationFrequency = compensationFrequencyRepository
          .findById(compensationFrequencyId).get();
      if (userCompensation == null) {
        userCompensation = new UserCompensation(compensation, user, compensationFrequency);
      } else {
        userCompensation.setWage(compensation);
        userCompensation.setUser(user);
        userCompensation.setCompensationFrequency(compensationFrequency);
      }
      userCompensation = userCompensationRepository.save(userCompensation);
    } else {
      if (userCompensation != null) {
        userCompensation.setWage(null);
        userCompensation.setCompensationFrequency(null);
      }
    }
    user.setUserCompensation(userCompensation);
    Long jobId = jobUser.getJob().getId();
    Job job = jobRepository.findById(jobId).get();
    job.setTitle(jobInformationPojo.getJobTitle());
    jobRepository.save(job);

    Long officeAddressId = jobInformationPojo.getOfficeAddressId();
    if (officeAddressId != null) {
      Office office = officeRepository.findById(officeAddressId).get();
      jobUser.setOffice(office);
    }

    Long managerId = jobInformationPojo.getManagerId();
    User manager = userRepository.findById(managerId).get();
    user.setManagerUser(manager);
    userRepository.save(user);

    Long employmentId = jobInformationPojo.getEmploymentTypeId();
    if (employmentId != null) {
      EmploymentType employmentType = employmentTypeRepository
          .findById(employmentId).get();
      jobUser.setEmploymentType(employmentType);
    }

    jobUser.setStartDate(jobInformationPojo.getStartDate());
    Long departmentId = jobInformationPojo.getDepartmentId();
    if (departmentId != null) {
      Department department = departmentRepository.findById(departmentId).get();
      jobUser.setDepartment(department);
    }
    jobUserRepository.save(jobUser);
  }
}
