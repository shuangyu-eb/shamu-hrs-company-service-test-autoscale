package shamu.company.benefit;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import shamu.company.benefit.entity.BenefitRequest;
import shamu.company.benefit.entity.mapper.BenefitRequestMapper;
import shamu.company.benefit.repository.BenefitRequestRepository;
import shamu.company.benefit.service.BenefitRequestService;

public class BenefitRequestServiceTests {

  @Mock private BenefitRequestRepository benefitRequestRepository;

  @Mock private BenefitRequestMapper benefitRequestMapper;

  private BenefitRequestService benefitRequestService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    benefitRequestService =
        new BenefitRequestService(benefitRequestRepository, benefitRequestMapper);
  }

  @Test
  void testFindRequestsByStatusAndCompanyId() {
    final PageRequest pageRequest = Mockito.mock(PageRequest.class);
    final List<String> list = new ArrayList<>();
    final Page<BenefitRequest> benefitRequests = Mockito.mock(Page.class);
    Mockito.when(
            benefitRequestRepository.findAllByStatusAndCompanyId(
                Mockito.anyList(), Mockito.anyString(), Mockito.any()))
        .thenReturn(benefitRequests);
    Assertions.assertDoesNotThrow(
        () -> benefitRequestService.findRequestsByStatusAndCompanyId(pageRequest, list, "1"));
  }

  @Test
  void testFindRequestsCountByStatusAndCompanyId() {
    Assertions.assertDoesNotThrow(
        () -> benefitRequestService.findRequestsCountByStatusAndCompanyId("1", "1"));
  }
}
