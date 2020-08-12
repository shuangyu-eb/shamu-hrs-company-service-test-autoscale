package shamu.company.account.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = AccountRestController.class)
public class AccountRestControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void testCreatePasswordTokenExist() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/account/password/123/qwe")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreatePassword() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final CreatePasswordDto createPasswordDto = new CreatePasswordDto();
    createPasswordDto.setEmailWork("example@example.com");
    createPasswordDto.setNewPassword("856723Xy");
    createPasswordDto.setResetPasswordToken("1");
    createPasswordDto.setCompanyId("asdzxc");

    Mockito.when(tenantService.isCompanyExists(createPasswordDto.getCompanyId())).thenReturn(true);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/account/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(createPasswordDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreatePasswordAndInvitationTokenExist() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    given(accountService.createPasswordAndInvitationTokenExist("1", "2")).willReturn(true);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/account/password/1/qwe").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUnlock() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setEmailWork("example@example.com");
    userLoginDto.setPassword("1");
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/account/unlock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(userLoginDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testValidateChangeWorkEmail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(userService.changeWorkEmailTokenExist("1")).willReturn(true);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/account/change-work-email/1/qwe")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testResendVerificationEmail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/account/1/verification-email")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Nested
  class testFindByEmailWork {
    @Test
    void whenNotVerified_thenShouldReturnOK() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      Mockito.when(userService.checkUserVerifiedEmail(Mockito.anyString())).thenReturn(false);
      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get("/company/account/email/example@example.com")
                      .headers(httpHeaders))
              .andReturn();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void whenVerified_thenShouldReturnNoContent() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      Mockito.when(userService.checkUserVerifiedEmail(Mockito.anyString())).thenReturn(true);
      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get("/company/account/email/example@example.com")
                      .headers(httpHeaders))
              .andReturn();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
  }
}
