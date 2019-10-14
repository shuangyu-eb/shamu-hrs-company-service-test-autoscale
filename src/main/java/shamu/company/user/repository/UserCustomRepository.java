package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;

public interface UserCustomRepository {

  Page<JobUserListItem> getAllByCondition(EmployeeListSearchCondition employeeListSearchCondition,
                                          Long companyId, Pageable pageable, Role role);

  Page<JobUserListItem> getMyTeamByManager(EmployeeListSearchCondition employeeListSearchCondition,
      User user, Pageable paramPageable);

  List<OrgChartDto> findOrgChartItemByManagerId(Long managerId,
      Long companyId);

  OrgChartDto findOrgChartItemByUserId(Long id, Long companyId);
  
  User saveUser(User user);
  
  List<User> saveAllUsers(final Iterable<User> users);
}
