package shamu.company.timeoff.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
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
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.repository.TimeOffAccrualFrequencyRepository;

@WebMvcTest(controllers = TimeOffAccrualFrequenciesRestController.class)
class TimeOffAccrualFrequenciesRestControllerTests extends WebControllerBaseTests {

  @MockBean private TimeOffAccrualFrequencyRepository timeOffAccrualFrequencyRepository;

  @Autowired private MockMvc mockMvc;

  @Test
  void testGetAllTimeOffFrequencies() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    given(timeOffAccrualFrequencyRepository.findAll()).willReturn(Collections.emptyList());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-accrual-frequencies")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
