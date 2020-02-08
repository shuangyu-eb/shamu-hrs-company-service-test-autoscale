package shamu.company.user.controller;

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
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.service.UserBenefitsSettingService;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = UserBenefitsSettingRestController.class)
public class UserBenefitsSettingRestControllerTests extends WebControllerBaseTests {

  @MockBean
  private UserBenefitsSettingService benefitService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFindUserBenefitsEffectYear() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefits-setting/effect-year/2020")
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSaveUserBenefitsEffectYear() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/benefits-setting/effect-year")
        .contentType(MediaType.ALL)
        .headers(httpHeaders).content("2020")).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
