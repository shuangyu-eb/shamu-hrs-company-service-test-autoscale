package shamu.company.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.repository.SuperAdminRepository;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserService;

class SuperObjectAdminServiceTests {

  @Mock
  private UserService userService;

  @Mock
  private SuperAdminRepository superAdminRepository;

  @Mock
  private Auth0Helper auth0Helper;

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Mock
  private AuthUserCacheManager authUserCacheManager;

  private SuperAdminService superAdminService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    superAdminService = new SuperAdminService(userService, superAdminRepository, auth0Helper, userMapper,
        authUserCacheManager);
  }

  @Test
  void testMockUser() {
    final User user = new User();
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final UserRole userRole = new UserRole();
    userRole.setName(Role.ADMIN.getValue());
    user.setId(userId);
    user.setUserRole(userRole);
    Mockito.when(userService.findByUserId(userId)).thenReturn(user);

    final List<String> permissions = new ArrayList<>();
    Mockito.when(auth0Helper.getPermissionBy(userId)).thenReturn(permissions);

    final MockUserDto mockUserDto = new MockUserDto();
    mockUserDto.setId(userId);
    mockUserDto.setPermissions(permissions);

    Assertions.assertEquals(mockUserDto, superAdminService.mockUser(userId, "123"));
  }

}
