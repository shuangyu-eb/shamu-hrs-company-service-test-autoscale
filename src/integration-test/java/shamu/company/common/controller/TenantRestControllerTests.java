package shamu.company.common.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = TenantRestController.class)
public class TenantRestControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenNoLambdaToken_thenShouldForbidden() throws Exception {
    final String id = UuidUtil.getUuidString().toUpperCase();
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.post("/company/tenant/" + id).headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void whenHoldLambdaToken_thenShouldForbidden() throws Exception {
    httpHeaders.set("X-Tenant-Lambda-Token", "${lambda.token}");
    final String id = UuidUtil.getUuidString().toUpperCase();
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.post("/company/tenant/" + id).headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
