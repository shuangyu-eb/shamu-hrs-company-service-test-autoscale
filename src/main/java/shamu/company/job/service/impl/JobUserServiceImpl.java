package shamu.company.job.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.job.entity.JobUser;
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
  UserRepository userRepository;

  @Autowired
  OfficeRepository officeRepository;

  @Autowired
  CompensationFrequencyRepository compensationFrequencyRepository;

  @Autowired
  UserRoleRepository userRoleRepository;

  @Override
  public JobUser getJobUserByUserId(Long userId) {
    return jobUserRepository.findByUserId(userId);
  }

  @Override
  public List getManagers(User user) {
    UserRole userRole = userRoleRepository.findByName(Role.MANAGER.name());
    List<User> managers = userRepository
        .findByUserRoleAndCompany(userRole, user.getCompany());
    return managers.stream().map(tempManger -> {
      ManagerPojo manager = new ManagerPojo();
      manager.setId(tempManger.getId());
      UserPersonalInformation information = tempManger.getUserPersonalInformation();
      manager.setName(UserNameUtil
          .getUserName(information.getFirstName(), information.getMiddleName(),
              information.getLastName()));
      return manager;
    }).collect(Collectors.toList());
  }

  @Override
  public JobUser save(JobUser jobUser) {
    return jobUserRepository.save(jobUser);
  }

}
