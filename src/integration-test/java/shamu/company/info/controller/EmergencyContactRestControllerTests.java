package shamu.company.info.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
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
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.JsonUtil;

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

  @Test
  void testCreateEmergencyContacts() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final User user = new User();
    final UserEmergencyContactDto userEmergencyContactDto = new UserEmergencyContactDto();
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(userService.findById("1")).willReturn(user);
    given(userEmergencyContactMapper.createFromUserEmergencyContactDto(userEmergencyContactDto))
        .willReturn(new UserEmergencyContact());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/users/1/user-emergency-contacts")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(userEmergencyContactDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteEmergencyContacts() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final User user = new User();
    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    userEmergencyContact.setUser(user);
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(userEmergencyContactService.findById(Mockito.anyString()))
        .willReturn(userEmergencyContact);
    given(userService.findById("1")).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/user-emergency-contacts/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateEmergencyContact() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final User user = new User();
    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
    final UserEmergencyContactDto userEmergencyContactDto = new UserEmergencyContactDto();
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    userEmergencyContact.setUser(user);
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(userEmergencyContactService.findById(Mockito.anyString()))
        .willReturn(userEmergencyContact);
    given(userService.findById("1")).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/user-emergency-contacts")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(userEmergencyContactDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
