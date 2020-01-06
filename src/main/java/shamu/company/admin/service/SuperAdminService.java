package shamu.company.admin.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.repository.SuperAdminRepository;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserService;

@Service
public class SuperAdminService {

  private final UserService userService;

  private final SuperAdminRepository superAdminRepository;

  private final Auth0Helper auth0Helper;

  private final UserMapper userMapper;

  private final AuthUserCacheManager authUserCacheManager;

  @Autowired
  public SuperAdminService(final UserService userService,
      final SuperAdminRepository superAdminRepository,
      final Auth0Helper auth0Helper,
      final UserMapper userMapper,
      final AuthUserCacheManager authUserCacheManager) {
    this.userService = userService;
    this.superAdminRepository = superAdminRepository;
    this.auth0Helper = auth0Helper;
    this.userMapper = userMapper;
    this.authUserCacheManager = authUserCacheManager;
  }

  public Page<SuperAdminUserDto> getUsersByKeywordAndPageable(final String keyword,
      final Pageable pageable) {
    return superAdminRepository.getUsersByKeywordAndPageable(keyword, pageable);
  }

  public MockUserDto mockUser(final String userId, final String token) {
    final User user = userService.findActiveUserById(userId);
    final AuthUser authUser = userMapper.convertToAuthUser(user);
    final MockUserDto mockUserDto = userMapper.convertToMockUserDto(user);
    final List<String> permissions = auth0Helper
        .getPermissionBy(user.getId());
    authUser.setPermissions(permissions);
    authUserCacheManager.cacheAuthUser(token, authUser);
    mockUserDto.setPermissions(permissions);
    return mockUserDto;
  }
}
