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
      BenefitPlanDependentRepository benefitPlanDependentRepository) {
    this.benefitPlanDependentRepository = benefitPlanDependentRepository;
  }


  @Override
  public void createBenefitPlanDependent(BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  @Override
  public List<BenefitPlanDependent> getDependentListsByEmployeeId(Long id) {
    return benefitPlanDependentRepository.findByUserId(id);
  }

  @Override
  public void updateDependentContact(BenefitPlanDependent benefitPlanDependent) {
    benefitPlanDependentRepository.save(benefitPlanDependent);
  }

  @Override
  public void deleteDependentContact(Long id) {
    benefitPlanDependentRepository.delete(id);
  }

  @Override
  public BenefitPlanDependent findDependentById(Long dependentId) {

    Optional<BenefitPlanDependent> benefitPlanDependent
        = benefitPlanDependentRepository.findById(dependentId);
    return benefitPlanDependent.get() == null
        ? null : benefitPlanDependent.get();
  }
}
