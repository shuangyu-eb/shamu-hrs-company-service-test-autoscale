package shamu.company.benefit.controller;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.benefit.dto.BenefitRequestInfoDto;
import shamu.company.benefit.entity.BenefitRequestApprovalStatus.BenefitRequestStatus;
import shamu.company.benefit.service.BenefitRequestService;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class BenefitRequestRestController {

  private final BenefitRequestService benefitRequestService;

  public BenefitRequestRestController(final BenefitRequestService benefitRequestService) {
    this.benefitRequestService = benefitRequestService;
  }

  @GetMapping("benefit/requests")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public PageImpl<BenefitRequestInfoDto> getRequestsByStatus(
      final int page, final int size, final String status) {
    final BenefitRequestStatus benefitRequestStatus = BenefitRequestStatus.valueOf(status);

    final PageRequest pageRequest = PageRequest.of(page, size);
    return benefitRequestService.findRequestsByStatus(pageRequest, benefitRequestStatus);
  }

  @GetMapping("benefit/pending-requests/count")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public Integer getPendingRequestsCount() {
    final BenefitRequestStatus benefitRequestStatus = BenefitRequestStatus.AWAITING_REVIEW;
    return benefitRequestService.findRequestsCountByStatus(benefitRequestStatus);
  }
}
