package shamu.company.server;

import java.util.List;
import shamu.company.user.entity.User;

public interface CompanyUserService {

  List<User> getUsersBy(List<Long> ids);

  User findUserByEmail(String email);

  List<User> getAllUsers(Long companyId);

  User findUserByUserId(Long userId);
}

