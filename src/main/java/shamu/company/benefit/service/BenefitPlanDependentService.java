package shamu.company.benefit.service;

import java.util.List;
import java.util.Optional;
import shamu.company.benefit.entity.BenefitPlanDependent;

public interface BenefitPlanDependentService {

  void createBenefitPlanDependent(BenefitPlanDependent benefitPlanDependent);

  List<BenefitPlanDependent> getDependentListsByEmployeeId(Long id);

  void updateDependentContact(BenefitPlanDependent benefitPlanDependent);

  void deleteDependentContact(Long id);

  BenefitPlanDependent findDependentById(Long dependentId);

}
