package shamu.company.timeoff.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import shamu.company.job.dto.JobUserDto;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayEmployeeDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = PaidHolidayRestController.class)
class PaidHolidayRestControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void testGetAllPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/paid-holidays").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserAllPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/paid-holidays/user/" + getAuthUser().getId() + "")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/employees").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPaidHolidaysEmployeesCount() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final PaidHolidayRelatedUserListDto paidHolidayRelatedUserListDto =
        new PaidHolidayRelatedUserListDto();
    final List<JobUserDto> jobUserDtoList = new ArrayList<>();
    paidHolidayRelatedUserListDto.setPaidHolidaySelectedEmployees(jobUserDtoList);
    given(paidHolidayService.getPaidHolidayEmployees(Mockito.any()))
        .willReturn(paidHolidayRelatedUserListDto);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/employees/count")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestUpdatePaidHolidayEmployees {

    private List<PaidHolidayEmployeeDto> paidHolidayEmployeeDtos;

    @BeforeEach
    void init() {
      targetUser.setId(UuidUtil.getUuidString());

      final PaidHolidayEmployeeDto paidHolidayEmployeeDto = new PaidHolidayEmployeeDto();
      paidHolidayEmployeeDto.setId(targetUser.getId());
      paidHolidayEmployeeDtos = Collections.singletonList(paidHolidayEmployeeDto);

      setGiven();
    }

    private class CommonTests {

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
    class SameCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setCompany(company);
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class DifferentCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setCompany(theOtherCompany);
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
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
              MockMvcRequestBuilders.patch("/company/paid-holidays/employees")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(paidHolidayEmployeeDtos))
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Test
  void testGetPaidHolidaysByYear() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/years/2020")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestUpdateHolidaySelects {

    private List<PaidHolidayDto> paidHolidayDtos;

    private PaidHoliday paidHoliday;

    private CompanyPaidHoliday companyPaidHoliday;

    @BeforeEach
    void init() {
      paidHoliday = new PaidHoliday();
      paidHoliday.setId(UuidUtil.getUuidString());

      companyPaidHoliday = new CompanyPaidHoliday();
      companyPaidHoliday.setId(UuidUtil.getUuidString());
      companyPaidHoliday.setPaidHoliday(paidHoliday);

      final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
      paidHolidayDto.setId(paidHoliday.getId());
      paidHolidayDtos = Collections.singletonList(paidHolidayDto);
    }

    private class CommonTests {

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
    class SameCompany extends CommonTests {

      @BeforeEach
      void init() {
        companyPaidHoliday.setCompany(company);
        setGiven();
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class DifferentCompany extends CommonTests {

      @BeforeEach
      void init() {
        companyPaidHoliday.setCompany(theOtherCompany);
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(
              companyPaidHolidayService.findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(
                  companyPaidHoliday.getPaidHoliday().getId(),
                  companyPaidHoliday.getCompany().getId()))
          .willReturn(companyPaidHoliday);
      given(companyPaidHolidayService.findAllByCompanyId(companyPaidHoliday.getCompany().getId()))
          .willReturn(Collections.singletonList(companyPaidHoliday));
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/paid-holidays/select")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(paidHolidayDtos))
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Test
  void testCreatePaidHoliday() throws Exception {
    setPermission(Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/paid-holidays")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new PaidHolidayDto()))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestDeletePaidHoliday {

    PaidHoliday paidHoliday;

    @BeforeEach
    void init() {
      paidHoliday = new PaidHoliday();
      paidHoliday.setId(UuidUtil.getUuidString());
      setGiven();
    }

    private class CommonTests {

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
    class DefaultPaidHoliday extends CommonTests {

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class CreatedByCurrentUser extends CommonTests {

      @BeforeEach
      void init() {
        paidHoliday.setCompany(company);
        paidHoliday.setCreator(new User(currentUser.getId()));
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class NotCreatedByCurrentUser extends DefaultPaidHoliday {

      @BeforeEach
      void init() {
        paidHoliday.setCompany(company);
        paidHoliday.setCreator(new User(UuidUtil.getUuidString()));
      }
    }

    @Nested
    class DifferentCompany extends DefaultPaidHoliday {

      @BeforeEach
      void init() {
        paidHoliday.setCompany(theOtherCompany);
        paidHoliday.setCreator(new User(UuidUtil.getUuidString()));
      }
    }

    private void setGiven() {
      given(paidHolidayService.getPaidHoliday(paidHoliday.getId())).willReturn(paidHoliday);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.delete("/company/paid-holidays/" + paidHoliday.getId())
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Nested
  class TestUpdatePaidHoliday extends TestDeletePaidHoliday {

    PaidHolidayDto paidHolidayDto;

    @Override
    @BeforeEach
    void init() {
      paidHolidayDto = new PaidHolidayDto();
      paidHolidayDto.setId(UuidUtil.getUuidString());

      paidHoliday = new PaidHoliday();
      paidHoliday.setId(paidHolidayDto.getId());
      setGiven();
    }

    private void setGiven() {
      given(paidHolidayService.getPaidHoliday(paidHoliday.getId())).willReturn(paidHoliday);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/paid-holidays/1")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(paidHolidayDto))
                  .headers(httpHeaders))
          .andReturn();
    }
  }
}
