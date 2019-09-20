package shamu.company.admin.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.Auth0Util;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

  private static final String ERROR_MESSAGE = "User does not exist!";

  private final UserRepository userRepository;

  private final Auth0Util auth0Util;

  private final UserMapper userMapper;

  private final AuthUserCacheManager authUserCacheManager;

  @Autowired
  public SuperAdminServiceImpl(final UserRepository userRepository,
      final Auth0Util auth0Util, final UserMapper userMapper,
      final AuthUserCacheManager authUserCacheManager) {
    this.userRepository = userRepository;
    this.auth0Util = auth0Util;
    this.userMapper = userMapper;
    this.authUserCacheManager = authUserCacheManager;
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

  @Override
  public MockUserDto mockUser(final Long userId, final String token) {
    final User user = getUser(userId);
    final AuthUser authUser = userMapper.convertToAuthUser(user);
    final MockUserDto mockUserDto = userMapper.convertToMockUserDto(user);
    final List<String> permissions = auth0Util
        .getPermissionBy(user.getUserContactInformation().getEmailWork());
    authUser.setPermissions(permissions);
    authUserCacheManager.cacheAuthUser(token, authUser);
    mockUserDto.setPermissions(permissions);
    return mockUserDto;
  }

  private User getUser(final Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(ERROR_MESSAGE));
  }
}
