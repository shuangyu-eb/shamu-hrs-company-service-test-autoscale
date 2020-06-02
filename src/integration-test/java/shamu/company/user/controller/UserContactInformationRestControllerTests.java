package shamu.company.user.controller;

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
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = UserContactInformationRestController.class)
public class UserContactInformationRestControllerTests extends WebControllerBaseTests {

  @MockBean private UserContactInformationService contactInformationService;

  @MockBean private UserContactInformationMapper userContactInformationMapper;

  @Autowired private MockMvc mockMvc;

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

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/" + getAuthUser().getId() + "/user-contact-information")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class testUpdate {

    private AuthUser currentUser;

    private User targetUser;

    private UserContactInformationDto userContactInformationDto;

    @BeforeEach
    void init() {
      currentUser = getAuthUser();
      targetUser = new User();
      userContactInformationDto = new UserContactInformationDto();
      userContactInformationDto.setEmailWork("testEmail@gmail.com");
      userContactInformationDto.setPhoneWork("18002738255");
      userContactInformationDto.setPhoneHome("18002738255");
    }

    @Test
    void asSelf_thenShouldSucess() throws Exception {
      buildAuthUserAsDeactivedUser();
      final Company company = new Company(currentUser.getCompanyId());
      targetUser.setCompany(company);
      targetUser.setId(currentUser.getId());
      setGiven();

      final MvcResult response = getResponse();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Nested
    class SameCompany {

      private Company company;

      @BeforeEach
      void init() {
        company = new Company(currentUser.getCompanyId());
        targetUser.setCompany(company);
        targetUser.setId(UuidUtil.getUuidString());
        setGiven();
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final User manager = new User(UuidUtil.getUuidString());
        manager.setCompany(company);
        targetUser.setManagerUser(manager);

        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asManager_notBelongToTargetUser_thenShouldFailed() throws Exception {
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
        buildAuthUserAsDeactivedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class DifferentCompany {

      private final Company theOtherCompany = new Company(UuidUtil.getUuidString());

      @BeforeEach
      void init() {
        targetUser.setCompany(theOtherCompany);
        targetUser.setId(UuidUtil.getUuidString());
        setGiven();
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
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
        buildAuthUserAsDeactivedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(userService.findUserByUserContactInformationId(targetUser.getId()))
          .willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch(
                      "/company/user-contact-information/" + targetUser.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(userContactInformationDto)))
          .andReturn();
    }
  }
}
