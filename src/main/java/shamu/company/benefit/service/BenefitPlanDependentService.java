package shamu.company.benefit.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.UserDependentsRepository;

@Service
public class BenefitPlanDependentService {

  private final UserDependentsRepository userDependentsRepository;

  @Autowired
  public BenefitPlanDependentService(
      final UserDependentsRepository userDependentsRepository) {
    this.userDependentsRepository = userDependentsRepository;
  }


  public void createBenefitPlanDependent(final BenefitPlanDependent benefitPlanDependent) {
    userDependentsRepository.save(benefitPlanDependent);
  }

  public List<BenefitPlanDependent> getDependentListsByEmployeeId(final String id) {
    return userDependentsRepository.findByUserId(id);
  }

  public void updateDependentContact(final BenefitPlanDependent benefitPlanDependent) {
    userDependentsRepository.save(benefitPlanDependent);
  }

  public void deleteDependentContact(final String id) {
    userDependentsRepository.delete(id);
  }

  public BenefitPlanDependent findDependentById(final String dependentId) {

    final Optional<BenefitPlanDependent> benefitPlanDependent
        = userDependentsRepository.findById(dependentId);
    return benefitPlanDependent.orElse(null);
  }
}
