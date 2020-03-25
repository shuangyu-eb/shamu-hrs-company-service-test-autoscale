package shamu.company.admin;

import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.entity.mapper.SystemAnnouncementsMapper;
import shamu.company.admin.repository.SuperAdminRepository;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.admin.service.SystemAnnouncementsService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

class SuperAdminServiceTests {

  @Mock
  private UserService userService;

  @Mock
  private SuperAdminRepository superAdminRepository;

  @Mock
  private Auth0Helper auth0Helper;

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Mock
  private AuthUserCacheManager authUserCacheManager;

  @Mock
  private SystemAnnouncementsService systemAnnouncementsService;

  @Mock
  private SystemAnnouncementsMapper systemAnnouncementsMapper;

  private SuperAdminService superAdminService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    superAdminService = new SuperAdminService(userService, superAdminRepository, auth0Helper, userMapper,
        authUserCacheManager, systemAnnouncementsService, systemAnnouncementsMapper);
  }

  @Test
  void testMockUser() {
    final User user = new User();
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final UserRole userRole = new UserRole();
    userRole.setName(Role.ADMIN.getValue());
    user.setId(userId);
    user.setUserRole(userRole);
    Mockito.when(userService.findActiveUserById(userId)).thenReturn(user);

    final List<String> permissions = new ArrayList<>();
    Mockito.when(auth0Helper.getPermissionBy(user)).thenReturn(permissions);

    final MockUserDto mockUserDto = new MockUserDto();
    mockUserDto.setId(userId);
    mockUserDto.setPermissions(permissions);

    Assertions.assertEquals(mockUserDto, superAdminService.mockUser(userId, "123"));
  }

  @Test
  void testPublicSystemAnnouncement() {
    final User user = new User(UuidUtil.getUuidString());
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
    final SystemAnnouncementDto systemAnnouncementDto = new SystemAnnouncementDto();

    systemAnnouncement.setUser(user);
    systemAnnouncementDto.setContent("test public system announcement content.");

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);
    Mockito.when(systemAnnouncementsService.getSystemActiveAnnouncement()).thenReturn(systemAnnouncement);
    Mockito.when(systemAnnouncementsService.save(Mockito.any())).thenReturn(systemAnnouncement);

    Assertions.assertDoesNotThrow(() -> superAdminService.publishSystemAnnouncement("1", systemAnnouncementDto));
  }
}
