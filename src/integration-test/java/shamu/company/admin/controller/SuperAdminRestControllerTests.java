package shamu.company.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.service.SuperAdminService;
import shamu.company.authorization.Permission;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = SuperAdminRestController.class)
class SuperAdminRestControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private SuperAdminService superAdminService;

  @Test
  void testGetSystemActiveAnnouncement() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final SystemAnnouncementDto systemAnnouncement = new SystemAnnouncementDto();
    systemAnnouncement.setId("1");
    given(superAdminService.getSystemActiveAnnouncement()).willReturn(systemAnnouncement);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/super-admin/system-active-announcement")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testPublicSystemAnnouncement() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/super-admin/publish-system-announcement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(new SystemAnnouncementDto())))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateSystemActiveAnnouncement() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/super-admin/system-active-announcement/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetSystemPastAnnouncements() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/super-admin/system-past-announcements/?page=1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestMockUser {

    @BeforeEach
    void init() {
      targetUser.setId(UuidUtil.getUuidString());
    }

    @Test
    void asManager_thenShouldFailed() throws Exception {
      buildAuthUserAsManager();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asEmployee_thenShouldFailed() throws Exception {
      buildAuthUserAsEmployee();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asDeactivatedUser_thenShouldFailed() throws Exception {
      buildAuthUserAsDeactivatedUser();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asAdminUser_thenShouldFailed() throws Exception {
      buildAuthUserAsAdmin();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asSuper_Admin_thenShouldSuccess() throws Exception {
      buildAuthUserAsSuperAdmin();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      "/company/super-admin/mock/companies/123/users/" + targetUser.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
  }
}
