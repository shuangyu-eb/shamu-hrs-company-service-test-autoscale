package shamu.company.info.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
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
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = EmergencyContactRestController.class)
public class EmergencyContactRestControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void testGetEmergencyContacts() throws Exception {
    setPermission(Permission.Name.VIEW_USER_EMERGENCY_CONTACT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final UserRole role = new UserRole();
    final User user = new User();
    role.setName("ADMIN");
    user.setUserRole(role);
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(userService.findById("1")).willReturn(user);
    given(userEmergencyContactService.findUserEmergencyContacts("1")).willReturn(new ArrayList<>());
    given(userService.findActiveUserById(Mockito.anyString())).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/1/user-emergency-contacts")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestCreateEmergencyContacts {

    private AuthUser currentUser;

    private User targetUser;

    private final UserEmergencyContactDto userEmergencyContactDto = new UserEmergencyContactDto();

    @BeforeEach
    void init() {
      currentUser = getAuthUser();
      targetUser = new User();
    }

    @Test
    void asSelf_thenShouldSuccess() throws Exception {
      buildAuthUserAsDeactivedUser();
      targetUser.setId(currentUser.getId());
      targetUser.setCompany(new Company(currentUser.getCompanyId()));
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
        final User manager = new User(getAuthUser().getId());
        manager.setCompany(company);
        targetUser.setManagerUser(manager);
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
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
      given(userEmergencyContactMapper.createFromUserEmergencyContactDto(userEmergencyContactDto))
          .willReturn(new UserEmergencyContact());
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      String.format(
                          "/company/users/%s/user-emergency-contacts", targetUser.getId()))
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(userEmergencyContactDto)))
          .andReturn();
    }
  }

  @Nested
  class TestDeleteEmergencyContacts {

    private AuthUser currentUser;

    private User targetUser;

    private UserEmergencyContact userEmergencyContact;

    @BeforeEach
    void init() {
      currentUser = getAuthUser();
      targetUser = new User();
      userEmergencyContact = new UserEmergencyContact();
      userEmergencyContact.setUser(targetUser);
      userEmergencyContact.setId(UuidUtil.getUuidString());
    }

    @Test
    void asSelf_thenShouldSuccess() throws Exception {
      buildAuthUserAsDeactivedUser();
      targetUser.setId(currentUser.getId());
      targetUser.setCompany(new Company(currentUser.getCompanyId()));
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
        final User manager = new User(getAuthUser().getId());
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
      given(userEmergencyContactService.findById(userEmergencyContact.getId()))
          .willReturn(userEmergencyContact);
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.delete(
                      String.format(
                          "/company/user-emergency-contacts/%s", userEmergencyContact.getId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Nested
  class TestUpdateEmergencyContact {

    private AuthUser currentUser;

    private User targetUser;

    private UserEmergencyContact userEmergencyContact;

    private UserEmergencyContactDto userEmergencyContactDto;

    @BeforeEach
    void init() {
      currentUser = getAuthUser();
      targetUser = new User();
      userEmergencyContact = new UserEmergencyContact();
      userEmergencyContact.setId(UuidUtil.getUuidString());
      userEmergencyContact.setUser(targetUser);
      userEmergencyContactDto = new UserEmergencyContactDto();
      userEmergencyContactDto.setId(userEmergencyContact.getId());
    }

    @Test
    void asSelf_thenShouldSuccess() throws Exception {
      buildAuthUserAsDeactivedUser();
      targetUser.setId(currentUser.getId());
      targetUser.setCompany(new Company(currentUser.getCompanyId()));
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
        final User manager = new User(getAuthUser().getId());
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
      given(userEmergencyContactService.findById(userEmergencyContact.getId()))
          .willReturn(userEmergencyContact);
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/user-emergency-contacts")
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(userEmergencyContactDto)))
          .andReturn();
    }
  }
}
