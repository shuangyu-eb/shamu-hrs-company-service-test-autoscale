package shamu.company.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.FileInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmailUpdateDto;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserRoleUpdateDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = UserRestController.class)
public class UserRestControllerTests extends WebControllerBaseTests {

  @MockBean private UserMapper userMapper;

  @Autowired private MockMvc mockMvc;

  @Test
  void testSignUp() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(new UserSignUpDto())))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCheckEmail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/email-check/test@gmail.com")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteHeadPortrait() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete(
                        "/company/users/" + currentUser.getId() + "/head-portrait")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFileUpload() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);

    final FileInputStream fis = new FileInputStream("src/integration-test/resources/test.jpg");
    final MockMultipartFile file = new MockMultipartFile("file", fis);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.multipart(
                        "/company/users/" + currentUser.getId() + "/head-portrait")
                    .file(file)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSendResetPasswordEmail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/users/password-reset/test@gmail.com")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testResetPassword() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
    updatePasswordDto.setNewPassword("Eb123456");
    updatePasswordDto.setResetPasswordToken("token");

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/users/password-reset")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(updatePasswordDto)))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdatePassword() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final ChangePasswordDto changePasswordDto = new ChangePasswordDto();
    changePasswordDto.setNewPassword("Eb123456");

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/users/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(changePasswordDto)))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPreSetAccountInfo() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/" + currentUser.getId() + "/account-info")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserAvatar() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/" + currentUser.getId() + "/avatar")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateUserRole() throws Exception {
    setPermission(Permission.Name.VIEW_SETTING.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/users/" + currentUser.getId() + "/user-role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(new UserRoleUpdateDto())))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeactivateUser() throws Exception {
    setPermission(Permission.Name.DEACTIVATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch(
                        "/company/users/" + currentUser.getId() + "/deactivate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(new UserStatusUpdateDto())))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteUser() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/users/" + currentUser.getId())
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllUsers() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/users/").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserInfoWhenMockBySuperAdmin() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    httpHeaders.set("X-Mock-To", "mockId");

    final User user = new User();
    final UserRole userRole = new UserRole();
    userRole.setName(User.Role.SUPER_ADMIN.name());
    user.setUserRole(userRole);

    given(userService.findActiveUserById(Mockito.anyString())).willReturn(user);

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/current/user-info").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(userService, Mockito.times(1)).getMockUserInfo(Mockito.anyString());
  }

  @Test
  void testGetUserInfoWhenMockNotBySuperAdmin() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    httpHeaders.set("X-Mock-To", "mockId");

    final User user = new User();
    final UserRole userRole = new UserRole();
    userRole.setName(User.Role.EMPLOYEE.name());
    user.setUserRole(userRole);

    given(userService.findActiveUserById(Mockito.anyString())).willReturn(user);

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/current/user-info").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void testGetUserInfoWhenNotMock() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final CurrentUserDto userDto = new CurrentUserDto();
    userDto.setId(getAuthUser().getId());
    given(userService.getCurrentUserInfo(Mockito.anyString())).willReturn(userDto);

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/current/user-info").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(userService, Mockito.times(1)).getCurrentUserInfo(Mockito.anyString());
  }

  @Test
  void testCheckPassword() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/check-password/Eb123456")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateWorkEmail() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final EmailUpdateDto emailResendDto = new EmailUpdateDto();
    emailResendDto.setEmail("resendEmail@gmail.com");

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/users/work-email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(emailResendDto)))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetChangeWorkEmail() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User user = new User();
    user.setChangeWorkEmail("changedEmail@gmail.com");

    given(userService.findById(Mockito.anyString())).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/change-work-email").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSendVerifyChangeWorkEmail() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/send-verify-work-email")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testIsCurrentActiveAnnouncementDismissed() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    given(userService.isCurrentActiveAnnouncementDismissed(Mockito.any(), Mockito.any()))
        .willReturn(false);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/current-active-announcement/is-dismissed/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDismissCurrentActiveAnnouncement() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/users/dismiss-current-active-announcement/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
