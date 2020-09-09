package shamu.company.benefit.controller;

import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.benefit.dto.BenefitRequestInfoDto;
import shamu.company.benefit.entity.BenefitRequestApprovalStatus.BenefitRequestStatus;
import shamu.company.benefit.service.BenefitRequestService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class BenefitRequestRestController extends BaseRestController {

  private final BenefitRequestService benefitRequestService;

  public BenefitRequestRestController(final BenefitRequestService benefitRequestService) {
    this.benefitRequestService = benefitRequestService;
  }

  @GetMapping("benefit/requests")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public Page<BenefitRequestInfoDto> getRequestsByStatus(
      final Integer page, final Integer size, final String[] status) {
    final List<String> statues = Arrays.asList(status);

    final PageRequest pageRequest = PageRequest.of(page, size);
    return benefitRequestService.findRequestsByStatus(pageRequest, statues);
  }

  @GetMapping("benefit/pending-requests/count")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public Integer getPendingRequestsCount() {
    final String benefitRequestStatus = BenefitRequestStatus.AWAITING_REVIEW.name();

    return benefitRequestService.findRequestsCountByStatus(benefitRequestStatus);
  }
}
