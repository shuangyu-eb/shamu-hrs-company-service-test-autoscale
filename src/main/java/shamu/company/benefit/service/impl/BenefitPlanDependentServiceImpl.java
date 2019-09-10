package shamu.company.benefit.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.BenefitPlanDependentRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;

@Service
public class BenefitPlanDependentServiceImpl implements BenefitPlanDependentService {

  private final BenefitPlanDependentRepository benefitPlanDependentRepository;

  @Autowired
  public BenefitPlanDependentServiceImpl(
      final BenefitPlanDependentRepository benefitPlanDependentRepository) {
    this.benefitPlanDependentRepository = benefitPlanDependentRepository;
  }


  @Override
  public void createBenefitPlanDependent(final BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  @Override
  public List<BenefitPlanDependent> getDependentListsByEmployeeId(final Long id) {
    return benefitPlanDependentRepository.findByUserId(id);
  }

  @Override
  public void updateDependentContact(final BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  @Override
  public void deleteDependentContact(final Long id) {
    benefitPlanDependentRepository.delete(id);
  }

  @Override
  public BenefitPlanDependent findDependentById(final Long dependentId) {

    final Optional<BenefitPlanDependent> benefitPlanDependent
        = benefitPlanDependentRepository.findById(dependentId);
    return benefitPlanDependent.orElse(null);
  }
}
