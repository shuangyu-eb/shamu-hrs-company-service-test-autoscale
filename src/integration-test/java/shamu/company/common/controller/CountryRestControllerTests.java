package shamu.company.common.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.common.service.CountryService;
import shamu.company.tests.utils.JwtUtil;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = CountryRestController.class)
public class CountryRestControllerTests extends WebControllerBaseTests {

  @MockBean
  private CountryService countryService;
  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFindCountries() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(countryService.findCountries()).willReturn(new ArrayList<>());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/countries")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

}
