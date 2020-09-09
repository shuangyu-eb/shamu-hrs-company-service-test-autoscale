package shamu.company.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = UserPersonalInformationRestController.class)
public class UserPersonalInformationRestControllerTests extends WebControllerBaseTests {

  @MockBean private UserPersonalInformationService userPersonalInformationService;

  @MockBean private UserPersonalInformationMapper userPersonalInformationMapper;

  @MockBean private UserMapper userMapper;

  @MockBean private EncryptorUtil encryptorUtil;

  @Autowired private MockMvc mockMvc;

  @Nested
  class testUpdate {

    private final UserPersonalInformationDto userPersonalInformationDto =
        new UserPersonalInformationDto();

    @Test
    void asSelf_thenShouldSuccess() throws Exception {
      buildAuthUserAsEmployee();
      targetUser.setId(currentUser.getId());

      setGiven();

      final MvcResult response = getResponse();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Nested
    class SameCompany {

      @BeforeEach
      void init() {
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
        final User manager = new User(currentUser.getId());
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
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(userService.findUserByUserPersonalInformationId(targetUser.getId()))
          .willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch(
                      "/company/user-personal-information/" + targetUser.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(userPersonalInformationDto)))
          .andReturn();
    }
  }

  @Test
  void testGetUserPersonalInformation() throws Exception {
    setPermission(Permission.Name.VIEW_SELF.name());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/" + currentUser.getId() + "/user-personal-information")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserRoleAndStatus() throws Exception {
    setPermission(Permission.Name.VIEW_USER_ROLE_AND_STATUS.name());

    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    targetUser.setId(currentUser.getId());

    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(userMapper.convertToUserRoleAndStatusInfoDto(Mockito.any()))
        .willReturn(new UserRoleAndStatusInfoDto());
    given(auth0Helper.getUserRole(targetUser)).willReturn(User.Role.ADMIN);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/" + currentUser.getId() + "/user-role-status")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
