package shamu.company.benefit.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.benefit.dto.BenefitPlanClusterDto;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.benefit.pojo.BenefitPlanCoveragePojo;
import shamu.company.benefit.pojo.BenefitPlanPojo;
import shamu.company.benefit.pojo.BenefitPlanUserPojo;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
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

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  public BenefitPlanServiceImpl(
      BenefitPlanRepository benefitPlanRepository,
      BenefitPlanUserRepository benefitPlanUserRepository,
      BenefitPlanCoverageRepository benefitPlanCoverageRepository,
      RetirementPlanTypeRepository retirementPlanTypeRepository,
      BenefitPlanTypeRepository benefitPlanTypeRepository) {
    this.benefitPlanRepository = benefitPlanRepository;
    this.benefitPlanUserRepository = benefitPlanUserRepository;
    this.benefitPlanCoverageRepository = benefitPlanCoverageRepository;
    this.retirementPlanTypeRepository = retirementPlanTypeRepository;
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
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
                  benefitPlanUserPojo.getBenefitPlanUser(createdBenefitPlan.getId()))
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

  @Override
  public List<BenefitPlanClusterDto> getBenefitPlanCluster(Company company) {
    List<BenefitPlan> benefitPlans = benefitPlanRepository.findBenefitPlanByCompany(company);
    List<BenefitPlanType> benefitPlanTypes = benefitPlanTypeRepository.findAll();
    return generateBenefitPlanClusters(benefitPlans, benefitPlanTypes);
  }

  @Override
  public void updateBenefitPlanUsers(Long benefitPlanId,
      List<BenefitPlanUserPojo> benefitPlanUsers) {
    List<BenefitPlanUser> originalBenefitPlanUsers = benefitPlanUserRepository
        .findAllByBenefitPlan(new BenefitPlan(benefitPlanId));

    List<BenefitPlanUser> deleteBenefitPlanUsers = originalBenefitPlanUsers.stream()
        .filter(originalBenefitPlanUser -> benefitPlanUsers.stream()
            .noneMatch(benefitPlanUser -> benefitPlanUser.getId()
                .equals(originalBenefitPlanUser.getUser().getId()))
        ).collect(Collectors.toList());

    List<BenefitPlanUserPojo> saveBenefitPlanUserPojos = benefitPlanUsers.stream().filter(
        benefitPlanUser -> originalBenefitPlanUsers.stream().noneMatch(
            originalBenefitPlanUser -> originalBenefitPlanUser.getUser().getId()
                .equals(benefitPlanUser.getId()))).collect(Collectors.toList());

    benefitPlanUserRepository.delete(deleteBenefitPlanUsers);

    saveBenefitPlanUserPojos.forEach(saveBenefitPlanUserPojo -> benefitPlanUserRepository.save(
        saveBenefitPlanUserPojo.getBenefitPlanUser(benefitPlanId)));
  }

  private List<BenefitPlanClusterDto> generateBenefitPlanClusters(List<BenefitPlan> benefitPlans,
      List<BenefitPlanType> benefitPlanTypes) {
    HashMap<String, BenefitPlanClusterDto> benefitPlanClusterDtoHashMap = new HashMap<>();

    benefitPlanTypes.forEach(benefitPlanType -> benefitPlanClusterDtoHashMap
        .put(benefitPlanType.getName(), new BenefitPlanClusterDto(benefitPlanType.getName(),
            new ArrayList<>())));

    benefitPlans.forEach(benefitPlan -> {
      String benefitTypeName = benefitPlan.getBenefitPlanType().getName();

      BenefitPlanClusterDto benefitPlanClusterDto = benefitPlanClusterDtoHashMap
          .get(benefitTypeName);

      benefitPlanClusterDto
          .setBenefitPlanNumber(benefitPlanClusterDto.getBenefitPlanNumber() + 1);

      List<BenefitPlanUser> benefitPlanUserList = benefitPlanUserRepository
          .findAllByBenefitPlan(benefitPlan);

      Integer eligibleNumber = benefitPlanUserList.size();
      Integer enrolledNumber = Math
          .toIntExact(benefitPlanUserList.stream().filter(BenefitPlanUser::getEnrolled).count());
      List<BenefitPlanUserDto> benefitPlanUsers = benefitPlanUserList
          .stream()
          .map(benefitPlanUser -> new BenefitPlanUserDto(benefitPlanUser.getUser()))
          .collect(Collectors.toList());

      List<BenefitPlanPreviewDto> benefitPlanPreviewDtoList = benefitPlanClusterDto
          .getBenefitPlans();
      benefitPlanPreviewDtoList
          .add(new BenefitPlanPreviewDto(benefitPlan.getId(), benefitPlan.getName(),
              eligibleNumber, enrolledNumber, benefitPlanUsers));
      benefitPlanClusterDto.setBenefitPlans(benefitPlanPreviewDtoList);

      benefitPlanClusterDtoHashMap.put(benefitTypeName, benefitPlanClusterDto);
    });

    return new ArrayList<>(benefitPlanClusterDtoHashMap.values());
  }
}
