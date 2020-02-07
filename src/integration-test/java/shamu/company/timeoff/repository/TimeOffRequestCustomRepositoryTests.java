package shamu.company.timeoff.repository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import shamu.company.DataLayerBaseTests;
import shamu.company.timeoff.entity.TimeOffRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class TimeOffRequestCustomRepositoryTests extends DataLayerBaseTests {

  @MockBean
  private TimeOffRequestCustomRepositoryImpl timeOffRequestCustomRepository;

  @Test
  void testGetFilteredReviewedTimeOffRequestsIds() throws Exception {
    final List<String> result = new ArrayList<>();
    given(timeOffRequestCustomRepository.getFilteredReviewedTimeOffRequestsIds(Mockito.any(), Mockito.any(), Mockito.any())).willReturn(result);
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  void testFindByTimeOffPolicyUserAndStatus() throws Exception {
    final List<TimeOffRequest> result = new ArrayList<>();
    given(timeOffRequestCustomRepository.findByTimeOffPolicyUserAndStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willReturn(result);
    assertThat(result.size()).isEqualTo(0);
  }

}
