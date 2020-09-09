package shamu.company.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.PageRequestDto.Field;
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

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
  @Mock private UserService userService;
  @Mock private SuperAdminRepository superAdminRepository;
  @Mock private Auth0Helper auth0Helper;
  @Mock private AuthUserCacheManager authUserCacheManager;

  @Mock private SystemAnnouncementsService systemAnnouncementsService;

  @Mock private SystemAnnouncementsMapper systemAnnouncementsMapper;

  private SuperAdminService superAdminService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    superAdminService =
        new SuperAdminService(
            userService,
            superAdminRepository,
            auth0Helper,
            userMapper,
            authUserCacheManager,
            systemAnnouncementsService,
            systemAnnouncementsMapper);
  }

  @Test
  void testMockUser() {
    final User user = new User();
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final UserRole userRole = new UserRole();
    userRole.setName(Role.ADMIN.getValue());
    user.setId(userId);
    user.setUserRole(userRole);
    Mockito.when(userService.findById(userId)).thenReturn(user);

    final List<String> permissions = new ArrayList<>();
    Mockito.when(auth0Helper.getPermissionBy(user)).thenReturn(permissions);

    final MockUserDto mockUserDto = new MockUserDto();
    mockUserDto.setId(userId);
    mockUserDto.setPermissions(permissions);

    assertThat(superAdminService.mockUser(userId, "123")).isEqualTo(mockUserDto);
  }

  @Test
  void testPublicSystemAnnouncement() {
    final User user = new User(UuidUtil.getUuidString());
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
    final SystemAnnouncementDto systemAnnouncementDto = new SystemAnnouncementDto();

    systemAnnouncement.setUserId(user.getId());
    systemAnnouncementDto.setContent("test public system announcement content.");

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);
    Mockito.when(systemAnnouncementsService.getSystemActiveAnnouncement())
        .thenReturn(systemAnnouncement);
    Mockito.when(systemAnnouncementsService.save(Mockito.any())).thenReturn(systemAnnouncement);

    Assertions.assertDoesNotThrow(
        () -> superAdminService.publishSystemAnnouncement("1", systemAnnouncementDto));
  }

  @Test
  void testUpdateSystemActiveAnnouncement() {
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();

    Mockito.when(systemAnnouncementsService.findById(Mockito.any())).thenReturn(systemAnnouncement);
    Mockito.when(systemAnnouncementsService.save(Mockito.any())).thenReturn(systemAnnouncement);

    assertThatCode(() -> superAdminService.updateSystemActiveAnnouncement("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void testGetSystemPastAnnouncements() {
    Mockito.when(systemAnnouncementsService.getSystemPastAnnouncements(Mockito.any()))
        .thenReturn(Page.empty());

    assertThatCode(() -> superAdminService.getSystemPastAnnouncements(Mockito.any()))
        .doesNotThrowAnyException();
  }

  @Nested
  class getUsersByKeywordAndPageable {

    @BeforeEach
    void init() {}

    @Test
    void getUsersByKeywordAndPageable() {

      Mockito.when(
              superAdminRepository.getUsersByKeywordAndPageable(
                  Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(Page.empty());

      assertThatCode(
              () ->
                  superAdminService.getUsersByKeywordAndPageable(
                      "", PageRequest.of(1, 10, Direction.ASC, Field.NAME.getValue())))
          .doesNotThrowAnyException();
    }
  }
}
