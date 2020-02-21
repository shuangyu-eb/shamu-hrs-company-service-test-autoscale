package shamu.company.email.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.email.service.EmailService;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = EmailRestController.class)
class EmailRestControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @MockBean private EmailService emailService;

  @Test
  void testUpdateEmailStatus() throws Exception {
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/emails/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(Collections.emptyList())))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
