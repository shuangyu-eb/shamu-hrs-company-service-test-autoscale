package shamu.company.benefit.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.benefit.pojo.BenefitPlanCoveragePojo;
import shamu.company.benefit.pojo.BenefitPlanPojo;
import shamu.company.benefit.pojo.BenefitPlanUserPojo;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.repository.RetirementPlanTypeRepository;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;

@Service
public class BenefitPlanServiceImpl implements BenefitPlanService {

  private final BenefitPlanRepository benefitPlanRepository;

  private final BenefitPlanUserRepository benefitPlanUserRepository;

  private final BenefitPlanCoverageRepository benefitPlanCoverageRepository;

  private final RetirementPlanTypeRepository retirementPlanTypeRepository;

  @Autowired
  public BenefitPlanServiceImpl(
      BenefitPlanRepository benefitPlanRepository,
      BenefitPlanUserRepository benefitPlanUserRepository,
      BenefitPlanCoverageRepository benefitPlanCoverageRepository,
      RetirementPlanTypeRepository retirementPlanTypeRepository) {
    this.benefitPlanRepository = benefitPlanRepository;
    this.benefitPlanUserRepository = benefitPlanUserRepository;
    this.benefitPlanCoverageRepository = benefitPlanCoverageRepository;
    this.retirementPlanTypeRepository = retirementPlanTypeRepository;
  }

  @Override
  public BenefitPlan createBenefitPlan(BenefitPlanPojo benefitPlanPojo,
      List<BenefitPlanCoveragePojo> benefitPlanCoveragePojoList,
      List<BenefitPlanUserPojo> benefitPlanUserPojoList, Company company) {

    BenefitPlan benefitPlan = benefitPlanPojo.getBenefitPlan(company);

    BenefitPlan createdBenefitPlan = benefitPlanRepository
        .save(benefitPlan);

    if (benefitPlanPojo.getRetirementTypeId() != null) {
      RetirementPlanType retirementPlanType = new RetirementPlanType(createdBenefitPlan,
          new RetirementType(benefitPlanPojo.getRetirementTypeId()));
      retirementPlanTypeRepository.save(retirementPlanType);
    }

    if (!benefitPlanCoveragePojoList.isEmpty()) {
      benefitPlanCoverageRepository.saveAll(
          benefitPlanCoveragePojoList
              .stream()
              .map(benefitPlanCoveragePojo -> benefitPlanCoveragePojo
                  .getBenefitPlanCoverage(createdBenefitPlan))
              .collect(Collectors.toList())
      );
    }

    if (!benefitPlanUserPojoList.isEmpty()) {
      benefitPlanUserRepository.saveAll(
          benefitPlanUserPojoList
              .stream()
              .map(benefitPlanUserPojo ->
                  benefitPlanUserPojo.getBenefitPlanUser(createdBenefitPlan))
              .collect(Collectors.toList())
      );
    }

    return createdBenefitPlan;
  }

  @Override
  public BenefitPlan findBenefitPlanById(Long id) {
    return benefitPlanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Can not find benefit plan"));
  }

  @Override
  public void save(BenefitPlan benefitPlan) {
    benefitPlanRepository.save(benefitPlan);
  }
}
