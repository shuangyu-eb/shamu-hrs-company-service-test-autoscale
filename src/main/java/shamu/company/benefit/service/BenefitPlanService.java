package shamu.company.benefit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.benefit.dto.BenefitCoveragesDto;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCoveragesDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanRelatedUserListDto;
import shamu.company.benefit.dto.BenefitPlanReportDto;
import shamu.company.benefit.dto.BenefitPlanReportSummaryDto;
import shamu.company.benefit.dto.BenefitPlanSearchCondition;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.dto.BenefitPlanTypeEnum;
import shamu.company.benefit.dto.BenefitPlanTypeWithoutExpiredDto;
import shamu.company.benefit.dto.BenefitPlanUpdateDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.dto.BenefitReportCoveragesDto;
import shamu.company.benefit.dto.BenefitReportParamDto;
import shamu.company.benefit.dto.BenefitReportPlansDto;
import shamu.company.benefit.dto.BenefitSummaryDto;
import shamu.company.benefit.dto.EnrollmentBreakdownDto;
import shamu.company.benefit.dto.EnrollmentBreakdownSearchCondition;
import shamu.company.benefit.dto.NewBenefitPlanWrapperDto;
import shamu.company.benefit.dto.RetirementDto;
import shamu.company.benefit.dto.RetirementUserDto;
import shamu.company.benefit.dto.SelectedEnrollmentInfoDto;
import shamu.company.benefit.dto.UserBenefitPlanDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitDependentRecord;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.BenefitPlanDocument;
import shamu.company.benefit.entity.BenefitPlanPreviewPojo;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanType.PlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.BenefitReportPlansPojo;
import shamu.company.benefit.entity.EnrollmentBreakdownPojo;
import shamu.company.benefit.entity.RetirementPayTypes;
import shamu.company.benefit.entity.RetirementPayTypes.PayTypes;
import shamu.company.benefit.entity.RetirementPayment;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.benefit.entity.mapper.BenefitCoveragesMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanCoverageMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanDependentMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanReportMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanUserMapper;
import shamu.company.benefit.entity.mapper.MyBenefitsMapper;
import shamu.company.benefit.entity.mapper.RetirementPaymentMapper;
import shamu.company.benefit.entity.mapper.RetirementPlanTypeMapper;
import shamu.company.benefit.repository.BenefitCoveragesRepository;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanDependentRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.repository.RetirementPaymentRepository;
import shamu.company.benefit.repository.RetirementPlanTypeRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.helpers.s3.AccessType;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.RetirementPayTypesRepository;
import shamu.company.user.repository.RetirementTypeRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserBenefitsSettingService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@Service
public class BenefitPlanService {

  private static final String BENEFIT_NEW_COVERAGE = "add";

  private static final String DEFAULT_ID = "default";

  private static final String RETIREMENT_TYPE_NAME = "Retirement";

  private final BenefitPlanRepository benefitPlanRepository;

  private final BenefitPlanUserRepository benefitPlanUserRepository;

  private final BenefitPlanCoverageRepository benefitPlanCoverageRepository;

  private final RetirementPlanTypeRepository retirementPlanTypeRepository;

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  private final BenefitCoveragesRepository benefitCoveragesRepository;

  private final BenefitPlanCoverageMapper benefitPlanCoverageMapper;

  private final BenefitPlanUserMapper benefitPlanUserMapper;

  private final RetirementPlanTypeMapper retirementPlanTypeMapper;

  private final BenefitPlanMapper benefitPlanMapper;

  private final MyBenefitsMapper myBenefitsMapper;

  private final UserMapper userMapper;

  private final BenefitCoveragesMapper benefitCoveragesMapper;

  private final UserRepository userRepository;

  private final BenefitPlanDependentRepository benefitPlanDependentRepository;

  private final AwsHelper awsHelper;

  private final UserBenefitsSettingService userBenefitsSettingService;

  private final JobUserService jobUserService;

  private final JobUserMapper jobUserMapper;

  private final BenefitPlanDependentMapper benefitPlanDependentMapper;

  private final BenefitPlanReportMapper benefitPlanReportMapper;

  private final UserService userService;

  private final RetirementPaymentMapper retirementPaymentMapper;

  private final RetirementPayTypesRepository retirementPayTypesRepository;

  private final RetirementPaymentRepository retirementPaymentRepository;

  private final RetirementTypeRepository retirementTypeRepository;

  public BenefitPlanService(
      final BenefitPlanRepository benefitPlanRepository,
      final BenefitPlanUserRepository benefitPlanUserRepository,
      final BenefitPlanCoverageRepository benefitPlanCoverageRepository,
      final RetirementPlanTypeRepository retirementPlanTypeRepository,
      final BenefitPlanTypeRepository benefitPlanTypeRepository,
      final BenefitCoveragesRepository benefitCoveragesRepository,
      final BenefitPlanCoverageMapper benefitPlanCoverageMapper,
      final RetirementPlanTypeMapper retirementPlanTypeMapper,
      final BenefitPlanUserMapper benefitPlanUserMapper,
      final BenefitPlanMapper benefitPlanMapper,
      final MyBenefitsMapper myBenefitsMapper,
      final UserMapper userMapper,
      final BenefitCoveragesMapper benefitCoveragesMapper,
      final UserRepository userRepository,
      final BenefitPlanDependentRepository benefitPlanDependentRepository,
      final AwsHelper awsHelper,
      final UserBenefitsSettingService userBenefitsSettingService,
      final JobUserService jobUserService,
      final JobUserMapper jobUserMapper,
      final BenefitPlanDependentMapper benefitPlanDependentMapper,
      final BenefitPlanReportMapper benefitPlanReportMapper,
      final UserService userService,
      final RetirementPaymentMapper retirementPaymentMapper,
      final RetirementPayTypesRepository retirementPayTypesRepository,
      final RetirementPaymentRepository retirementPaymentRepository,
      final RetirementTypeRepository retirementTypeRepository) {
    this.benefitPlanRepository = benefitPlanRepository;
    this.benefitPlanUserRepository = benefitPlanUserRepository;
    this.benefitPlanCoverageRepository = benefitPlanCoverageRepository;
    this.retirementPlanTypeRepository = retirementPlanTypeRepository;
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
    this.benefitCoveragesRepository = benefitCoveragesRepository;
    this.benefitPlanCoverageMapper = benefitPlanCoverageMapper;
    this.benefitPlanUserMapper = benefitPlanUserMapper;
    this.retirementPlanTypeMapper = retirementPlanTypeMapper;
    this.benefitPlanMapper = benefitPlanMapper;
    this.myBenefitsMapper = myBenefitsMapper;
    this.userMapper = userMapper;
    this.benefitCoveragesMapper = benefitCoveragesMapper;
    this.userRepository = userRepository;
    this.benefitPlanDependentRepository = benefitPlanDependentRepository;
    this.awsHelper = awsHelper;
    this.userBenefitsSettingService = userBenefitsSettingService;
    this.jobUserService = jobUserService;
    this.jobUserMapper = jobUserMapper;
    this.benefitPlanDependentMapper = benefitPlanDependentMapper;
    this.benefitPlanReportMapper = benefitPlanReportMapper;
    this.userService = userService;
    this.retirementPaymentMapper = retirementPaymentMapper;
    this.retirementPayTypesRepository = retirementPayTypesRepository;
    this.retirementPaymentRepository = retirementPaymentRepository;
    this.retirementTypeRepository = retirementTypeRepository;
  }

  static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
    final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  public BenefitPlanDto createBenefitPlan(final NewBenefitPlanWrapperDto data) {
    final BenefitPlanCreateDto benefitPlanCreateDto = data.getBenefitPlan();
    final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = data.getCoverages();
    final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList = data.getSelectedEmployees();
    final boolean isRetirement = data.getCoverages().isEmpty();

    final Map<String, List<BenefitPlanUserCreateDto>> benefitUsers =
        benefitPlanUserCreateDtoList.stream()
            .collect(Collectors.groupingBy(BenefitPlanUserCreateDto::getCoverage));

    final BenefitPlan benefitPlan =
        benefitPlanMapper.createFromBenefitPlanCreateDto(benefitPlanCreateDto);

    final BenefitPlan createdBenefitPlan = benefitPlanRepository.save(benefitPlan);

    if (isRetirement) {
      saveRetirementPayment(benefitPlanCreateDto, benefitPlan);

      final RetirementType retirementType =
          retirementTypeRepository.findByName(
              benefitPlanCreateDto.getRetirement().getRetirementTypeName());

      final RetirementPlanType retirementPlanType =
          new RetirementPlanType(createdBenefitPlan, retirementType);
      retirementPlanTypeRepository.save(retirementPlanType);
    } else if (!benefitPlanCoverageDtoList.isEmpty()) {
      saveBenefitCoverages(benefitPlanCoverageDtoList, createdBenefitPlan, benefitUsers);
    }

    saveBenefitPlanUsers(isRetirement, benefitPlanUserCreateDtoList, createdBenefitPlan);

    return benefitPlanMapper.convertToBenefitPlanDto(benefitPlan);
  }

  private void saveBenefitCoverages(
      final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList,
      final BenefitPlan createdBenefitPlan,
      final Map<String, List<BenefitPlanUserCreateDto>> benefitUsers) {
    benefitPlanCoverageDtoList.forEach(
        benefitPlanCoverageDto -> {
          if (benefitPlanCoverageDto.getId().startsWith(BENEFIT_NEW_COVERAGE)) {
            final BenefitCoverages benefitCoverages =
                benefitCoveragesRepository.save(
                    benefitCoveragesMapper.createFromBenefitPlanCoverageDtoAndPlan(
                        benefitPlanCoverageDto, createdBenefitPlan));
            final String newBenefitId = benefitCoverages.getId();
            changeEmployeesCoverage(benefitUsers.get(benefitPlanCoverageDto.getId()), newBenefitId);
            benefitPlanCoverageDto.setId(benefitCoverages.getId());
          }
        });

    benefitPlanCoverageRepository.saveAll(
        benefitPlanCoverageDtoList.stream()
            .map(
                benefitCoverageDto ->
                    benefitPlanCoverageMapper.createFromBenefitPlanCoverageAndBenefitPlan(
                        benefitCoverageDto,
                        createdBenefitPlan,
                        getBenefitCoveragesById(benefitCoverageDto.getId())))
            .collect(Collectors.toList()));
  }

  private void saveRetirementPayment(
      final BenefitPlanCreateDto benefitPlanCreateDto, final BenefitPlan createdBenefitPlan) {
    final RetirementPayment retirementPayment =
        retirementPaymentMapper.convertToRetirementPayment(benefitPlanCreateDto);
    final RetirementDto retirementDto = benefitPlanCreateDto.getRetirement();
    final RetirementPayTypes percentageRetirementType =
        retirementPayTypesRepository.findByName(PayTypes.PERCENTAGE_OF_GROSS_PAY.getValue());
    final RetirementPayTypes amountRetirementType =
        retirementPayTypesRepository.findByName(PayTypes.AMOUNT.getValue());

    retirementPayment.setEmployeeDeduction(
        retirementDto.getIsEmployeePercentage() ? percentageRetirementType : amountRetirementType);

    retirementPayment.setCompanyContribution(
        retirementDto.getIsCompanyPercentage() ? percentageRetirementType : amountRetirementType);

    retirementPayment.setLimitStandard(retirementDto.getIsDeductionLimit());
    retirementPayment.setBenefitPlan(createdBenefitPlan);
    retirementPaymentRepository.save(retirementPayment);
  }

  private void saveBenefitPlanUsers(
      final boolean isRetirement,
      final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList,
      final BenefitPlan createdBenefitPlan) {
    if (isRetirement) {
      retirementPaymentRepository.saveAll(
          benefitPlanUserCreateDtoList.stream()
              .map(
                  benefitPlanUserCreateDto ->
                      retirementPaymentMapper.convertToRetirementPayment(
                          benefitPlanUserCreateDto,
                          new RetirementPayTypes(benefitPlanUserCreateDto.getEmployeeDeduction()),
                          new RetirementPayTypes(benefitPlanUserCreateDto.getCompanyContribution()),
                          new User(benefitPlanUserCreateDto.getId()),
                          createdBenefitPlan))
              .collect(Collectors.toList()));
    } else {
      benefitPlanUserRepository.saveAll(
          benefitPlanUserCreateDtoList.stream()
              .map(
                  benefitPlanUserCreateDto ->
                      benefitPlanUserMapper.createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
                          benefitPlanUserCreateDto,
                          createdBenefitPlan.getId(),
                          benefitPlanCoverageRepository.getByBenefitPlanIdAndBenefitCoverageId(
                              createdBenefitPlan.getId(), benefitPlanUserCreateDto.getCoverage()),
                          true,
                          true))
              .collect(Collectors.toList()));
    }
  }

  private void changeEmployeesCoverage(
      final List<BenefitPlanUserCreateDto> coverageUsers, final String newCoverageId) {
    if (!CollectionUtils.isEmpty(coverageUsers)) {
      coverageUsers.forEach(s -> s.setCoverage(newCoverageId));
    }
  }

  public BenefitPlanDto updateBenefitPlan(
      final NewBenefitPlanWrapperDto data, final String planId) {
    final BenefitPlanCreateDto benefitPlanCreateDto = data.getBenefitPlan();
    final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = data.getCoverages();
    final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList = data.getSelectedEmployees();

    final BenefitPlan benefitPlan = benefitPlanRepository.findBenefitPlanById(planId);

    benefitPlanMapper.updateFromBenefitPlanCreateDto(benefitPlan, benefitPlanCreateDto);

    updateBenefitDocuments(benefitPlan, data.getBenefitPlan().getRemainingDocumentIds());
    final BenefitPlan updatedBenefitPlan = benefitPlanRepository.save(benefitPlan);

    // update retirement benefit plan
    if (CollectionUtils.isEmpty(data.getCoverages())) {
      retirementPaymentRepository.delete(
          retirementPaymentRepository.findByBenefitPlanAndUserIsNull(updatedBenefitPlan).getId());
      saveRetirementPayment(benefitPlanCreateDto, updatedBenefitPlan);
      updateRetirementPlanType(benefitPlanCreateDto, planId, updatedBenefitPlan);
    }

    List<BenefitPlanCoverage> benefitPlanCoverageList = new ArrayList<>();

    if (!CollectionUtils.isEmpty(benefitPlanCoverageDtoList)) {
      benefitPlanCoverageList =
          updateBenefitPlanCoverage(
              benefitPlanCoverageDtoList, planId, benefitPlanUserCreateDtoList);
    }

    updateBenefitPlanUser(data, planId, benefitPlanCoverageList);

    // is not retirement type
    if (!CollectionUtils.isEmpty(benefitPlanCoverageDtoList)) {
      final List<String> benefitPlanIds =
          benefitPlanCoverageList.stream()
              .map(BenefitPlanCoverage::getId)
              .collect(Collectors.toList());
      final List<BenefitCoverages> coverages =
          benefitPlanCoverageList.stream()
              .map(BenefitPlanCoverage::getBenefitCoverage)
              .collect(Collectors.toList());
      final List<String> coverageIds =
          coverages.stream().map(BenefitCoverages::getId).collect(Collectors.toList());
      final List<BenefitCoverages> benefitCoverages =
          benefitCoveragesRepository.findAllByBenefitPlanId(planId);
      final List<BenefitPlanCoverage> benefitPlanCoverages =
          benefitPlanCoverageRepository.findAllByBenefitPlanId(planId);
      benefitPlanCoverages.forEach(
          s -> {
            if (!benefitPlanIds.contains(s.getId())) {
              benefitPlanCoverageRepository.delete(s);
            }
          });
      benefitCoverages.forEach(
          s -> {
            if (!coverageIds.contains(s.getId())) {
              benefitCoveragesRepository.delete(s);
            }
          });
    }

    return benefitPlanMapper.convertToBenefitPlanDto(benefitPlan);
  }

  private void updateBenefitDocuments(
      final BenefitPlan benefitPlan, final List<String> documentIds) {
    final Set<BenefitPlanDocument> documents = benefitPlan.getBenefitPlanDocuments();

    final List<String> deletedDocumentUrls =
        documents.stream()
            .filter(document -> documentIds.stream().anyMatch(id -> !document.getId().equals(id)))
            .map(BenefitPlanDocument::getUrl)
            .collect(Collectors.toList());
    awsHelper.amazonS3DeleteObject(deletedDocumentUrls);

    final Set<BenefitPlanDocument> remainingDocuments =
        documents.stream()
            .filter(document -> documentIds.stream().anyMatch(id -> document.getId().equals(id)))
            .collect(Collectors.toSet());
    benefitPlan.setBenefitPlanDocuments(remainingDocuments);
  }

  private void updateRetirementPlanType(
      final BenefitPlanCreateDto benefitPlanCreateDto,
      final String planId,
      final BenefitPlan updatedBenefitPlan) {
    final RetirementPlanType retirementPlanType =
        retirementPlanTypeRepository.findByBenefitPlan(new BenefitPlan(planId));
    final String currentPlanTypeName = benefitPlanCreateDto.getRetirement().getRetirementTypeName();

    if (!currentPlanTypeName.equals(retirementPlanType.getRetirementType().getName())) {
      final RetirementType retirementType =
          retirementTypeRepository.findByName(currentPlanTypeName);
      final RetirementPlanType newRetirementPlanType =
          new RetirementPlanType(updatedBenefitPlan, retirementType);
      retirementPlanTypeMapper.updateFromNewRetirementPlanType(
          retirementPlanType, newRetirementPlanType);
      retirementPlanTypeRepository.save(retirementPlanType);
    }
  }

  private List<BenefitPlanCoverage> updateBenefitPlanCoverage(
      final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList,
      final String planId,
      final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList) {

    final List<BenefitPlanCoverage> result = new ArrayList<>();
    final Map<String, List<BenefitPlanUserCreateDto>> benefitUsers =
        benefitPlanUserCreateDtoList.stream()
            .collect(Collectors.groupingBy(BenefitPlanUserCreateDto::getCoverage));

    final List<BenefitPlanCoverage> benefitPlanCoverages =
        benefitPlanCoverageRepository.findAllByBenefitPlanId(planId);
    final List<String> existBenefitPlanCoverageIds =
        benefitPlanCoverages.stream().map(BenefitPlanCoverage::getId).collect(Collectors.toList());

    benefitPlanCoverageDtoList.forEach(
        s -> {
          if (!s.getId().startsWith(BENEFIT_NEW_COVERAGE)
              && existBenefitPlanCoverageIds.contains(s.getId())) {
            final BenefitPlanCoverage currentBenefitPlanCoverage =
                getBenefitPlanCoverageById(s.getId());
            final BenefitPlanCoverage benefitPlanCoverage =
                benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndPlanCoverage(
                    s, currentBenefitPlanCoverage);
            benefitPlanCoverage.setBenefitPlanId(planId);
            final BenefitPlanCoverage newBenefitPlanCoverage =
                benefitPlanCoverageRepository.save(benefitPlanCoverage);
            result.add(newBenefitPlanCoverage);
          }

          if (!s.getId().startsWith(BENEFIT_NEW_COVERAGE)
              && !existBenefitPlanCoverageIds.contains(s.getId())) {
            final BenefitCoverages newBenefitCoverages = getBenefitCoveragesById(s.getId());
            final BenefitPlanCoverage benefitPlanCoverage =
                benefitPlanCoverageMapper.createFromBenefitPlanCoverageDto(s, newBenefitCoverages);
            benefitPlanCoverage.setBenefitPlanId(planId);
            final BenefitPlanCoverage basicBenefitPlanCoverage =
                benefitPlanCoverageRepository.save(benefitPlanCoverage);
            result.add(basicBenefitPlanCoverage);
            changeEmployeesCoverage(benefitUsers.get(s.getId()), basicBenefitPlanCoverage.getId());
          }

          if (s.getId().startsWith(BENEFIT_NEW_COVERAGE)) {
            final BenefitCoverages addNewBenefitCoverage =
                benefitCoveragesMapper.createFromBenefitPlanCoverageDto(s);
            addNewBenefitCoverage.setBenefitPlanId(planId);
            final BenefitCoverages newBenefitCoverage =
                benefitCoveragesRepository.save(addNewBenefitCoverage);
            final BenefitPlanCoverage benefitPlanCoverage =
                benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndCoverage(
                    s, newBenefitCoverage);
            benefitPlanCoverage.setBenefitPlanId(planId);
            final BenefitPlanCoverage newBenefitPlanCoverage =
                benefitPlanCoverageRepository.save(benefitPlanCoverage);
            changeEmployeesCoverage(benefitUsers.get(s.getId()), newBenefitPlanCoverage.getId());
            s.setId(newBenefitCoverage.getId());
            result.add(newBenefitPlanCoverage);
          }
        });
    return result;
  }

  private void updateBenefitPlanUser(
      final NewBenefitPlanWrapperDto data,
      final String planId,
      final List<BenefitPlanCoverage> benefitPlanCoverageList) {

    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(planId));
    if (!data.getSelectedEmployees().isEmpty()) {
      final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList =
          data.getSelectedEmployees();
      updateBenefitPlanUsers(planId, benefitPlanUserCreateDtoList, benefitPlanCoverageList);
    }
    benefitPlanUserRepository.delete(benefitPlanUsers);
  }

  public BenefitPlan findBenefitPlanById(final String id) {
    return benefitPlanRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Benefit plan with id %s not found!", id), id, "benefit plan"));
  }

  public void save(final BenefitPlan benefitPlan) {
    benefitPlanRepository.save(benefitPlan);
  }

  public List<BenefitPlanTypeWithoutExpiredDto> getBenefitPlanTypesAndNum() {
    final List<BenefitPlanTypeDto> benefitPlanTypes =
        benefitPlanRepository.findPlanTypeAndNumOrderByTypeId();

    final Map<String, List<BenefitPlanTypeDto>> categoryMap = new HashMap<>();
    for (final BenefitPlanTypeDto benefitPlanTypeDto : benefitPlanTypes) {
      categoryMap
          .computeIfAbsent(benefitPlanTypeDto.getBenefitPlanType(), k -> generateList())
          .add(benefitPlanTypeDto);
    }

    final List<BenefitPlanTypeWithoutExpiredDto> result = new ArrayList<>();

    final List<String> orderKey = new ArrayList<>();
    orderKey.add(PlanType.MEDICAL.getValue());
    orderKey.add(PlanType.DENTAL.getValue());
    orderKey.add(PlanType.VISION.getValue());
    orderKey.add(PlanType.RETIREMENT.getValue());
    orderKey.add(PlanType.OTHER.getValue());

    for (final String key : orderKey) {
      int excludedNumber = 0;
      String benefitPlanTypeId = null;
      for (final BenefitPlanTypeDto benefitPlanTypeDto : categoryMap.get(key)) {
        benefitPlanTypeId = benefitPlanTypeDto.getBenefitPlanTypeId();
        if (benefitPlanTypeDto.getBenefitPlanEndDate() == null
            || DateUtil.toLocalDateTime(benefitPlanTypeDto.getBenefitPlanEndDate())
                    .compareTo(DateUtil.getLocalUtcTime().minusDays(1))
                <= 0) {
          excludedNumber++;
        }
      }

      if (benefitPlanTypeId != null) {
        result.add(
            new BenefitPlanTypeWithoutExpiredDto(
                benefitPlanTypeId, key, categoryMap.get(key).size() - excludedNumber));
      }
    }
    return result;
  }

  private List<BenefitPlanTypeDto> generateList() {
    return new ArrayList<>();
  }

  public List<BenefitPlanPreviewDto> getBenefitPlanPreview(final String planTypeId) {
    final List<BenefitPlan> benefitPlans =
        benefitPlanRepository.findByBenefitPlanTypeIdOrderByNameAsc(planTypeId);

    final List<BenefitPlanPreviewDto> benefitPlanPreviewDtos = new LinkedList<>();

    benefitPlans.forEach(
        benefitPlan -> {
          final Number eligibleNumber =
              benefitPlanUserRepository.getEligibleEmployeeNumber(benefitPlan.getId());
          final Number enrolledNumber =
              benefitPlanUserRepository.countByBenefitPlanIdAndConfirmedIsTrue(benefitPlan.getId());
          benefitPlanPreviewDtos.add(
              new BenefitPlanPreviewDto(
                  benefitPlan.getId(),
                  benefitPlan.getName(),
                  benefitPlan.getStartDate(),
                  benefitPlan.getEndDate(),
                  "",
                  eligibleNumber,
                  enrolledNumber));
        });

    return benefitPlanPreviewDtos;
  }

  public void updateBenefitPlanUsers(
      final String benefitPlanId,
      final List<BenefitPlanUserCreateDto> benefitPlanUsers,
      final List<BenefitPlanCoverage> benefitPlanCoverageList) {

    final Map<String, List<BenefitPlanCoverage>> coverageUserMap =
        benefitPlanCoverageList.stream().collect(Collectors.groupingBy(BenefitPlanCoverage::getId));

    // Retirement type
    if (CollectionUtils.isEmpty(benefitPlanCoverageList)) {
      final List<RetirementPayment> existRetirementPayments =
          retirementPaymentRepository.findAllByBenefitPlanAndUserIsNotNull(
              new BenefitPlan(benefitPlanId));
      retirementPaymentRepository.delete(existRetirementPayments);
      retirementPaymentRepository.saveAll(
          benefitPlanUsers.stream()
              .map(
                  saveBenefitPlanUserCreateDto ->
                      retirementPaymentMapper.convertToRetirementPayment(
                          saveBenefitPlanUserCreateDto,
                          new RetirementPayTypes(
                              saveBenefitPlanUserCreateDto.getEmployeeDeduction()),
                          new RetirementPayTypes(
                              saveBenefitPlanUserCreateDto.getCompanyContribution()),
                          new User(saveBenefitPlanUserCreateDto.getId()),
                          new BenefitPlan(benefitPlanId)))
              .collect(Collectors.toList()));
    } else {
      benefitPlanUsers.forEach(
          saveBenefitPlanUserCreateDto -> {
            final BenefitPlanCoverage benefitPlanCoverage =
                coverageUserMap.get(saveBenefitPlanUserCreateDto.getCoverage()).get(0);
            benefitPlanUserRepository.save(
                benefitPlanUserMapper.createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
                    saveBenefitPlanUserCreateDto, benefitPlanId, benefitPlanCoverage, true, true));
          });
    }
  }

  public BenefitSummaryDto getBenefitSummary(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId);
    final Long benefitNumber = (long) benefitPlanUsers.size();
    BigDecimal benefitCost = BigDecimal.valueOf(0);
    final List<BenefitPlanDependentUserDto> dependentUsers = new ArrayList<>();
    final List<String> dependentUserIds = new ArrayList<>();
    for (final BenefitPlanUser benefitPlanUser : benefitPlanUsers) {
      if (!benefitPlanUser
          .getBenefitPlan()
          .getBenefitPlanType()
          .getName()
          .equals(BenefitPlanType.PlanType.RETIREMENT.getValue())) {
        benefitCost = benefitCost.add(benefitPlanUser.getBenefitPlanCoverage().getEmployeeCost());
        final Set<BenefitPlanDependent> benefitPlanDependents =
            benefitPlanUser.getBenefitPlanDependents();
        for (final BenefitPlanDependent benefitPlanDependent : benefitPlanDependents) {
          if (!dependentUserIds.contains(benefitPlanDependent.getId())) {
            final BenefitPlanDependentUserDto benefitPlanDependentUserDto =
                benefitPlanDependentMapper.convertToBenefitPlanDependentUser(benefitPlanDependent);
            dependentUsers.add(benefitPlanDependentUserDto);
            dependentUserIds.add(benefitPlanDependent.getId());
          }
        }
      }
    }
    return myBenefitsMapper.convertToBenefitSummaryDto(
        benefitNumber, benefitCost, (long) dependentUsers.size(), dependentUsers);
  }

  public List<UserBenefitPlanDto> getUserBenefitPlans(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId);
    final List<RetirementPayment> retirementPayments =
    retirementPaymentRepository.findAllByUserId(userId);

    final List<UserBenefitPlanDto> retirementUserDtos =
    retirementPayments.stream()
            .map(retirementPaymentMapper::convertForm).collect(Collectors.toList());

    final List<UserBenefitPlanDto> commonUserDtos =
    benefitPlanUsers.stream()
        .map(benefitPlanUserMapper::convertFrom).collect(Collectors.toList());

    return Stream.concat(retirementUserDtos.stream(), commonUserDtos.stream()).collect(Collectors.toList());
  }

  public List<UserBenefitPlanDto> getUserAvailableBenefitPlans(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findAllByUserId(userId);

    return benefitPlanUsers.stream()
        .map(benefitPlanUserMapper::convertFrom)
        .collect(Collectors.toList());
  }

  public void updateUserBenefitPlanEnrollmentInfo(
      final String userId, final List<SelectedEnrollmentInfoDto> selectedBenefitPlanInfo) {
    selectedBenefitPlanInfo.forEach(
        s -> {
          if (s.getBenefitPlanType().equals(BenefitPlanType.PlanType.OTHER.getValue())) {
            if (s.getPlanId() == null) {
              final BenefitPlan benefitPlan =
                  benefitPlanRepository.findBenefitPlanByName(s.getOtherTypePlanTitle());
              final BenefitPlanUser originBenefitPlanUser =
                  getBenefitPlanUserByUserIdAndBenefitPlanId(userId, benefitPlan.getId());
              originBenefitPlanUser.setEnrolled(false);
              benefitPlanUserRepository.save(originBenefitPlanUser);
              return;
            }
            final BenefitPlan benefitPlan =
                benefitPlanRepository.findBenefitPlanById(s.getPlanId());
            enrollBenefitPlanUser(userId, benefitPlan, s);
            return;
          }

          if (s.getPlanId() == null) {
            // find all this type of benefitPlan of user's company and set enrolled as false
            // and clear all the data that related to this benefit plan
            final BenefitPlanType benefitPlanType =
                benefitPlanTypeRepository.findByName(s.getBenefitPlanType());

            clearBenefitPlansEnrollmentInfoByPlanType(benefitPlanType, userId);

          } else {
            // selected all this type of benefitPlan of user's company
            updateBenefitPlansEnrollmentInfoByPlanType(s, userId);
          }
        });
  }

  // clear all the enrollmentInfo relate to this type of
  // benefit plan when selected enroll planId is null
  private void clearBenefitPlansEnrollmentInfoByPlanType(
      final BenefitPlanType benefitPlanType, final String userId) {
    final List<BenefitPlan> benefitPlans =
        benefitPlanRepository.findByBenefitPlanTypeIdOrderByNameAsc(benefitPlanType.getId());

    benefitPlans.forEach(
        benefitPlan -> {
          final BenefitPlanUser originBenefitPlanUser =
              getBenefitPlanUserByUserIdAndBenefitPlanId(userId, benefitPlan.getId());

          originBenefitPlanUser.setEnrolled(false);
          originBenefitPlanUser.setBenefitPlanCoverage(null);
          final List<BenefitDependentRecord> records =
              benefitPlanDependentRepository.findByBenefitPlansUsersId(
                  originBenefitPlanUser.getId());
          benefitPlanDependentRepository.delete(records);
          benefitPlanUserRepository.save(originBenefitPlanUser);
        });
  }

  private void enrollBenefitPlanUser(
      final String userId,
      final BenefitPlan benefitPlan,
      final SelectedEnrollmentInfoDto selectedEnrollmentInfoDto) {
    final BenefitPlanUser originBenefitPlanUser =
        getBenefitPlanUserByUserIdAndBenefitPlanId(userId, benefitPlan.getId());
    originBenefitPlanUser.setEnrolled(true);
    if (selectedEnrollmentInfoDto.getCoverageOptionId() == null) {
      originBenefitPlanUser.setBenefitPlanCoverage(null);
    } else {
      originBenefitPlanUser.setBenefitPlanCoverage(
          getBenefitPlanCoverageById(selectedEnrollmentInfoDto.getCoverageOptionId()));
    }

    if (selectedEnrollmentInfoDto.getSelectedDependents() != null) {
      updateSelectedDependentsByBenefitPlanUser(
          selectedEnrollmentInfoDto.getSelectedDependents(), originBenefitPlanUser.getId());
    }
    benefitPlanUserRepository.save(originBenefitPlanUser);
  }

  // update benefit enrollment info based on this benefitPlanDto
  private void updateBenefitPlansEnrollmentInfoByPlanType(
      final SelectedEnrollmentInfoDto selectedEnrollmentInfoDto, final String userId) {
    benefitPlanRepository
        .findByBenefitPlanTypeIdOrderByNameAsc(
            benefitPlanTypeRepository
                .findByName(selectedEnrollmentInfoDto.getBenefitPlanType())
                .getId())
        .forEach(
            benefitPlan -> {
              if (benefitPlan.getId().equals(selectedEnrollmentInfoDto.getPlanId())) {
                // find which benefitPlan under this type is selected and
                // update the information

                enrollBenefitPlanUser(userId, benefitPlan, selectedEnrollmentInfoDto);
                // update related information under one type finished
              } else {

                // find which benefitPlan under this type isn't selected
                // and update the information
                final BenefitPlanUser originBenefitPlanUser =
                    getBenefitPlanUserByUserIdAndBenefitPlanId(userId, benefitPlan.getId());
                originBenefitPlanUser.setEnrolled(false);
                originBenefitPlanUser.setBenefitPlanCoverage(null);
                benefitPlanUserRepository.save(originBenefitPlanUser);
              }
            });
  }

  // update selected dependents correspond to the benefitPlanUser record
  private void updateSelectedDependentsByBenefitPlanUser(
      final List<BenefitPlanUserDto> selectedDependents, final String benefitPlanUserId) {

    final List<BenefitDependentRecord> prevBenefitDependentRecords =
        benefitPlanDependentRepository.findByBenefitPlansUsersId(benefitPlanUserId);

    final List<String> oldDependentIds =
        prevBenefitDependentRecords.stream()
            .map(BenefitDependentRecord::getUserDependentsId)
            .collect(Collectors.toList());

    final List<String> newDependentIds =
        selectedDependents.stream().map(BenefitPlanUserDto::getId).collect(Collectors.toList());

    // map all the old Records to find records which has been removed form the new Records
    oldDependentIds.forEach(
        oldDependentId -> {
          if (!newDependentIds.contains(oldDependentId)) {
            final BenefitDependentRecord oldBenefitDependentRecord =
                benefitPlanDependentRepository
                    .findByBenefitPlansUsersIdAndUserDependentsId(benefitPlanUserId, oldDependentId)
                    .orElseThrow(
                        () ->
                            new ResourceNotFoundException(
                                String.format(
                                    "Benefit dependent with id %s not found!", oldDependentId),
                                oldDependentId,
                                "benefit dependent"));
            benefitPlanDependentRepository.delete(oldBenefitDependentRecord);
          }
        });

    // map all the new records to find record which haven't been added
    newDependentIds.forEach(
        newDependentId -> {
          if (!oldDependentIds.contains(newDependentId)) {
            final BenefitDependentRecord newBenefitDependentRecord =
                new BenefitDependentRecord(benefitPlanUserId, newDependentId);
            benefitPlanDependentRepository.save(newBenefitDependentRecord);
          }
        });
  }

  public void deleteBenefitPlanByPlanId(final String benefitPlanId) {
    benefitPlanRepository.delete(benefitPlanId);
    final List<String> benefitCoverageIds =
        benefitCoveragesRepository.findAllByBenefitPlanId(benefitPlanId).stream()
            .map(BenefitCoverages::getId)
            .collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(benefitCoverageIds)) {
      benefitCoveragesRepository.deleteInBatch(benefitCoverageIds);
    }
  }

  public BenefitPlanUpdateDto getBenefitPlanByPlanId(final String planId) {
    final BenefitPlan benefitPlan = benefitPlanRepository.findBenefitPlanById(planId);

    if (RETIREMENT_TYPE_NAME.equals(benefitPlan.getBenefitPlanType().getName())) {
      final String percentageName = PayTypes.PERCENTAGE_OF_GROSS_PAY.getValue();
      final String retirementTypeName =
          retirementPlanTypeRepository.findByBenefitPlan(benefitPlan).getRetirementType().getName();
      final List<RetirementPayment> retirementUserPayments =
          retirementPaymentRepository.findAllByBenefitPlanAndUserIsNotNull(benefitPlan);

      final RetirementPayment planRetirementPayment =
          retirementPaymentRepository.findByBenefitPlanAndUserIsNull(benefitPlan);
      final RetirementDto retirementDto =
          RetirementDto.builder()
              .isEmployeePercentage(
                  planRetirementPayment.getEmployeeDeduction().getName().equals(percentageName))
              .isCompanyPercentage(
                  planRetirementPayment.getCompanyContribution().getName().equals(percentageName))
              .isDeductionLimit(planRetirementPayment.getLimitStandard())
              .retirementTypeName(retirementTypeName)
              .build();
      final BenefitPlanDto benefitPlanDto =
          retirementPaymentMapper.convertToBenefitPlanByRetirement(
              benefitPlan, planRetirementPayment, retirementDto);

      final List<BenefitPlanUserDto> benefitPlanUserDtos =
          retirementUserPayments.stream()
              .map(
                  retirementPayment ->
                      retirementPaymentMapper.convertToBenefitPlanUserDto(
                          retirementPayment.getUser(),
                          retirementPaymentMapper.convertToRetirementPayment(retirementPayment)))
              .collect(Collectors.toList());

      return benefitPlanMapper.convertToRBenefitRetirementDto(benefitPlanDto, benefitPlanUserDtos);

    } else {
      final List<BenefitPlanCoverage> benefitPlanCoverage =
          benefitPlanCoverageRepository.findAllByBenefitPlanId(planId);
      final List<BenefitPlanUser> benefitPlanUsers =
          benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(planId));
      final RetirementPlanType retirementPlanType =
          retirementPlanTypeRepository.findByBenefitPlan(new BenefitPlan(planId));
      final BenefitPlanUpdateDto benefitPlanUpdateDto =
          benefitPlanMapper.convertToOldBenefitPlanDto(
              benefitPlan, benefitPlanCoverage, benefitPlanUsers, retirementPlanType);
      benefitPlanUpdateDto
          .getBenefitPlanCoverages()
          .forEach(
              benefitPlanCoverageDto ->
                  benefitPlanCoverageDto.setCoverageName(
                      getBenefitCoveragesById(benefitPlanCoverageDto.getCoverageId()).getName()));
      return benefitPlanUpdateDto;
    }
  }

  public void saveBenefitPlanDocuments(
      final String benefitPlanId, final List<MultipartFile> files) {
    final BenefitPlan benefitPlan = findBenefitPlanById(benefitPlanId);
    files.forEach(
        file -> {
          final String path = awsHelper.uploadFile(file, AccessType.PRIVATE);

          final String title =
              StringUtils.isNotBlank(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
          final String fileName = title.substring(0, title.lastIndexOf('.'));
          final BenefitPlanDocument document = new BenefitPlanDocument(fileName, path);
          benefitPlan.addBenefitPlanDocument(document);
        });
    save(benefitPlan);
  }

  public void confirmBenefitPlanEnrollment(
      final String userId, final List<SelectedEnrollmentInfoDto> selectedBenefitPlanInfo) {
    updateUserBenefitPlanEnrollmentInfo(userId, selectedBenefitPlanInfo);
    selectedBenefitPlanInfo.forEach(
        s -> {
          final BenefitPlanUser confirmedBenefitPlanUser =
              getBenefitPlanUserByUserIdAndBenefitPlanId(userId, s.getPlanId());
          confirmedBenefitPlanUser.setConfirmed(true);
          benefitPlanUserRepository.save(confirmedBenefitPlanUser);
        });
    userBenefitsSettingService.saveUserBenefitsSettingEffectYear(
        userId, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
  }

  public boolean isConfirmed(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndConfirmedIsTrue(userId);
    return !benefitPlanUsers.isEmpty();
  }

  public void updateBenefitPlanEmployees(
      final List<BenefitPlanUserCreateDto> employees, final String benefitPlanId) {
    final boolean isRetirement =
        RETIREMENT_TYPE_NAME.equals(
            benefitPlanRepository
                .findBenefitPlanById(benefitPlanId)
                .getBenefitPlanType()
                .getName());

    if (isRetirement) {
      final BenefitPlan benefitPlan = new BenefitPlan(benefitPlanId);
      final List<RetirementPayment> existPayments =
          retirementPaymentRepository.findAllByBenefitPlanAndUserIsNotNull(benefitPlan);
      retirementPaymentRepository.delete(existPayments);

      retirementPaymentRepository.saveAll(
          employees.stream()
              .map(
                  benefitPlanUserCreateDto ->
                      retirementPaymentMapper.convertToRetirementPayment(
                          benefitPlanUserCreateDto,
                          new RetirementPayTypes(benefitPlanUserCreateDto.getEmployeeDeduction()),
                          new RetirementPayTypes(benefitPlanUserCreateDto.getCompanyContribution()),
                          new User(benefitPlanUserCreateDto.getId()),
                          benefitPlan))
              .collect(Collectors.toList()));

    } else {
      final List<String> existIds =
          benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId).stream()
              .map(BenefitPlanUser::getId)
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(existIds)) {
        benefitPlanUserRepository.deleteInBatch(existIds);
      }
      benefitPlanUserRepository.saveAll(
          employees.stream()
              .map(
                  benefitPlanUserCreateDto -> {
                    final BenefitPlanCoverage benefitPlanCoverage = new BenefitPlanCoverage();
                    benefitPlanCoverage.setId(benefitPlanUserCreateDto.getCoverage());
                    return benefitPlanUserMapper.createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
                        benefitPlanUserCreateDto, benefitPlanId, benefitPlanCoverage, true, true);
                  })
              .collect(Collectors.toList()));
    }
  }

  public List<BenefitPlanUserDto> findAllEmployeesForBenefitPlan(final String benefitPlanId) {
    final List<User> policyEmployees = userRepository.findAllActiveUsers();

    final boolean isRetirement =
        RETIREMENT_TYPE_NAME.equals(
            benefitPlanRepository
                .findBenefitPlanById(benefitPlanId)
                .getBenefitPlanType()
                .getName());

    final List<JobUserDto> allJobUsers =
        policyEmployees.stream()
            .map(
                user -> {
                  final JobUser employeeWithJob = jobUserService.findJobUserByUser(user);
                  final String name = userService.getUserNameInUsers(user, policyEmployees);
                  return new JobUserDto(user, employeeWithJob, name);
                })
            .collect(Collectors.toList());

    final List<BenefitPlanUserDto> allUsers =
        allJobUsers.stream()
            .map(jobUserMapper::covertToBenefitPlanUserDto)
            .collect(Collectors.toList());

    if (isRetirement) {
      final BenefitPlan benefitPlan = new BenefitPlan(benefitPlanId);
      final RetirementPayment planRetirementPayment =
          retirementPaymentRepository.findByBenefitPlanAndUserIsNull(benefitPlan);
      final RetirementUserDto planRetirementUserDto =
          retirementPaymentMapper.convertToRetirementPayment(planRetirementPayment);

      final List<BenefitPlanUserDto> selectUsers =
          retirementPaymentRepository.findAllByBenefitPlanAndUserIsNotNull(benefitPlan).stream()
              .map(
                  retirementPayment -> {
                    final RetirementUserDto retirementUserDto =
                        retirementPaymentMapper.convertToRetirementPayment(retirementPayment);
                    return retirementPaymentMapper.convertToBenefitPlanUserDto(
                        retirementPayment.getUser(), retirementUserDto);
                  })
              .collect(Collectors.toList());

      allUsers.forEach(
          user -> {
            final List<BenefitPlanUserDto> benefitPlanUserDtos =
                selectUsers.stream()
                    .filter(selectUser -> selectUser.getId().equals(user.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(benefitPlanUserDtos)) {
              user.setRetirementUserDto(planRetirementUserDto);
            } else {
              user.setCoverageId(RETIREMENT_TYPE_NAME);
              user.setRetirementUserDto(benefitPlanUserDtos.get(0).getRetirementUserDto());
            }
          });
    } else {
      final List<BenefitPlanUserDto> selectUsers =
          benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId).stream()
              .map(benefitPlanUserMapper::convertToBenefitPlanUserDto)
              .collect(Collectors.toList());
      selectUsers.forEach(
          s ->
              allUsers.forEach(
                  user -> {
                    if (user.getId().equals(s.getId())) {
                      user.setCoverageId(s.getCoverageId());
                    }
                  }));
    }
    return allUsers;
  }

  public List<BenefitCoveragesDto> findAllCoveragesByBenefitPlan(final String benefitPlanId) {
    return benefitPlanCoverageRepository.getBenefitPlanCoveragesByPlanId(benefitPlanId);
  }

  public BenefitPlanRelatedUserListDto findRelatedUsersByBenefitPlan(final String benefitPlanId) {
    final List<String> selectedUserIds = new ArrayList<>();
    final List<User> allUsers = userRepository.findAllActiveUsers();
    final List<BenefitPlanUser> benefitPlanUserList =
        benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId);

    final List<BenefitPlanUserDto> selectedUsers =
        benefitPlanUserList.stream()
            .filter(distinctByKey(b -> b.getUser().getId()))
            .map(
                benefitPlanUser -> {
                  selectedUserIds.add(benefitPlanUser.getUser().getId());
                  return benefitPlanUserMapper.convertToBenefitPlanUserDto(benefitPlanUser);
                })
            .collect(Collectors.toList());

    final List<BenefitPlanUserDto> unselectedUsers =
        allUsers.stream()
            .filter(user -> !selectedUserIds.contains(user.getId()))
            .map(userMapper::covertToBenefitPlanUserDto)
            .collect(Collectors.toList());
    return new BenefitPlanRelatedUserListDto(unselectedUsers, selectedUsers);
  }

  public BenefitPlanCoveragesDto findCoveragesByBenefitPlanId() {
    final List<BenefitCoverages> coverages =
        benefitCoveragesRepository.findAllByBenefitPlanIdIsNullOrderByRefIdAsc();
    final List<BenefitCoveragesDto> benefitCoveragesDtos =
        coverages.stream()
            .map(benefitCoveragesMapper::convertToBenefitCoveragesDto)
            .collect(Collectors.toList());
    return new BenefitPlanCoveragesDto(benefitCoveragesDtos);
  }

  public BenefitPlanReportDto findBenefitPlanReport(
      final String typeName, final BenefitReportParamDto benefitReportParamDto) {
    final String coverageId = benefitReportParamDto.getCoverageId();
    List<String> benefitPlanIds = new ArrayList<>();
    List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();

    final BenefitPlanTypeEnum typeEnum =
        BenefitPlanTypeEnum.getEnumByDesc(benefitReportParamDto.getPlanId());
    benefitPlanIds = getSearchBenefitPlanIds(typeEnum, benefitPlanIds, typeName);

    if (BenefitPlanTypeEnum.OTHER.equals(typeEnum)) {
      benefitPlanIds = Collections.singletonList(benefitReportParamDto.getPlanId());
      enrollmentBreakdownDtos =
          findEnrollmentBreakdownToExport(benefitReportParamDto, benefitPlanIds);
    }

    if (!CollectionUtils.isEmpty(benefitPlanIds) && !BenefitPlanTypeEnum.OTHER.equals(typeEnum)) {
      enrollmentBreakdownDtos = getEnrollmentBreakdownDtosByCoverage(coverageId, benefitPlanIds);

      int i = 1;
      for (final EnrollmentBreakdownDto enrollmentBreakdownDto : enrollmentBreakdownDtos) {
        enrollmentBreakdownDto.setNumber(i);
        i++;
      }
    }

    final List<BenefitPlanReportSummaryDto> benefitPlanReportSummaryDtos =
        findBenefitPlanReportSummary(benefitReportParamDto, benefitPlanIds);
    final List<BenefitReportPlansDto> benefitReportPlansDtos =
        findBenefitPlansByPlanTypeName(typeName);
    final List<BenefitReportCoveragesDto> benefitReportCoveragesDtos =
        findBenefitCoveragesByPlanIds(benefitPlanIds);
    return BenefitPlanReportDto.builder()
        .benefitPlanReportSummaryDtos(benefitPlanReportSummaryDtos)
        .benefitReportCoveragesDtos(benefitReportCoveragesDtos)
        .benefitReportPlansDtos(benefitReportPlansDtos)
        .enrollmentBreakdownDtos(enrollmentBreakdownDtos)
        .build();
  }

  private List<EnrollmentBreakdownDto> getEnrollmentBreakdownDtosByCoverage(
      final String coverageId, final List<String> benefitPlanIds) {
    final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos;

    if (StringUtils.isNotEmpty(coverageId) && !DEFAULT_ID.equals(coverageId)) {
      final String coverageName = benefitCoveragesRepository.findById(coverageId).get().getName();
      final List<String> coverageIds =
          benefitCoveragesRepository.getCoverageIdsByNameAndPlan(coverageName, benefitPlanIds);
      if (!CollectionUtils.isEmpty(coverageIds)) {
        enrollmentBreakdownDtos =
            benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds, coverageIds);
      } else {
        enrollmentBreakdownDtos =
            benefitPlanRepository.getEnrollmentBreakdown(
                benefitPlanIds, Collections.singletonList(coverageId));
      }
    } else {
      enrollmentBreakdownDtos =
          benefitPlanRepository.getEnrollmentBreakdownWhenPlanIdIsEmpty(benefitPlanIds);
    }
    return enrollmentBreakdownDtos;
  }

  private List<String> getSearchBenefitPlanIds(
      final BenefitPlanTypeEnum typeEnum, List<String> benefitPlanIds, final String typeName) {
    switch (typeEnum) {
      case ACTIVE:
        benefitPlanIds = benefitPlanRepository.getActiveBenefitPlanIds(typeName);
        break;
      case EXPIRED:
        benefitPlanIds = benefitPlanRepository.getExpiredBenefitPlanIds(typeName);
        break;
      case STARTING:
        benefitPlanIds = benefitPlanRepository.getStartingBenefitPlanIds(typeName);
        break;
      default:
        break;
    }
    return benefitPlanIds;
  }

  public List<BenefitPlanReportSummaryDto> findBenefitPlanReportSummary(
      final BenefitReportParamDto benefitReportParamDto, final List<String> benefitPlanIds) {
    BigDecimal employeesEnrolledNum = BigDecimal.valueOf(0);
    BigDecimal companyCost = BigDecimal.valueOf(0);
    BigDecimal employeeCost = BigDecimal.valueOf(0);
    if (!benefitPlanIds.isEmpty()) {
      final BigDecimal companyCostResult;
      final BigDecimal employeeCostResult;
      if (benefitReportParamDto.getCoverageId().isEmpty()
          || DEFAULT_ID.equals(benefitReportParamDto.getCoverageId())) {
        companyCostResult = benefitPlanCoverageRepository.getCompanyCost(benefitPlanIds);
        employeeCostResult = benefitPlanCoverageRepository.getEmployeeCost(benefitPlanIds);
        employeesEnrolledNum =
            BigDecimal.valueOf(
                benefitPlanUserRepository.getEmployeesEnrolledNumber(benefitPlanIds));
      } else {
        companyCostResult =
            benefitPlanCoverageRepository.getCompanyCostByCoverageId(
                benefitPlanIds, benefitReportParamDto.getCoverageId());
        employeeCostResult =
            benefitPlanCoverageRepository.getEmployeeCostByCoverageId(
                benefitPlanIds, benefitReportParamDto.getCoverageId());
        employeesEnrolledNum =
            BigDecimal.valueOf(
                benefitPlanUserRepository.getEmployeesEnrolledNumberByCoverageId(
                    benefitPlanIds, benefitReportParamDto.getCoverageId()));
      }
      companyCost = (companyCostResult == null ? BigDecimal.valueOf(0) : companyCostResult);
      employeeCost = (employeeCostResult == null ? BigDecimal.valueOf(0) : employeeCostResult);
    }
    final List<BenefitPlanReportSummaryDto> benefitPlanReportSummaryDtos = new ArrayList<>();
    final BenefitPlanReportSummaryDto employeesEnrolled =
        benefitPlanReportMapper.covertToBenefitPlanReportSummaryDto(
            "a", "Employees Enrolled", employeesEnrolledNum, "");

    final BenefitPlanReportSummaryDto companyTotalCost =
        benefitPlanReportMapper.covertToBenefitPlanReportSummaryDto(
            "b", "Company Cost", companyCost, "per month");

    final BenefitPlanReportSummaryDto employeeTotalCost =
        benefitPlanReportMapper.covertToBenefitPlanReportSummaryDto(
            "c", "Employee Cost", employeeCost, "per month");

    benefitPlanReportSummaryDtos.add(employeesEnrolled);
    benefitPlanReportSummaryDtos.add(companyTotalCost);
    benefitPlanReportSummaryDtos.add(employeeTotalCost);
    return benefitPlanReportSummaryDtos;
  }

  public List<BenefitReportPlansDto> findBenefitPlansByPlanTypeName(final String typeName) {
    final List<BenefitReportPlansDto> benefitReportPlansDtos = new ArrayList<>();
    final List<BenefitReportPlansPojo> benefitReportPlansPojos =
        benefitPlanRepository.getBenefitPlans(typeName);
    for (final BenefitReportPlansPojo benefitReportPlansPojo : benefitReportPlansPojos) {
      final BenefitReportPlansDto benefitReportPlansDto = new BenefitReportPlansDto();
      BeanUtils.copyProperties(benefitReportPlansPojo, benefitReportPlansDto);
      benefitReportPlansDtos.add(benefitReportPlansDto);
    }
    return benefitReportPlansDtos;
  }

  public List<BenefitReportCoveragesDto> findBenefitCoveragesByPlanIds(
      final List<String> benefitPlanIds) {
    List<BenefitReportCoveragesDto> benefitReportCoveragesDtos = new ArrayList<>();
    if (!benefitPlanIds.isEmpty()) {
      benefitReportCoveragesDtos =
          benefitPlanCoverageRepository.getBenefitReportCoverages(benefitPlanIds).stream()
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toCollection(
                          () ->
                              new TreeSet<>(
                                  Comparator.comparing(BenefitReportCoveragesDto::getName))),
                      ArrayList::new));
    }
    final BenefitReportCoveragesDto benefitReportCoveragesDto =
        benefitPlanReportMapper.covertToBenefitReportCoveragesDto(DEFAULT_ID, "All Coverage Types");
    benefitReportCoveragesDtos.add(benefitReportCoveragesDto);
    return benefitReportCoveragesDtos;
  }

  public List<EnrollmentBreakdownDto> findEnrollmentBreakdownToExport(
      final BenefitReportParamDto benefitReportParamDto, final List<String> benefitPlanIds) {
    final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos;
    if (benefitReportParamDto.getCoverageId().isEmpty()
        || DEFAULT_ID.equals(benefitReportParamDto.getCoverageId())) {
      enrollmentBreakdownDtos = benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds);
    } else {
      enrollmentBreakdownDtos =
          benefitPlanRepository.getEnrollmentBreakdown(
              benefitPlanIds, Collections.singletonList(benefitReportParamDto.getCoverageId()));
    }
    int i = 1;
    for (final EnrollmentBreakdownDto enrollmentBreakdownDto : enrollmentBreakdownDtos) {
      enrollmentBreakdownDto.setNumber(i);
      i++;
    }
    return enrollmentBreakdownDtos;
  }

  public Page<EnrollmentBreakdownDto> findEnrollmentBreakdown(
      final EnrollmentBreakdownSearchCondition enrollmentBreakdownSearchCondition,
      final String planTypeName,
      final BenefitReportParamDto benefitReportParamDto) {
    final String coverageId = enrollmentBreakdownSearchCondition.getCoverageId();
    List<String> benefitPlanIds = new ArrayList<>();
    final Pageable paramPageable = getPageable(enrollmentBreakdownSearchCondition);

    final BenefitPlanTypeEnum typeEnum =
        BenefitPlanTypeEnum.getEnumByDesc(benefitReportParamDto.getPlanId());
    benefitPlanIds = getSearchBenefitPlanIds(typeEnum, benefitPlanIds, planTypeName);

    if (typeEnum.equals(BenefitPlanTypeEnum.OTHER)) {
      benefitPlanIds = Collections.singletonList(benefitReportParamDto.getPlanId());
      return findEnrollmentBreakdownByCondition(
          paramPageable, benefitReportParamDto, benefitPlanIds);
    }
    return getEnrollmentBreakdownPageByCoverage(benefitPlanIds, paramPageable, coverageId);
  }

  private PageImpl getEnrollmentBreakdownPageByCoverage(
      final List<String> benefitPlanIds, final Pageable paramPageable, final String coverageId) {
    final Page<EnrollmentBreakdownPojo> enrollmentBreakdownDtoPage;
    List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();

    if (!CollectionUtils.isEmpty(benefitPlanIds)) {
      if (StringUtils.isNotEmpty(coverageId) && !DEFAULT_ID.equals(coverageId)) {
        final String coverageName = benefitCoveragesRepository.findById(coverageId).get().getName();
        final List<String> coverageIds =
            benefitCoveragesRepository.getCoverageIdsByNameAndPlan(coverageName, benefitPlanIds);
        if (!CollectionUtils.isEmpty(coverageIds)) {
          enrollmentBreakdownDtoPage =
              benefitPlanRepository.getEnrollmentBreakdownByConditionAndCoverageId(
                  benefitPlanIds, coverageIds, paramPageable);
        } else {
          enrollmentBreakdownDtoPage =
              benefitPlanRepository.getEnrollmentBreakdownByConditionAndCoverageId(
                  benefitPlanIds, Collections.singletonList(coverageId), paramPageable);
        }
      } else {
        enrollmentBreakdownDtoPage =
            benefitPlanRepository.getEnrollmentBreakdownByConditionAndPlanIdIsEmpty(
                benefitPlanIds, paramPageable);
      }
      enrollmentBreakdownDtos =
          findEnrollmentBreakdownContent(enrollmentBreakdownDtoPage.getContent());
      return new PageImpl<>(
          enrollmentBreakdownDtos,
          enrollmentBreakdownDtoPage.getPageable(),
          enrollmentBreakdownDtoPage.getTotalElements());
    }
    return new PageImpl<>(enrollmentBreakdownDtos, paramPageable, 0);
  }

  private Pageable getPageable(
      final EnrollmentBreakdownSearchCondition enrollmentBreakdownSearchCondition) {
    final String sortDirection =
        enrollmentBreakdownSearchCondition.getSortDirection().toUpperCase();

    final String sortValue = enrollmentBreakdownSearchCondition.getSortField().getSortValue();

    if (!sortValue.equals(EnrollmentBreakdownSearchCondition.SortField.NAME.getSortValue())) {
      final Sort.Order order = new Sort.Order(Sort.Direction.valueOf(sortDirection), sortValue);
      final Sort.Order orderName =
          new Sort.Order(
              Sort.Direction.ASC, EnrollmentBreakdownSearchCondition.SortField.NAME.getSortValue());

      final Sort sort = Sort.by(order, orderName);
      return PageRequest.of(
          enrollmentBreakdownSearchCondition.getPage(),
          enrollmentBreakdownSearchCondition.getSize(),
          sort);
    }
    return PageRequest.of(
        enrollmentBreakdownSearchCondition.getPage(),
        enrollmentBreakdownSearchCondition.getSize(),
        Sort.Direction.valueOf(sortDirection),
        sortValue);
  }

  private Page<EnrollmentBreakdownDto> findEnrollmentBreakdownByCondition(
      final Pageable paramPageable,
      final BenefitReportParamDto benefitReportParamDto,
      final List<String> benefitPlanIds) {
    final Page<EnrollmentBreakdownPojo> enrollmentBreakdownDtoPage;

    if (benefitReportParamDto.getCoverageId().isEmpty()
        || DEFAULT_ID.equals(benefitReportParamDto.getCoverageId())) {
      enrollmentBreakdownDtoPage =
          benefitPlanRepository.getEnrollmentBreakdownByCondition(benefitPlanIds, paramPageable);
    } else {
      enrollmentBreakdownDtoPage =
          benefitPlanRepository.getEnrollmentBreakdownByConditionAndCoverageId(
              benefitPlanIds,
              Collections.singletonList(benefitReportParamDto.getCoverageId()),
              paramPageable);
    }

    final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos =
        findEnrollmentBreakdownContent(enrollmentBreakdownDtoPage.getContent());

    return new PageImpl<>(
        enrollmentBreakdownDtos,
        enrollmentBreakdownDtoPage.getPageable(),
        enrollmentBreakdownDtoPage.getTotalElements());
  }

  List<EnrollmentBreakdownDto> findEnrollmentBreakdownContent(
      final List<EnrollmentBreakdownPojo> enrollmentBreakdownDtoPage) {
    int i = 1;
    final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
    for (final EnrollmentBreakdownPojo enrollmentBreakdownPojo : enrollmentBreakdownDtoPage) {
      final EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      BeanUtils.copyProperties(enrollmentBreakdownPojo, enrollmentBreakdownDto);
      enrollmentBreakdownDto.setNumber(i);
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);
      i++;
    }
    return enrollmentBreakdownDtos;
  }

  public BenefitCoverages getBenefitCoveragesById(final String id) {
    return benefitCoveragesRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Benefit coverage with id %s not found!", id),
                    id,
                    "benefit coverage"));
  }

  public BenefitPlanCoverage getBenefitPlanCoverageById(final String id) {
    return benefitPlanCoverageRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Benefit plan coverage with id %s not found!", id),
                    id,
                    "benefit plan coverage"));
  }

  public BenefitPlanUser getBenefitPlanUserByUserIdAndBenefitPlanId(
      final String userId, final String benefitPlanId) {
    return benefitPlanUserRepository
        .findByUserIdAndBenefitPlanId(userId, benefitPlanId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Benefit plan user with id %s not found!", userId),
                    userId,
                    "benefit plan user"));
  }

  public Page<BenefitPlanPreviewDto> findBenefitPlans(
      final String planTypeId,
      final boolean expired,
      final BenefitPlanSearchCondition benefitPlanSearchCondition) {
    final Pageable paramPageable = getBenefitPlanPageable(benefitPlanSearchCondition);
    final Page<BenefitPlanPreviewPojo> benefitPlanPreviewDtoPage;
    final Optional<BenefitPlanType> benefitPlanTypeOptional =
        benefitPlanTypeRepository.findById(planTypeId);
    final String planTypeName =
        benefitPlanTypeOptional.isPresent() ? benefitPlanTypeOptional.get().getName() : "";
    final boolean isRetirement = planTypeName.equals(RETIREMENT_TYPE_NAME);

    if (expired) {
      benefitPlanPreviewDtoPage =
          benefitPlanRepository.getBenefitPlanList(planTypeId, paramPageable);
    } else {
      benefitPlanPreviewDtoPage =
          benefitPlanRepository.getBenefitPlanListWithOutExpired(planTypeId, paramPageable);
    }
    final List<BenefitPlanPreviewDto> benefitPlanPreviewDtos =
        findBenefitPlanContent(benefitPlanPreviewDtoPage.getContent(), isRetirement);
    return new PageImpl<>(
        benefitPlanPreviewDtos,
        benefitPlanPreviewDtoPage.getPageable(),
        benefitPlanPreviewDtoPage.getTotalElements());
  }

  private List<BenefitPlanPreviewDto> findBenefitPlanContent(
      final List<BenefitPlanPreviewPojo> benefitPlanPreviewPojos, final boolean isRetirement) {
    final List<BenefitPlanPreviewDto> benefitPlanPreviewDtos = new ArrayList<>();
    for (final BenefitPlanPreviewPojo benefitPlanPreviewPojo : benefitPlanPreviewPojos) {
      final BenefitPlanPreviewDto benefitPlanPreviewDto = new BenefitPlanPreviewDto();
      BeanUtils.copyProperties(benefitPlanPreviewPojo, benefitPlanPreviewDto);
      benefitPlanPreviewDto.setEnrolledNumber(
          isRetirement
              ? retirementPaymentRepository.countByBenefitPlanIdAndUserIsNotNull(
                  benefitPlanPreviewPojo.getBenefitPlanId())
              : benefitPlanUserRepository.countByBenefitPlanIdAndEnrolledIsTrue(
                  benefitPlanPreviewPojo.getBenefitPlanId()));
      benefitPlanPreviewDtos.add(benefitPlanPreviewDto);
    }
    return benefitPlanPreviewDtos;
  }

  private Pageable getBenefitPlanPageable(
      final BenefitPlanSearchCondition benefitPlanSearchCondition) {
    final String sortDirection = benefitPlanSearchCondition.getSortDirection().toUpperCase();

    final String sortValue = benefitPlanSearchCondition.getSortField().getSortValue();
    return PageRequest.of(
        benefitPlanSearchCondition.getPage(),
        benefitPlanSearchCondition.getSize(),
        Sort.Direction.valueOf(sortDirection),
        sortValue);
  }

  public List<BenefitPlanDto> findAllPlans() {
    final List<BenefitPlan> benefitPlans = benefitPlanRepository.findAll();
    return benefitPlans.stream()
        .map(benefitPlanMapper::convertToBenefitPlanDto)
        .collect(Collectors.toList());
  }

  public List<BenefitCoverages> findAllByBenefitPlanIdIsNullOrderByRefIdAsc() {
    return benefitCoveragesRepository.findAllByBenefitPlanIdIsNullOrderByRefIdAsc();
  }
}
