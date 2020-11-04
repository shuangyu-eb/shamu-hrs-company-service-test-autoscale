package shamu.company.payroll.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.payroll.dto.PayrollAuthorizedEmployeeDto;
import shamu.company.payroll.dto.PayrollSetupEmployeeDto;
import shamu.company.payroll.service.PayrollSetUpService;
import shamu.company.tests.utils.JwtUtil;

@WebMvcTest(controllers = PayrollSetUpRestController.class)
class PayrollSetUpRestControllerTests extends WebControllerBaseTests {

  @MockBean private PayrollSetUpService payrollSetUpService;

  @Autowired private MockMvc mockMvc;

  @Test
  void testGetEmployeesCompensation() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<PayrollSetupEmployeeDto> payrollSetupEmployeeDtos = new ArrayList<>();
    given(payrollSetUpService.getPayrollSetUpEmployees()).willReturn(payrollSetupEmployeeDtos);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/payroll/setup/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetTaxList() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<CompanyTaxIdDto> companyTaxIdDtos = new ArrayList<>();
    given(payrollSetUpService.getTaxList()).willReturn(companyTaxIdDtos);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/payroll/required-tax-fields")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPayrollAuthorizedSignerEmployees() throws Exception {
    final Page<PayrollAuthorizedEmployeeDto> result = new PageImpl<>(Collections.emptyList());
    given(payrollSetUpService.getPayrollAuthorizedEmployees(Mockito.any())).willReturn(result);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/payroll/setup/authorized-signers/employees")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
