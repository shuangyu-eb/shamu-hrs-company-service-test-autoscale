package shamu.company.financialengine.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
import shamu.company.financialengine.dto.BankAccountInfoDto;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.service.FEBankService;
import shamu.company.tests.utils.JwtUtil;

/** @author Lucas */
@WebMvcTest(controllers = FEBankRestController.class)
class FEBankRestControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;
  @MockBean FEBankService feBankService;

  @Test
  void testGetBankConnection() throws Exception {
    final BankConnectionWidgetDto bankConnectionWidgetDto = new BankConnectionWidgetDto();
    given(feBankService.getCompanyBankConnection()).willReturn(bankConnectionWidgetDto);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/financial-engine/bank/connection-widget")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBankAccount() throws Exception {
    final BankAccountInfoDto BankAccount = new BankAccountInfoDto();
    given(feBankService.getCompanyBankAccountInfo()).willReturn(BankAccount);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/financial-engine/bank/account-info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
