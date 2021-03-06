package shamu.company.admin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.entity.mapper.SystemAnnouncementsMapper;
import shamu.company.admin.repository.SuperAdminRepository;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserService;

@Service
public class SuperAdminService {

  private final UserService userService;

  private final SuperAdminRepository superAdminRepository;

  private final Auth0Helper auth0Helper;

  private final UserMapper userMapper;

  private final AuthUserCacheManager authUserCacheManager;

  private final SystemAnnouncementsService systemAnnouncementsService;

  private final SystemAnnouncementsMapper systemAnnouncementsMapper;

  @Autowired
  public SuperAdminService(
      final UserService userService,
      final SuperAdminRepository superAdminRepository,
      final Auth0Helper auth0Helper,
      final UserMapper userMapper,
      final AuthUserCacheManager authUserCacheManager,
      final SystemAnnouncementsService systemAnnouncementsService,
      final SystemAnnouncementsMapper systemAnnouncementsMapper) {
    this.userService = userService;
    this.superAdminRepository = superAdminRepository;
    this.auth0Helper = auth0Helper;
    this.userMapper = userMapper;
    this.authUserCacheManager = authUserCacheManager;
    this.systemAnnouncementsService = systemAnnouncementsService;
    this.systemAnnouncementsMapper = systemAnnouncementsMapper;
  }

  public Page<SuperAdminUserDto> getUsersByKeywordAndPageable(
      final String keyword, final Pageable pageable) {
    final String[] userStatus = {
      Status.ACTIVE.name(),
      Status.CHANGING_EMAIL_VERIFICATION.name(),
      Status.PENDING_VERIFICATION.name(),
      Status.DISABLED.name()
    };
    return superAdminRepository.getUsersByKeywordAndPageable(keyword, userStatus, pageable);
  }

  public MockUserDto mockUser(final String userId, final String token) {
    final User user = userService.findById(userId);
    final AuthUser authUser = userMapper.convertToAuthUser(user);
    final MockUserDto mockUserDto = userMapper.convertToMockUserDto(user);
    final List<String> permissions = auth0Helper.getPermissionBy(user);


    final String[] writePermissions = {
      "CREATE_AND_APPROVED_TIME_OFF_REQUEST",
      "CREATE_DEPARTMENT",
      "CREATE_DOCUMENT_REQUEST",
      "CREATE_EMPLOYEE_TYPE",
      "CREATE_JOB",
      "CREATE_OFFICE",
      "CREATE_USER",
      "DEACTIVATE_USER",
      "DELETE_PAID_HOLIDAY",
      "DELETE_USER",
      "DENY_DOCUMENT_REQUEST",
      "EDIT_PAID_HOLIDAY",
      "EDIT_SELF",
      "EDIT_USER",
      "UPDATE_COMPANY_NAME"
    };

    final List<String> permissionsFiltered = new ArrayList<>(permissions);

    for (final String permission : permissions) {
      if (Arrays.asList(writePermissions).contains(permission)) {
        permissionsFiltered.remove(permission);
      }
    }

    authUser.setPermissions(permissionsFiltered);
    authUserCacheManager.cacheAuthUser(token, authUser);
    mockUserDto.setPermissions(permissionsFiltered);
    return mockUserDto;
  }

  public SystemAnnouncementDto getSystemActiveAnnouncement() {
    return systemAnnouncementsMapper.convertSystemAnnouncementDto(
        systemAnnouncementsService.getSystemActiveAnnouncement());
  }

  public void publishSystemAnnouncement(
      final String userId, final SystemAnnouncementDto systemAnnouncementDto) {
    // update previous announcement past
    final SystemAnnouncement oldActiveAnnouncement =
        systemAnnouncementsService.getSystemActiveAnnouncement();
    if (null != oldActiveAnnouncement) {
      oldActiveAnnouncement.setIsPastAnnouncement(true);
      systemAnnouncementsService.save(oldActiveAnnouncement);
    }
    // public new system-active-announcement
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
    systemAnnouncement.setUserId(userId);
    systemAnnouncement.setContent(systemAnnouncementDto.getContent());
    systemAnnouncement.setIsPastAnnouncement(false);
    systemAnnouncementsService.save(systemAnnouncement);
  }

  public void updateSystemActiveAnnouncement(final String id) {
    final SystemAnnouncement systemAnnouncement = systemAnnouncementsService.findById(id);
    systemAnnouncement.setIsPastAnnouncement(true);
    systemAnnouncementsService.save(systemAnnouncement);
  }

  public Page<SystemAnnouncementDto> getSystemPastAnnouncements(final Pageable pageable) {
    return systemAnnouncementsService.getSystemPastAnnouncements(pageable);
  }
}
