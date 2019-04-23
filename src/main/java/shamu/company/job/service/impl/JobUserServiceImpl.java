package shamu.company.job.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.CompanyRepository;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.pojo.JobInformationPojo;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;
import shamu.company.user.ManagerPojo;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserRole.Role;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.utils.UserNameUtil;

@Service
public class JobUserServiceImpl implements JobUserService {

  @Autowired
  JobUserRepository jobUserRepository;

  @Autowired
  EmploymentTypeRepository employmentTypeRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  DepartmentRepository departmentRepository;

  @Autowired
  OfficeRepository officeRepository;

  @Autowired
  CompensationFrequencyRepository compensationFrequencyRepository;

  @Autowired
  StateProvinceRepository stateProvinceRepository;

  @Autowired
  UserRoleRepository userRoleRepository;

  @Autowired
  CompanyRepository companyRepository;

  @Override
  public JobUser getJobUserByUserId(Long userId) {
    return jobUserRepository.findByUserId(userId);
  }

  @Override
  public JobInformationPojo getJobInfoModal(Long userId) {
    User userParam = userRepository.findById(userId).get();
    JobUser jobUser = jobUserRepository.findJobUserByUser(userParam);
    Job job = jobUser.getJob();
    User user = jobUser.getUser();
    JobInformationPojo jobInformationPojo = new JobInformationPojo(user, job, jobUser);
    return jobInformationPojo;
  }

  @Override
  public List getOfficeAddresses(User user) {
    return officeRepository.findByCompany(user.getCompany());
  }

  @Override
  public List getEmploymentTypes(User user) {
    return employmentTypeRepository.findAllByCompany(user.getCompany());
  }

  @Override
  public List getDepartments(User user) {
    return departmentRepository.findAllByCompanyId(user.getCompany().getId());
  }

  @Override
  public List getCompensationFrequences(User user) {
    return compensationFrequencyRepository
        .findAllByCompany(user.getCompany());
  }

  @Override
  public List getStateProvinces() {
    return stateProvinceRepository.findAll();
  }

  @Override
  public List getManagers(User user) {
    UserRole userRole = userRoleRepository.findByName(Role.MANAGER.name());
    List<User> managers = userRepository
        .findByUserRoleAndCompany(userRole, user.getCompany());
    List<ManagerPojo> newManagers = managers.stream().map(tempManger -> {
      ManagerPojo manager = new ManagerPojo();
      manager.setId(tempManger.getId());
      UserPersonalInformation information = tempManger.getUserPersonalInformation();
      manager.setName(UserNameUtil
          .getUserName(information.getFirstName(), information.getMiddleName(),
              information.getLastName()));
      return manager;
    }).collect(Collectors.toList());
    return newManagers;
  }

  @Override
  public JobInformationPojo getJobInfoByUserId(Long userId) {
    User userParam = userRepository.findById(userId).get();
    JobUser jobUser = jobUserRepository.findJobUserByUser(userParam);
    Job job = jobUser.getJob();
    User user = jobUser.getUser();
    String jobTitle = job.getTitle();
    JobInformationPojo jobInfomationPojo = new JobInformationPojo(jobUser);
    return jobInfomationPojo;
  }
}
