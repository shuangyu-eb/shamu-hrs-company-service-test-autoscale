package shamu.company.timeoff.controller;

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
import shamu.company.authorization.Permission.Name;
import shamu.company.company.entity.Company;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.dto.TimeOffRequestCreateDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = TimeOffRequestRestController.class)
class TimeOffRequestRestControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @Nested
  class TestCreateTimeOffRequest {

    @Nested
    class SameUser {

      @BeforeEach
      void init() {
        targetUser.setId(currentUser.getId());
        setGiven();
      }

      @Test
      void asSelf_asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asSelf_asEmployee_noManager_thenShouldFailed() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asSelf_asEmployee_hasManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsEmployee();
        targetUser.setManagerUser(new User(UuidUtil.getUuidString()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asSelf_asManager_noManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asSelf_asManager_hasManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsManager();
        targetUser.setManagerUser(new User(UuidUtil.getUuidString()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asSelf_asAdmin_noManager_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asSelf_asAdmin_hasManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        targetUser.setManagerUser(new User(UuidUtil.getUuidString()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class OtherUser {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
        setGiven();
      }

      @Test
      void asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
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
      void asManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      "/company/users/" + targetUser.getId() + "/time-off-requests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(new TimeOffRequestCreateDto())))
          .andReturn();
    }
  }

  @Nested
  class TestCreateTimeOffRequestAndApprove {

    @Nested
    class SameUser {

      @BeforeEach
      void init() {
        targetUser.setId(currentUser.getId());
        setGiven();
      }

      @Test
      void asSelf_noManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asSelf_hasManager_thenShouldFailed() throws Exception {
        buildAuthUserAsEmployee();
        targetUser.setManagerUser(new User(UuidUtil.getUuidString()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asSelf_asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class OtherUser {

      @BeforeEach
      void init() {
        company = new Company(currentUser.getCompanyId());
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
      void asManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsManager();
        targetUser.setManagerUser(new User(currentUser.getId()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
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
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      "/company/users/" + targetUser.getId() + "/time-off-requests/approved")
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(new TimeOffRequestCreateDto())))
          .andReturn();
    }
  }

  @Test
  void testGetPendingTimeOffRequestsCount() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-requests/approver/status/pending/count")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindTimeOffRequests() throws Exception {
    setPermission(Name.VIEW_TEAM_TIME_OFF_REQUEST.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/users/"
                            + getAuthUser().getId()
                            + "/time-off-requests?status="
                            + TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED
                            + "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(
                        JsonUtil.formatToString(
                            TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED)))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findTimeOffRequest() throws Exception {
    setPermission(Name.VIEW_TEAM_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(targetUser);
    given(userService.findById(Mockito.any())).willReturn(targetUser);
    given(timeOffRequestService.getById(Mockito.any())).willReturn(timeOffRequest);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-requests/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestUpdateTimeOffRequestStatus {

    private TimeOffRequest timeOffRequest;

    @BeforeEach
    void init() {
      timeOffRequest = new TimeOffRequest();
      timeOffRequest.setId(UuidUtil.getUuidString());
      timeOffRequest.setRequesterUser(targetUser);
      setGiven();
    }

    @Nested
    class SameUser {

      // As self, whatever the role he is, should return failed.

      @BeforeEach
      void init() {
        targetUser.setId(currentUser.getId());
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
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class SameCompany {

      // Only the admin or the manager of the targetUser could manage requests of the targetUser.

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asManager_belongToTargetUser_thenShouldSuccess() throws Exception {
        buildAuthUserAsManager();
        targetUser.setManagerUser(new User(currentUser.getId()));
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
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
      given(timeOffRequestService.getById(timeOffRequest.getId())).willReturn(timeOffRequest);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/time-off-requests/" + timeOffRequest.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(new TimeOffRequestUpdateDto())))
          .andReturn();
    }
  }

  @Test
  void testFindPendingRequestsByApprover() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-pending-requests/approver?page=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindReviewedRequestsByApprover() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-reviewed-requests/approver?page=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindPendingRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-pending-requests/requester/"
                            + getAuthUser().getId()
                            + "?page=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindReviewedRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-reviewed-requests/requester/"
                            + getAuthUser().getId()
                            + "?startDay=1&&endDay=1&&page=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindMyTimeOffRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-requests/approved-after-now/requester/"
                            + getAuthUser().getId()
                            + "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteUnimplementedRequest() throws Exception {
    setPermission(Name.MANAGE_SELF_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(Mockito.any())).willReturn(targetUser);
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(targetUser);
    final TimeOffRequestApprovalStatus status = new TimeOffRequestApprovalStatus();
    status.setName(TimeOffApprovalStatus.AWAITING_REVIEW.name());
    timeOffRequest.setTimeOffRequestApprovalStatus(status);
    given(timeOffRequestService.getById(Mockito.any())).willReturn(timeOffRequest);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/time-off-requests/1/unimplemented-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestDeleteUnimplementedRequest {

    private TimeOffRequest timeOffRequest;

    @BeforeEach
    void init() {
      timeOffRequest = new TimeOffRequest();
      timeOffRequest.setId(UuidUtil.getUuidString());
      timeOffRequest.setRequesterUser(targetUser);
      setGiven();
    }

    @Nested
    class SameUser {

      @BeforeEach
      void init() {
        targetUser.setId(currentUser.getId());
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asManager_thenShouldSuccess() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asEmployee_thenShouldSuccess() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }

      @Test
      void asDeactivated_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class OtherUser {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
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
        targetUser.setManagerUser(new User(currentUser.getId()));
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
      void asDeactivated_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(timeOffRequestService.getById(timeOffRequest.getId())).willReturn(timeOffRequest);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.delete(
                      String.format(
                          "/company/time-off-requests/%s/unimplemented-requests",
                          timeOffRequest.getId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Test
  void testHasUserPermission() throws Exception {
    setPermission(Name.VIEW_TEAM_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    final UserRole userRole = new UserRole();
    userRole.setName(User.Role.ADMIN.getValue());
    targetUser.setUserRole(userRole);
    given(userService.findById(Mockito.any())).willReturn(targetUser);
    given(userService.findActiveUserById(Mockito.any())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-requests/has-privilege/users/"
                            + getAuthUser().getId()
                            + "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
