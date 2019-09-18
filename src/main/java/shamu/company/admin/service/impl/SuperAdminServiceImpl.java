package shamu.company.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.user.entity.User.Role;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.Auth0Util;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

  private final UserRepository userRepository;
  private final Auth0Util auth0Util;

  @Autowired
  public SuperAdminServiceImpl(final UserRepository userRepository,
      final Auth0Util auth0Util) {
    this.userRepository = userRepository;
    this.auth0Util = auth0Util;
  }

  @Override
  public Page<SuperAdminUserDto> getUsersBy(final String keyword, final Pageable pageable) {
    final Page<SuperAdminUserDto> users = userRepository.findBy(keyword, pageable);
    users.forEach(user -> {
      final Role userRole = auth0Util.getUserRole(user.getEmail());
      user.setRole(userRole.getValue());
    });
    return users;
  }
}
