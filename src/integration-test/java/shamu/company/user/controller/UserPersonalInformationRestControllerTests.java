package shamu.company.user.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import shamu.company.crypto.EncryptorUtil;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = UserPersonalInformationRestController.class)
public class UserPersonalInformationRestControllerTests extends WebControllerBaseTests {

  @MockBean
  private UserPersonalInformationService userPersonalInformationService;

  @MockBean
  private UserPersonalInformationMapper userPersonalInformationMapper;

  @MockBean
  private UserMapper userMapper;

  @MockBean
  private Auth0Helper auth0Helper;

  @MockBean
  private EncryptorUtil encryptorUtil;

  @Autowired
  private MockMvc mockMvc;

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

    final UserPersonalInformationDto userPersonalInformationDto = new UserPersonalInformationDto();

    given(userService.findUserByUserPersonalInformationId(currentUser.getId())).willReturn(targetUser);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/user-personal-information/" + currentUser.getId())
        .contentType(MediaType.APPLICATION_JSON).headers(httpHeaders)
        .content(JsonUtil.formatToString(userPersonalInformationDto))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserPersonalInformation() throws Exception{
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
        .get("/company/users/" + currentUser.getId() + "/user-personal-information")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserRoleAndStatus() throws Exception{
    setPermission(Permission.Name.VIEW_USER_ROLE_AND_STATUS.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(userMapper.convertToUserRoleAndStatusInfoDto(Mockito.any())).willReturn(new UserRoleAndStatusInfoDto());
    given(auth0Helper.getUserRole(targetUser)).willReturn(User.Role.ADMIN);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/" + currentUser.getId() + "/user-role-status")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
