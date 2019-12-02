package shamu.company.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.admin.dto.PageRequestDto;
import shamu.company.admin.dto.SuperAdminUserDto;
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
    return superAdminService.mockUser(id, getToken());
  }

}
