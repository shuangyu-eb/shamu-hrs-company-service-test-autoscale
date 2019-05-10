package shamu.company.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;

public interface UserCustomRepository {

  Page<JobUserListItem> getAllByCondition(
      EmployeeListSearchCondition employeeListSearchCondition, Long companyId, Pageable pageable);


  Page<JobUserListItem> getMyTeamByManager(EmployeeListSearchCondition employeeListSearchCondition,
      User user, Pageable paramPageable);
}
