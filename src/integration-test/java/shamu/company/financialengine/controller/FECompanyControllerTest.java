package shamu.company.financialengine.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

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
import shamu.company.financialengine.dto.CompanyInformationDto;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.financialengine.dto.NewFECompanyInformationDto;
import shamu.company.financialengine.service.FECompanyService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = FECompanyController.class)
class FECompanyControllerTest extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @MockBean FECompanyService feCompanyService;

  @Test
  void testGetAvailableIndustries() throws Exception {
    final List<IndustryDto> industries = new ArrayList<>();
    given(feCompanyService.getAvailableIndustries()).willReturn(industries);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/financial-engine/available-industries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetCompanyInformation() throws Exception {
    final CompanyInformationDto companyInformationDto = new CompanyInformationDto();
    given(feCompanyService.getCompanyInformation()).willReturn(companyInformationDto);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/financial-engine/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSaveFinancialEngine() throws Exception {
    final NewFECompanyInformationDto companyDetailsDto = new NewFECompanyInformationDto();
    doNothing().when(feCompanyService).saveFinancialEngine(companyDetailsDto);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/financial-engine/save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(companyDetailsDto))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetLegalEntityType() throws Exception {
    final List<LegalEntityTypeDto> legalEntityTypes = new ArrayList<>();
    given(feCompanyService.getLegalEntityTypes()).willReturn(legalEntityTypes);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/financial-engine/legal-entity-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
