package shamu.company.benefit.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import shamu.company.benefit.dto.BenefitRequestInfoDto;
import shamu.company.benefit.entity.BenefitRequest;
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

  public PageImpl<BenefitRequestInfoDto> findRequestsByStatusAndCompanyId(
      final PageRequest pageRequest, final List<String> statuses, final String companyId) {

    final Page<BenefitRequest> benefitRequests =
        benefitRequestRepository.findAllByStatusAndCompanyId(statuses, companyId, pageRequest);

    return (PageImpl<BenefitRequestInfoDto>)
        benefitRequests.map(benefitRequestMapper::convertToBenefitRequestInfoDto);
  }

  public Integer findRequestsCountByStatusAndCompanyId(
      final String status, final String companyId) {
    return benefitRequestRepository.countRequestsByStatusAndCompanyId(status, companyId);
  }
}
