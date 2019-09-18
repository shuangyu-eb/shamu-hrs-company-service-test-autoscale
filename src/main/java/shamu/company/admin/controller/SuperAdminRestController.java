package shamu.company.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.admin.dto.PageRequestDto;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
class SuperAdminRestController {

  private final SuperAdminService superAdminService;

  @Autowired
  SuperAdminRestController(final SuperAdminService superAdminService) {
    this.superAdminService = superAdminService;
  }

  @GetMapping("/super-admin/users")
  // TODO Sort and filter by user role
  public Page<SuperAdminUserDto> getUsers(final PageRequestDto pageRequestDto) {
    return superAdminService.getUsersBy(pageRequestDto.getKeyword(), pageRequestDto.getPageable());
  }

}
