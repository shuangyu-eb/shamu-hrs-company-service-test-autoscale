package shamu.company.benefit.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.benefit.dto.BenefitPlanClusterDto;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.benefit.entity.mapper.BenefitPlanCoverageMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanUserMapper;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.repository.RetirementPlanTypeRepository;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.mapper.UserMapper;

@Service
public class BenefitPlanServiceImpl implements BenefitPlanService {

  private final BenefitPlanRepository benefitPlanRepository;

  private final BenefitPlanUserRepository benefitPlanUserRepository;

  private final BenefitPlanCoverageRepository benefitPlanCoverageRepository;

  private final RetirementPlanTypeRepository retirementPlanTypeRepository;

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  private final UserMapper userMapper;

  private final BenefitPlanCoverageMapper benefitPlanCoverageMapper;

  private final BenefitPlanUserMapper benefitPlanUserMapper;

  private final BenefitPlanMapper benefitPlanMapper;

  public BenefitPlanServiceImpl(
      final BenefitPlanRepository benefitPlanRepository,
      final BenefitPlanUserRepository benefitPlanUserRepository,
      final BenefitPlanCoverageRepository benefitPlanCoverageRepository,
      final RetirementPlanTypeRepository retirementPlanTypeRepository,
      final BenefitPlanTypeRepository benefitPlanTypeRepository,
      final UserMapper userMapper,
      final BenefitPlanCoverageMapper benefitPlanCoverageMapper,
      final BenefitPlanUserMapper benefitPlanUserMapper,
      final BenefitPlanMapper benefitPlanMapper) {
    this.benefitPlanRepository = benefitPlanRepository;
    this.benefitPlanUserRepository = benefitPlanUserRepository;
    this.benefitPlanCoverageRepository = benefitPlanCoverageRepository;
    this.retirementPlanTypeRepository = retirementPlanTypeRepository;
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
    this.userMapper = userMapper;
    this.benefitPlanCoverageMapper = benefitPlanCoverageMapper;
    this.benefitPlanUserMapper = benefitPlanUserMapper;
    this.benefitPlanMapper = benefitPlanMapper;
  }

  @Override
  public BenefitPlan createBenefitPlan(final BenefitPlanCreateDto benefitPlanCreateDto,
      final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList,
      final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList, final Company company) {

    final BenefitPlan benefitPlan = benefitPlanMapper
        .createFromBenefitPlanCreateDto(benefitPlanCreateDto);
    benefitPlan.setCompany(company);

    final BenefitPlan createdBenefitPlan = benefitPlanRepository
        .save(benefitPlan);

    if (benefitPlanCreateDto.getRetirementTypeId() != null) {
      final RetirementPlanType retirementPlanType = new RetirementPlanType(createdBenefitPlan,
          new RetirementType(benefitPlanCreateDto.getRetirementTypeId()));
      retirementPlanTypeRepository.save(retirementPlanType);
    }

    if (!benefitPlanCoverageDtoList.isEmpty()) {
      benefitPlanCoverageRepository.saveAll(
          benefitPlanCoverageDtoList
              .stream()
              .map(benefitPlanCoverageDto -> benefitPlanCoverageMapper
                  .createFromBenefitPlanCoverageAndBenefitPlan(benefitPlanCoverageDto,
                      createdBenefitPlan))
              .collect(Collectors.toList())
      );
    }

    if (!benefitPlanUserCreateDtoList.isEmpty()) {
      benefitPlanUserRepository.saveAll(
          benefitPlanUserCreateDtoList
              .stream()
              .map(benefitPlanUserCreateDto ->
                  benefitPlanUserMapper
                      .createFromBenefitPlanUserCreateDtoAndBenefitPlanId(benefitPlanUserCreateDto,
                          createdBenefitPlan.getId()))
              .collect(Collectors.toList())
      );
    }

    return createdBenefitPlan;
  }

  @Override
  public BenefitPlan findBenefitPlanById(final Long id) {
    return benefitPlanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Can not find benefit plan"));
  }

  @Override
  public void save(final BenefitPlan benefitPlan) {
    benefitPlanRepository.save(benefitPlan);
  }

  @Override
  public List<BenefitPlanClusterDto> getBenefitPlanCluster(final Company company) {
    final List<BenefitPlan> benefitPlans = benefitPlanRepository.findBenefitPlanByCompany(company);
    final List<BenefitPlanType> benefitPlanTypes = benefitPlanTypeRepository.findAll();
    return generateBenefitPlanClusters(benefitPlans, benefitPlanTypes);
  }

  @Override
  public void updateBenefitPlanUsers(final Long benefitPlanId,
      final List<BenefitPlanUserCreateDto> benefitPlanUsers) {
    final List<BenefitPlanUser> originalBenefitPlanUsers = benefitPlanUserRepository
        .findAllByBenefitPlan(new BenefitPlan(benefitPlanId));

    final List<BenefitPlanUser> deleteBenefitPlanUsers = originalBenefitPlanUsers.stream()
        .filter(originalBenefitPlanUser -> benefitPlanUsers.stream()
            .noneMatch(benefitPlanUser -> benefitPlanUser.getId()
                .equals(originalBenefitPlanUser.getUser().getId()))
        ).collect(Collectors.toList());

    final List<BenefitPlanUserCreateDto> saveBenefitPlanUserCreateDtos = benefitPlanUsers.stream()
        .filter(
            benefitPlanUser -> originalBenefitPlanUsers.stream().noneMatch(
                originalBenefitPlanUser -> originalBenefitPlanUser.getUser().getId()
                    .equals(benefitPlanUser.getId()))).collect(Collectors.toList());

    benefitPlanUserRepository.delete(deleteBenefitPlanUsers);

    saveBenefitPlanUserCreateDtos
        .forEach(saveBenefitPlanUserCreateDto -> benefitPlanUserRepository.save(
            benefitPlanUserMapper
                .createFromBenefitPlanUserCreateDtoAndBenefitPlanId(saveBenefitPlanUserCreateDto,
                    benefitPlanId)));
  }

  private List<BenefitPlanClusterDto> generateBenefitPlanClusters(
      final List<BenefitPlan> benefitPlans,
      final List<BenefitPlanType> benefitPlanTypes) {
    final HashMap<String, BenefitPlanClusterDto> benefitPlanClusterDtoHashMap = new HashMap<>();

    benefitPlanTypes.forEach(benefitPlanType -> benefitPlanClusterDtoHashMap
        .put(benefitPlanType.getName(), new BenefitPlanClusterDto(benefitPlanType.getName(),
            new ArrayList<>())));

    benefitPlans.forEach(benefitPlan -> {
      final String benefitTypeName = benefitPlan.getBenefitPlanType().getName();

      final BenefitPlanClusterDto benefitPlanClusterDto = benefitPlanClusterDtoHashMap
          .get(benefitTypeName);

      benefitPlanClusterDto
          .setBenefitPlanNumber(benefitPlanClusterDto.getBenefitPlanNumber() + 1);

      final List<BenefitPlanUser> benefitPlanUserList = benefitPlanUserRepository
          .findAllByBenefitPlan(benefitPlan);

      final Integer eligibleNumber = benefitPlanUserList.size();
      final Integer enrolledNumber = Math
          .toIntExact(benefitPlanUserList.stream().filter(BenefitPlanUser::getEnrolled).count());
      final List<BenefitPlanUserDto> benefitPlanUsers = benefitPlanUserList
          .stream()
          .map(benefitPlanUser -> userMapper.convertToBenefitPlanUserDto(benefitPlanUser.getUser()))
          .collect(Collectors.toList());

      final List<BenefitPlanPreviewDto> benefitPlanPreviewDtoList = benefitPlanClusterDto
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
