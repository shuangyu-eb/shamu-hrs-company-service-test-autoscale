package shamu.company.payroll.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
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
import shamu.company.financialengine.dto.CompanyTaxIdDto;
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
    List<PayrollSetupEmployeeDto> payrollSetupEmployeeDtos = new ArrayList<>();
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
    List<CompanyTaxIdDto> companyTaxIdDtos = new ArrayList<>();
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
}
