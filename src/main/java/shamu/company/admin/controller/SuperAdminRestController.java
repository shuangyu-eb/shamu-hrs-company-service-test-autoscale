package shamu.company.admin.controller;

import java.util.List;
import javax.validation.Valid;
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
import shamu.company.admin.dto.TenantDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.entity.Tenant;
import shamu.company.common.entity.mapper.TenantMapper;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.service.TenantService;

@RestApiController
class SuperAdminRestController extends BaseRestController {

  private final SuperAdminService superAdminService;

  private final TenantService tenantService;

  private final TenantMapper tenantMapper;

  @Autowired
  SuperAdminRestController(
      final SuperAdminService superAdminService,
      final TenantService tenantService,
      final TenantMapper tenantMapper) {
    this.superAdminService = superAdminService;
    this.tenantService = tenantService;
    this.tenantMapper = tenantMapper;
  }

  @GetMapping("/super-admin/users")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public Page<SuperAdminUserDto> getUsers(@Valid final PageRequestDto pageRequestDto) {
    TenantContext.setCurrentTenant(pageRequestDto.getCompanyId());
    return superAdminService.getUsersByKeywordAndPageable(
        pageRequestDto.getKeyword(), pageRequestDto.getPageable());
  }

  @GetMapping("/super-admin/companies")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public List<TenantDto> getCompanies() {
    final List<Tenant> tenants = tenantService.findAll();
    return tenantMapper.convertToTenantDtos(tenants);
  }

  @PostMapping("/super-admin/mock/companies/{companyId}/users/{userId}")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public MockUserDto mockUser(
      @PathVariable final String userId, @PathVariable final String companyId) {
    TenantContext.setCurrentTenant(companyId);
    return superAdminService.mockUser(userId, findToken());
  }

  @GetMapping("/super-admin/system-active-announcement")
  public SystemAnnouncementDto getSystemActiveAnnouncement() {
    TenantContext.clear();
    return superAdminService.getSystemActiveAnnouncement();
  }

  @PostMapping("/super-admin/publish-system-announcement")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public HttpEntity publishSystemAnnouncement(
      @RequestBody final SystemAnnouncementDto systemAnnouncementDto) {
    TenantContext.clear();
    superAdminService.publishSystemAnnouncement(findUserId(), systemAnnouncementDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("/super-admin/system-active-announcement/{id}")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public HttpEntity updateSystemActiveAnnouncement(@PathVariable final String id) {
    TenantContext.clear();
    superAdminService.updateSystemActiveAnnouncement(id);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/super-admin/system-past-announcements")
  @PreAuthorize("hasAuthority('SUPER_PERMISSION')")
  public Page<SystemAnnouncementDto> getSystemPastAnnouncements(
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "10") final Integer size) {
    TenantContext.clear();
    final Pageable pageable = PageRequest.of(page - 1, size);
    return superAdminService.getSystemPastAnnouncements(pageable);
  }
}
