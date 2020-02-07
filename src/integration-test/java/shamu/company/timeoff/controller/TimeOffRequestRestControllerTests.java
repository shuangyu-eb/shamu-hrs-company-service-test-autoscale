package shamu.company.timeoff.controller;

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
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = TimeOffRequestRestController.class)
class TimeOffRequestRestControllerTests extends WebControllerBaseTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testCreateTimeOffRequest() throws Exception {
    setPermission(Name.MANAGE_SELF_TIME_OFF_REQUEST.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/users/"+getAuthUser().getId()+"/time-off-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(new TimeOffRequestCreateDto()))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreateTimeOffRequestAndApprove() throws Exception {
    setPermission(Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/users/"+getAuthUser().getId()+"/time-off-requests/approved")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(new TimeOffRequestCreateDto()))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPendingTimeOffRequestsCount() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off_requests/approver/status/pending/count")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindTimeOffRequests() throws Exception {
    setPermission(Name.VIEW_TEAM_TIME_OFF_REQUEST.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/"+getAuthUser().getId()+"/time-off-requests?status="+ TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED+"")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findTimeOffRequest() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(targetUser);
    given(timeOffRequestService.getById(Mockito.any())).willReturn(timeOffRequest);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-requests/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateTimeOffRequestStatus() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(targetUser);
    given(timeOffRequestService.getById(Mockito.any())).willReturn(timeOffRequest);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/time-off-requests/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(new TimeOffRequestUpdateDto()))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindPendingRequestsByApprover() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-pending-requests/approver?page=1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindReviewedRequestsByApprover() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-reviewed-requests/approver?page=1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindPendingRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-pending-requests/requester/"+getAuthUser().getId()+"?page=1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindReviewedRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-reviewed-requests/requester/"+getAuthUser().getId()+"?startDay=1&&endDay=1&&page=1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindMyTimeOffRequests() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-requests/approved-after-now/requester/"+getAuthUser().getId()+"")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteUnimplementedRequest() throws Exception {
    setPermission(Name.EDIT_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(Mockito.any())).willReturn(targetUser);
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(targetUser);
    given(timeOffRequestService.getById(Mockito.any())).willReturn(timeOffRequest);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .delete("/company/time-off-requests/1/unimplemented-requests/"+getAuthUser().getId()+"")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testHasUserPermission() throws Exception {
    setPermission(Name.VIEW_TEAM_TIME_OFF_REQUEST.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    final UserRole userRole = new UserRole();
    userRole.setName(User.Role.ADMIN.getValue());
    targetUser.setUserRole(userRole);
    given(userService.findById(Mockito.any())).willReturn(targetUser);
    given(userService.findActiveUserById(Mockito.any())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/time-off-requests/has-privilege/users/"+getAuthUser().getId()+"")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
