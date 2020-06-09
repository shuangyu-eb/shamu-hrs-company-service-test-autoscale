package shamu.company.benefit.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

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
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Benefit plan type with id %s not found!", id),
                    id,
                    "benefit plan type"));
  }

  public List<BenefitPlanType> findAllBenefitPlanTypes() {
    return benefitPlanTypeRepository.findAll();
  }
}
