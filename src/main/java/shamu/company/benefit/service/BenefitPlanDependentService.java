package shamu.company.benefit.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.BenefitPlanDependentRepository;

@Service
public class BenefitPlanDependentService {

  private final BenefitPlanDependentRepository benefitPlanDependentRepository;

  @Autowired
  public BenefitPlanDependentService(
      final BenefitPlanDependentRepository benefitPlanDependentRepository) {
    this.benefitPlanDependentRepository = benefitPlanDependentRepository;
  }


  public void createBenefitPlanDependent(final BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  public List<BenefitPlanDependent> getDependentListsByEmployeeId(final Long id) {
    return benefitPlanDependentRepository.findByUserId(id);
  }

  public void updateDependentContact(final BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  public void deleteDependentContact(final Long id) {
    benefitPlanDependentRepository.delete(id);
  }

  public BenefitPlanDependent findDependentById(final Long dependentId) {

    final Optional<BenefitPlanDependent> benefitPlanDependent
        = benefitPlanDependentRepository.findById(dependentId);
    return benefitPlanDependent.orElse(null);
  }
}
