package shamu.company.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.common.service.StateProvinceService;
import shamu.company.tests.utils.JwtUtil;

@WebMvcTest(controllers = StateProvinceRestController.class)
public class StateProvinceRestControllerTests extends WebControllerBaseTests {
  @MockBean private StateProvinceService stateProvinceService;
  @Autowired private MockMvc mockMvc;

  @Test
  void testFindAllStateProvincesByCountry() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(stateProvinceService.findAllByCountry(Mockito.anyString())).willReturn(new ArrayList<>());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/country/1/state-provinces")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
