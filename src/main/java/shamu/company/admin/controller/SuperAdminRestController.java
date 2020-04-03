package shamu.company.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.PageRequestDto;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
class SuperAdminRestController extends BaseRestController {

  private final SuperAdminService superAdminService;

  @Autowired
  SuperAdminRestController(final SuperAdminService superAdminService) {
    this.superAdminService = superAdminService;
  }

  @GetMapping("/super-admin/users")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public Page<SuperAdminUserDto> getUsers(final PageRequestDto pageRequestDto) {
    return superAdminService
        .getUsersByKeywordAndPageable(pageRequestDto.getKeyword(), pageRequestDto.getPageable());
  }

  @PostMapping("/super-admin/mock/users/{id}")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public MockUserDto mockUser(@PathVariable final String id) {
    return superAdminService.mockUser(id, findToken());
  }

  @GetMapping("/super-admin/system-active-announcement")
  public SystemAnnouncementDto getSystemActiveAnnouncement() {
    return superAdminService.getSystemActiveAnnouncement();
  }

  @PostMapping("/super-admin/publish-system-announcement")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public HttpEntity publishSystemAnnouncement(
      @RequestBody final SystemAnnouncementDto systemAnnouncementDto) {
    superAdminService.publishSystemAnnouncement(findUserId(), systemAnnouncementDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("/super-admin/system-active-announcement/{id}")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public HttpEntity updateSystemActiveAnnouncement(@PathVariable String id) {
    superAdminService.updateSystemActiveAnnouncement(id);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/super-admin/system-past-announcements")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public Page<SystemAnnouncementDto> getSystemPastAnnouncements(
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);
    return superAdminService.getSystemPastAnnouncements(pageable);
  }
}
