package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;

public interface UserCustomRepository {

  Page<JobUserListItem> getAllByCondition(EmployeeListSearchCondition employeeListSearchCondition,
      String companyId, Pageable pageable);

  Page<JobUserListItem> getMyTeamByManager(EmployeeListSearchCondition employeeListSearchCondition,
      User user, Pageable paramPageable);

  List<OrgChartDto> findOrgChartItemByManagerId(String managerId,
      String companyId);

  OrgChartDto findOrgChartItemByUserId(String id, String companyId);

  User saveUser(User user);

  List<User> saveAllUsers(final Iterable<User> users);

  Page<JobUserListItem> getAllByName(EmployeeListSearchCondition employeeListSearchCondition,
      String companyId, Pageable pageable);
}
