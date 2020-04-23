package shamu.company.benefit.controller;

import static org.assertj.core.api.Assertions.assertThat;

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
import shamu.company.authorization.Permission;
import shamu.company.benefit.service.BenefitRequestService;
import shamu.company.tests.utils.JwtUtil;

@WebMvcTest(controllers = BenefitRequestRestController.class)
public class BenefitRequestRestControllerTests extends WebControllerBaseTests {

  @MockBean BenefitRequestService benefitRequestService;
  @Autowired private MockMvc mockMvc;

  @Test
  void testGetRequestsByStatus() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    String[] strings = new String[1];
    strings[0] = "1";
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/benefit/requests")
                    .param("status", strings)
                    .param("page", "1")
                    .param("size", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPendingRequestsCount() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/benefit/pending-requests/count")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
