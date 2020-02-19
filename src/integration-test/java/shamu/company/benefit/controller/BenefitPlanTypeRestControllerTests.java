package shamu.company.benefit.controller;

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
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.tests.utils.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = BenefitPlanTypeRestController.class)
public class BenefitPlanTypeRestControllerTests extends WebControllerBaseTests {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  BenefitPlanTypeRepository benefitPlanTypeRepository;

  @Test
  void testGetBenefitPlanTypes() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/all-benefit-plan-types")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

}
