package shamu.company.admin.controller;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = SuperAdminRestController.class)
class SuperAdminRestControllerTests extends WebControllerBaseTests {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SuperAdminService superAdminService;

  @Test
  void testGetSystemActiveAnnouncement() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final SystemAnnouncementDto systemAnnouncement = new SystemAnnouncementDto();
    systemAnnouncement.setId("1");
    given(superAdminService.getSystemActiveAnnouncement()).willReturn(systemAnnouncement);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/super-admin/system-active-announcement")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testPublicSystemAnnouncement() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/super-admin/publish-system-announcement")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(new SystemAnnouncementDto()))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateSystemActiveAnnouncement() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/super-admin/system-active-announcement/1")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetSystemPastAnnouncements() throws Exception {
    setPermission(Permission.Name.SUPER_PERMISSION.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/super-admin/system-past-announcements/?page=1")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
