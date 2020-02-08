package shamu.company.user.controller;

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
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = UserContactInformationRestController.class)
public class UserContactInformationRestControllerTests extends WebControllerBaseTests {

  @MockBean
  private UserContactInformationService contactInformationService;

  @MockBean
  private UserContactInformationMapper userContactInformationMapper;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testGetUserContactInformation() throws Exception {
    setPermission(Permission.Name.VIEW_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/" + getAuthUser().getId() + "/user-contact-information")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdate() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findUserByUserContactInformationId(currentUser.getId())).willReturn(targetUser);

    final UserContactInformationDto userContactInformationDto = new UserContactInformationDto();
    userContactInformationDto.setEmailWork("testEmail@gmail.com");
    userContactInformationDto.setPhoneWork("18002738255");
    userContactInformationDto.setPhoneHome("18002738255");

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/user-contact-information/" + getAuthUser().getId())
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(userContactInformationDto))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
