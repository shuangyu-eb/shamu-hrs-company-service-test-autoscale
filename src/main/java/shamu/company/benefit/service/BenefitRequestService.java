package shamu.company.benefit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import shamu.company.benefit.dto.BenefitRequestInfoDto;
import shamu.company.benefit.entity.BenefitRequest;
import shamu.company.benefit.entity.BenefitRequestApprovalStatus.BenefitRequestStatus;
import shamu.company.benefit.entity.mapper.BenefitRequestMapper;
import shamu.company.benefit.repository.BenefitRequestRepository;

@Service
public class BenefitRequestService {

  private final BenefitRequestRepository benefitRequestRepository;

  private final BenefitRequestMapper benefitRequestMapper;

  @Autowired
  public BenefitRequestService(
      final BenefitRequestRepository benefitRequestRepository,
      final BenefitRequestMapper benefitRequestMapper) {
    this.benefitRequestRepository = benefitRequestRepository;
    this.benefitRequestMapper = benefitRequestMapper;
  }

  public PageImpl<BenefitRequestInfoDto> findRequestsByStatus(
      final PageRequest pageRequest, final BenefitRequestStatus status) {

    final Page<BenefitRequest> benefitRequests =
        benefitRequestRepository.findAllByStatus(status, pageRequest);

    return (PageImpl<BenefitRequestInfoDto>)
        benefitRequests.map(benefitRequestMapper::convertToBenefitRequestInfoDto);
  }

  public Integer findRequestsCountByStatus(final BenefitRequestStatus status) {
    return benefitRequestRepository.countRequestsByStatus(status);
  }
}
