package shamu.company.benefit.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.common.exception.OldResourceNotFoundException;

@Service
public class BenefitPlanTypeService {

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  @Autowired
  public BenefitPlanTypeService(final BenefitPlanTypeRepository benefitPlanTypeRepository) {
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
  }

  public BenefitPlanType findBenefitPlanTypeById(final String id) {
    return benefitPlanTypeRepository
        .findById(id)
        .orElseThrow(() -> new OldResourceNotFoundException("Benefit plan type was not found"));
  }

  public List<BenefitPlanType> findAllBenefitPlanTypes() {
    return benefitPlanTypeRepository.findAll();
  }
}
