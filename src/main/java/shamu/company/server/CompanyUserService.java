package shamu.company.server;

import java.util.List;

import org.springframework.data.domain.Page;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;

public interface CompanyUserService {

  List<User> getUsersBy(List<String> ids);

  User findUserByEmail(String email);

  List<User> getAllUsers(String companyId);

  AuthUser findUserByUserId(String userId);

  Page<JobUserListItem> getAllEmployees(AuthUser user,
                                        EmployeeListSearchCondition employeeListSearchCondition);
}

