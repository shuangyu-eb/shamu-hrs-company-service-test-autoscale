package shamu.company.benefit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanRelatedUserListDto;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.dto.BenefitPlanUpdateDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.dto.BenefitSummaryDto;
import shamu.company.benefit.dto.NewBenefitPlanWrapperDto;
import shamu.company.benefit.dto.SelectedEnrollmentInfoDto;
import shamu.company.benefit.dto.UserBenefitPlanDto;
import shamu.company.benefit.entity.BenefitDependentRecord;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.BenefitPlanDocument;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.benefit.entity.mapper.BenefitPlanCoverageMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanUserMapper;
import shamu.company.benefit.entity.mapper.MyBenefitsMapper;
import shamu.company.benefit.entity.mapper.RetirementPlanTypeMapper;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanDependentRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.repository.RetirementPlanTypeRepository;
import shamu.company.benefit.repository.UserDependentsRepository;
import shamu.company.common.exception.AwsException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.helpers.s3.AccessType;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserBenefitsSettingService;

@Service
public class BenefitPlanService {

  private final BenefitPlanRepository benefitPlanRepository;

  private final BenefitPlanUserRepository benefitPlanUserRepository;

  private final BenefitPlanCoverageRepository benefitPlanCoverageRepository;

  private final RetirementPlanTypeRepository retirementPlanTypeRepository;

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  private final BenefitPlanCoverageMapper benefitPlanCoverageMapper;

  private final BenefitPlanUserMapper benefitPlanUserMapper;

  private final RetirementPlanTypeMapper retirementPlanTypeMapper;

  private final BenefitPlanMapper benefitPlanMapper;

  private final MyBenefitsMapper myBenefitsMapper;

  private final UserMapper userMapper;

  private final UserRepository userRepository;

  private final UserDependentsRepository userDependentsRepository;

  private final BenefitPlanDependentRepository benefitPlanDependentRepository;

  private final AwsHelper awsHelper;

  private final UserBenefitsSettingService userBenefitsSettingService;

  public BenefitPlanService(
      final BenefitPlanRepository benefitPlanRepository,
      final BenefitPlanUserRepository benefitPlanUserRepository,
      final BenefitPlanCoverageRepository benefitPlanCoverageRepository,
      final RetirementPlanTypeRepository retirementPlanTypeRepository,
      final BenefitPlanTypeRepository benefitPlanTypeRepository,
      final BenefitPlanCoverageMapper benefitPlanCoverageMapper,
      final RetirementPlanTypeMapper retirementPlanTypeMapper,
      final BenefitPlanUserMapper benefitPlanUserMapper,
      final BenefitPlanMapper benefitPlanMapper,
      final MyBenefitsMapper myBenefitsMapper,
      final UserMapper userMapper,
      final UserRepository userRepository,
      final UserDependentsRepository userDependentsRepository,
      final BenefitPlanDependentRepository benefitPlanDependentRepository,
      final AwsHelper awsHelper,
      final UserBenefitsSettingService userBenefitsSettingService) {
    this.benefitPlanRepository = benefitPlanRepository;
    this.benefitPlanUserRepository = benefitPlanUserRepository;
    this.benefitPlanCoverageRepository = benefitPlanCoverageRepository;
    this.retirementPlanTypeRepository = retirementPlanTypeRepository;
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
    this.benefitPlanCoverageMapper = benefitPlanCoverageMapper;
    this.benefitPlanUserMapper = benefitPlanUserMapper;
    this.retirementPlanTypeMapper = retirementPlanTypeMapper;
    this.benefitPlanMapper = benefitPlanMapper;
    this.myBenefitsMapper = myBenefitsMapper;
    this.userMapper = userMapper;
    this.userRepository = userRepository;
    this.userDependentsRepository = userDependentsRepository;
    this.benefitPlanDependentRepository = benefitPlanDependentRepository;
    this.awsHelper = awsHelper;
    this.userBenefitsSettingService = userBenefitsSettingService;
  }

  public BenefitPlanDto createBenefitPlan(
      final NewBenefitPlanWrapperDto data, final String companyId) {
    final BenefitPlanCreateDto benefitPlanCreateDto = data.getBenefitPlan();
    final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = data.getCoverages();
    final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList;

    if (data.getForAllEmployees()) {
      benefitPlanUserCreateDtoList =
          userRepository.findAllByCompanyId(companyId).stream()
              .map(user -> new BenefitPlanUserCreateDto(user.getId()))
              .collect(Collectors.toList());
    } else {
      benefitPlanUserCreateDtoList = data.getSelectedEmployees();
    }

    final BenefitPlan benefitPlan =
        benefitPlanMapper.createFromBenefitPlanCreateDto(benefitPlanCreateDto);
    benefitPlan.setCompany(new Company(companyId));

    final BenefitPlan createdBenefitPlan = benefitPlanRepository.save(benefitPlan);

    if (benefitPlanCreateDto.getRetirementTypeId() != null) {
      final RetirementPlanType retirementPlanType =
          new RetirementPlanType(
              createdBenefitPlan, new RetirementType(benefitPlanCreateDto.getRetirementTypeId()));
      retirementPlanTypeRepository.save(retirementPlanType);
    }

    if (!benefitPlanCoverageDtoList.isEmpty()) {
      benefitPlanCoverageRepository.saveAll(
          benefitPlanCoverageDtoList.stream()
              .map(
                  benefitPlanCoverageDto ->
                      benefitPlanCoverageMapper.createFromBenefitPlanCoverageAndBenefitPlan(
                          benefitPlanCoverageDto, createdBenefitPlan))
              .collect(Collectors.toList()));
    }

    benefitPlanUserRepository.saveAll(
        benefitPlanUserCreateDtoList.stream()
            .map(
                benefitPlanUserCreateDto ->
                    benefitPlanUserMapper.createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
                        benefitPlanUserCreateDto, createdBenefitPlan.getId()))
            .collect(Collectors.toList()));

    return benefitPlanMapper.convertToBenefitPlanDto(benefitPlan);
  }

  public BenefitPlanDto updateBenefitPlan(
      final NewBenefitPlanWrapperDto data, final String planId, final String companyId) {
    final BenefitPlanCreateDto benefitPlanCreateDto = data.getBenefitPlan();
    final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = data.getCoverages();

    final BenefitPlan benefitPlan = benefitPlanRepository.findBenefitPlanById(planId);

    benefitPlanMapper.updateFromBenefitPlanCreateDto(benefitPlan, benefitPlanCreateDto);

    updateBenefitDocuments(benefitPlan, data.getBenefitPlan().getRemainingDocumentIds());
    final BenefitPlan updatedBenefitPlan = benefitPlanRepository.save(benefitPlan);

    updateRetirementPlanType(benefitPlanCreateDto, planId, updatedBenefitPlan);

    updateBenefitPlanCoverage(benefitPlanCoverageDtoList, planId, updatedBenefitPlan);

    updateBenefitPlanUser(data, companyId, planId);

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

    if (benefitPlanCreateDto.getRetirementTypeId() != null) {
      final RetirementPlanType newRetirementPlanType =
          new RetirementPlanType(
              updatedBenefitPlan, new RetirementType(benefitPlanCreateDto.getRetirementTypeId()));
      retirementPlanTypeMapper.updateFromNewRetirementPlanType(
          retirementPlanType, newRetirementPlanType);
      retirementPlanTypeRepository.save(retirementPlanType);
    }
  }

  private void updateBenefitPlanCoverage(
      final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList,
      final String planId,
      final BenefitPlan updatedBenefitPlan) {

    final List<BenefitPlanCoverage> benefitPlanCoverages =
        benefitPlanCoverageRepository.findAllByBenefitPlanId(planId);

    if (!benefitPlanCoverageDtoList.isEmpty()) {
      final List<String> benefitPlanCoverageIds = new ArrayList<>();

      benefitPlanCoverageDtoList.forEach(
          benefitPlanCoverageDto -> {
            benefitPlanCoverageIds.add(benefitPlanCoverageDto.getId());
            final BenefitPlanCoverage benefitPlanCoverage =
                benefitPlanCoverageMapper.updateFromBenefitPlanCoverageAndBenefitPlan(
                    benefitPlanCoverageDto, updatedBenefitPlan);
            benefitPlanCoverageRepository.save(benefitPlanCoverage);
          });

      benefitPlanCoverages.forEach(
          benefitPlanCoverage -> {
            if (!benefitPlanCoverageIds.contains(benefitPlanCoverage.getId())) {
              benefitPlanCoverageRepository.delete(benefitPlanCoverage);
            }
          });
      return;
    }
    benefitPlanCoverageRepository.delete(benefitPlanCoverages);
  }

  private void updateBenefitPlanUser(
      final NewBenefitPlanWrapperDto data, final String companyId, final String planId) {

    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(planId));
    if (data.getForAllEmployees() || !data.getSelectedEmployees().isEmpty()) {
      final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList;
      if (data.getForAllEmployees()) {
        benefitPlanUserCreateDtoList =
            userRepository.findAllByCompanyId(companyId).stream()
                .map(user -> new BenefitPlanUserCreateDto(user.getId()))
                .collect(Collectors.toList());
      } else {
        benefitPlanUserCreateDtoList = data.getSelectedEmployees();
      }

      updateBenefitPlanUsers(planId, benefitPlanUserCreateDtoList);
      return;
    }
    benefitPlanUserRepository.delete(benefitPlanUsers);
  }

  public BenefitPlan findBenefitPlanById(final String id) {
    return benefitPlanRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cannot find benefit plan"));
  }

  public boolean existsByUserIdAnAndBenefitPlanId(final String userId, final String benefitPlanId) {
    return benefitPlanUserRepository
        .findByUserIdAndBenefitPlanId(userId, benefitPlanId)
        .isPresent();
  }

  public void save(final BenefitPlan benefitPlan) {
    benefitPlanRepository.save(benefitPlan);
  }

  public List<BenefitPlanTypeDto> getBenefitPlanTypesAndNum(final String companyId) {
    return benefitPlanRepository.findPlanTypeAndNumByCompanyIdOrderByTypeId(companyId);
  }

  public List<BenefitPlanPreviewDto> getBenefitPlanPreview(
      final String companyId, final String planTypeId) {
    final List<BenefitPlan> benefitPlans =
        benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyId(planTypeId, companyId);

    final List<BenefitPlanPreviewDto> benefitPlanPreviewDtos = new LinkedList<>();

    benefitPlans.forEach(
        benefitPlan -> {
          final Number eligibleNumber =
              benefitPlanUserRepository.getEligibleEmployeeNumber(benefitPlan.getId());
          final Number enrolledNumber =
              benefitPlanUserRepository.countByBenefitPlanIdAndConfirmedIsTrue(benefitPlan.getId());
          benefitPlanPreviewDtos.add(
              new BenefitPlanPreviewDto(
                  benefitPlan.getId(), benefitPlan.getName(), eligibleNumber, enrolledNumber));
        });

    return benefitPlanPreviewDtos;
  }

  public void updateBenefitPlanUsers(
      final String benefitPlanId, final List<BenefitPlanUserCreateDto> benefitPlanUsers) {
    final List<BenefitPlanUser> originalBenefitPlanUsers =
        benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(benefitPlanId));

    final List<BenefitPlanUser> deleteBenefitPlanUsers =
        originalBenefitPlanUsers.stream()
            .filter(
                originalBenefitPlanUser ->
                    benefitPlanUsers.stream()
                        .noneMatch(
                            benefitPlanUser ->
                                benefitPlanUser
                                    .getId()
                                    .equals(originalBenefitPlanUser.getUser().getId())))
            .collect(Collectors.toList());

    final List<BenefitPlanUserCreateDto> saveBenefitPlanUserCreateDtos =
        benefitPlanUsers.stream()
            .filter(
                benefitPlanUser ->
                    originalBenefitPlanUsers.stream()
                        .noneMatch(
                            originalBenefitPlanUser ->
                                originalBenefitPlanUser
                                    .getUser()
                                    .getId()
                                    .equals(benefitPlanUser.getId())))
            .collect(Collectors.toList());

    benefitPlanUserRepository.delete(deleteBenefitPlanUsers);

    saveBenefitPlanUserCreateDtos.forEach(
        saveBenefitPlanUserCreateDto ->
            benefitPlanUserRepository.save(
                benefitPlanUserMapper.createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
                    saveBenefitPlanUserCreateDto, benefitPlanId)));
  }

  public BenefitSummaryDto getBenefitSummary(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId);
    final Long benefitNumber = (long) benefitPlanUsers.size();
    BigDecimal benefitCost = BigDecimal.valueOf(0);
    final List<BenefitPlanDependentUserDto> dependentUsers = new ArrayList<>();
    final List<String> dependentUserIds = new ArrayList<>();
    for (final BenefitPlanUser benefitPlanUser : benefitPlanUsers) {
      benefitCost = benefitCost.add(benefitPlanUser.getBenefitPlanCoverage().getEmployeeCost());
      final Set<BenefitPlanDependent> benefitPlanDependents = benefitPlanUser
          .getBenefitPlanDependents();
      for (final BenefitPlanDependent benefitPlanDependent : benefitPlanDependents) {
        if  (!dependentUserIds.contains(benefitPlanDependent.getId())) {
          final BenefitPlanDependentUserDto benefitPlanDependentUserDto =
              BenefitPlanDependentUserDto.builder()
              .id(benefitPlanDependent.getId())
              .firstName(benefitPlanDependent.getFirstName())
              .lastName(benefitPlanDependent.getLastName())
              .build();
          dependentUsers.add(benefitPlanDependentUserDto);
          dependentUserIds.add(benefitPlanDependent.getId());
        }
      }
    }
    return myBenefitsMapper.convertToBenefitSummaryDto(
      benefitNumber, benefitCost, (long)dependentUsers.size(), dependentUsers);
  }

  public List<UserBenefitPlanDto> getUserBenefitPlans(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId);
    return benefitPlanUsers.stream()
        .map(benefitPlanUserMapper::convertFrom)
        .collect(Collectors.toList());
  }

  public List<UserBenefitPlanDto> getUserAvailableBenefitPlans(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findAllByUserId(userId);

    return benefitPlanUsers.stream()
        .map(benefitPlanUserMapper::convertFrom)
        .collect(Collectors.toList());
  }

  public void updateUserBenefitPlanEnrollmentInfo(
      final String userId,
      final List<SelectedEnrollmentInfoDto> selectedBenefitPlanInfo,
      final String companyId) {
    selectedBenefitPlanInfo.stream()
        .forEach(
            s -> {
              if (s.getBenefitPlanType().equals(BenefitPlanType.PlanType.OTHER.getValue())) {
                if (s.getPlanId() == null) {
                  final BenefitPlan benefitPlan =
                      benefitPlanRepository.findBenefitPlanByName(s.getOtherTypePlanTitle());
                  final BenefitPlanUser originBenefitPlanUser =
                      benefitPlanUserRepository
                          .findByUserIdAndBenefitPlanId(userId, benefitPlan.getId())
                          .get();
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

                clearBenefitPlansEnrollmentInfoByPlanType(benefitPlanType, companyId, userId);

              } else {
                // selected all this type of benefitPlan of user's company
                updateBenefitPlansEnrollmentInfoByPlanType(s, companyId, userId);
              }
            });
  }

  // clear all the enrollmentInfo relate to this type of
  // benefit plan when selected enroll planId is null
  private void clearBenefitPlansEnrollmentInfoByPlanType(
      final BenefitPlanType benefitPlanType, final String companyId, final String userId) {
    final List<BenefitPlan> benefitPlans =
        benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyId(
            benefitPlanType.getId(), companyId);

    benefitPlans.stream()
        .forEach(
            benefitPlan -> {
              final Optional<BenefitPlanUser> originBenefitPlanUser =
                  benefitPlanUserRepository.findByUserIdAndBenefitPlanId(
                      userId, benefitPlan.getId());

              if (originBenefitPlanUser.isPresent()) {
                final BenefitPlanUser newBenefitPlanUser = originBenefitPlanUser.get();
                newBenefitPlanUser.setEnrolled(false);
                newBenefitPlanUser.setBenefitPlanCoverage(null);
                final List<BenefitDependentRecord> records =
                    benefitPlanDependentRepository.findByBenefitPlansUsersId(
                        newBenefitPlanUser.getId());
                benefitPlanDependentRepository.delete(records);
                benefitPlanUserRepository.save(newBenefitPlanUser);
              }
            });
  }

  private void enrollBenefitPlanUser(
      final String userId,
      final BenefitPlan benefitPlan,
      final SelectedEnrollmentInfoDto selectedEnrollmentInfoDto) {
    final BenefitPlanUser originBenefitPlanUser =
        benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId, benefitPlan.getId()).get();
    originBenefitPlanUser.setEnrolled(true);
    if (selectedEnrollmentInfoDto.getCoverageOptionId() == null) {
      originBenefitPlanUser.setBenefitPlanCoverage(null);
    } else {
      originBenefitPlanUser.setBenefitPlanCoverage(
          benefitPlanCoverageRepository
              .findById(selectedEnrollmentInfoDto.getCoverageOptionId())
              .get());
    }

    if (selectedEnrollmentInfoDto.getSelectedDependents() != null
        && selectedEnrollmentInfoDto.getSelectedDependents().size() > 0) {
      updateSelectedDependentsByBenefitPlanUser(
          selectedEnrollmentInfoDto.getSelectedDependents(), originBenefitPlanUser.getId());
    }
    benefitPlanUserRepository.save(originBenefitPlanUser);
  }

  // update benefit enrollment info based on this benefitPlanDto
  private void updateBenefitPlansEnrollmentInfoByPlanType(
      final SelectedEnrollmentInfoDto selectedEnrollmentInfoDto,
      final String companyId,
      final String userId) {
    benefitPlanRepository
        .findByBenefitPlanTypeIdAndCompanyId(
            benefitPlanTypeRepository
                .findByName(selectedEnrollmentInfoDto.getBenefitPlanType())
                .getId(),
            companyId)
        .stream()
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
                final Optional<BenefitPlanUser> originBenefitPlanUser =
                    benefitPlanUserRepository.findByUserIdAndBenefitPlanId(
                        userId, benefitPlan.getId());
                if (originBenefitPlanUser.isPresent()) {
                  final BenefitPlanUser newBenefitPlanUser = originBenefitPlanUser.get();
                  newBenefitPlanUser.setEnrolled(false);
                  newBenefitPlanUser.setBenefitPlanCoverage(null);
                  benefitPlanUserRepository.save(newBenefitPlanUser);
                }
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
    oldDependentIds.stream()
        .forEach(
            oldDependentId -> {
              if (!newDependentIds.contains(oldDependentId)) {
                final Optional<BenefitDependentRecord> oldBenefitDependentRecord =
                    benefitPlanDependentRepository.findByBenefitPlansUsersIdAndUserDependentsId(
                        benefitPlanUserId, oldDependentId);
                benefitPlanDependentRepository.delete(oldBenefitDependentRecord.get());
              }
            });

    // map all the new records to find record which haven't been added
    newDependentIds.stream()
        .forEach(
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
  }

  public BenefitPlanUpdateDto getBenefitPlanByPlanId(final String planId) {
    final BenefitPlan benefitPlan = benefitPlanRepository.findBenefitPlanById(planId);
    final List<BenefitPlanCoverage> benefitPlanCoverage =
        benefitPlanCoverageRepository.findAllByBenefitPlanId(planId);
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(planId));
    final RetirementPlanType retirementPlanType =
        retirementPlanTypeRepository.findByBenefitPlan(new BenefitPlan(planId));
    return benefitPlanMapper.convertToOldBenefitPlanDto(
        benefitPlan, benefitPlanCoverage, benefitPlanUsers, retirementPlanType);
  }

  public void saveBenefitPlanDocuments(
      final String benefitPlanId, final List<MultipartFile> files) {
    final BenefitPlan benefitPlan = findBenefitPlanById(benefitPlanId);
    files.forEach(
        file -> {
          final String path = awsHelper.uploadFile(file, AccessType.PRIVATE);

          if (Strings.isBlank(path)) {
            throw new AwsException("AWS upload failed");
          }
          final String title =
              StringUtils.isNotBlank(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
          final String fileName = title.substring(0, title.lastIndexOf('.'));
          final BenefitPlanDocument document = new BenefitPlanDocument(fileName, path);
          benefitPlan.addBenefitPlanDocument(document);
        });
    save(benefitPlan);
  }

  public void confirmBenefitPlanEnrollment(
      final String userId,
      final List<SelectedEnrollmentInfoDto> selectedBenefitPlanInfo,
      final String companyId) {
    updateUserBenefitPlanEnrollmentInfo(userId, selectedBenefitPlanInfo, companyId);
    selectedBenefitPlanInfo.stream()
        .forEach(
            s -> {
              final BenefitPlanUser confirmedBenefitPlanUser =
                  benefitPlanUserRepository
                      .findByUserIdAndBenefitPlanId(userId, s.getPlanId())
                      .orElseThrow(
                          () -> new ResourceNotFoundException("Cannot find benefit plan user"));
              confirmedBenefitPlanUser.setConfirmed(true);
              benefitPlanUserRepository.save(confirmedBenefitPlanUser);
            });
    userBenefitsSettingService.saveUserBenefitsSettingEffectYear(
        userId, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
  }

  public boolean isConfirmed(final String userId) {
    final List<BenefitPlanUser> benefitPlanUsers =
        benefitPlanUserRepository.findByUserIdAndConfirmedIsTrue(userId);
    return benefitPlanUsers.size() > 0;
  }

  public BenefitPlanRelatedUserListDto updateBenefitPlanEmployees(
      final List<BenefitPlanUserCreateDto> employees,
      final String benefitPlanId, final String companyId) {
    final List<String> updateUsers = employees.stream().map(employee -> employee.getId())
        .collect(Collectors.toList());
    final List<BenefitPlanUser> existPlans = benefitPlanUserRepository
        .findAllByBenefitPlanId(benefitPlanId);
    final List<String> existUsers = existPlans.stream().map(benefitPlanUser ->
        benefitPlanUser.getUser().getId())
        .collect(Collectors.toList());
    final List<BenefitPlanUser> saveUsers = employees.stream().filter(employee ->
        !existUsers.contains(employee.getId()))
        .map(s -> benefitPlanUserMapper
            .createFromBenefitPlanUserCreateDtoAndBenefitPlanId(s, benefitPlanId))
        .collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(saveUsers)) {
      benefitPlanUserRepository.saveAll(saveUsers);
    }
    final List<String> deletePlanUsers = existPlans.stream().filter(existPlan -> !updateUsers
        .contains(existPlan.getUser().getId()))
        .map(s -> s.getId()).collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(deletePlanUsers)) {
      benefitPlanUserRepository.deleteInBatch(deletePlanUsers);
    }
    return findRelatedUsersByBenefitPlan(benefitPlanId, companyId);
  }

  public BenefitPlanRelatedUserListDto findRelatedUsersByBenefitPlan(final String benefitPlanId,
      final String companyId) {
    final List<String> selectedUserIds = new ArrayList<>();
    final List<User> allUsers = userRepository.findAllByCompanyId(companyId);
    final List<BenefitPlanUser> benefitPlanUserList = benefitPlanUserRepository
        .findAllByBenefitPlanId(benefitPlanId);
    final List<BenefitPlanUserDto> selectedUsers = benefitPlanUserList.stream()
        .filter(distinctByKey(b -> b.getUser().getId()))
        .map(benefitPlanUser -> {
          selectedUserIds.add(benefitPlanUser.getUser().getId());
          return benefitPlanUserMapper.convertToBenefitPlanUserDto(benefitPlanUser);
        }).collect(Collectors.toList());
    final List<BenefitPlanUserDto> unselectedUsers = allUsers.stream().filter(user ->
        !selectedUserIds.contains(user.getId()))
        .map(userMapper::covertToBenefitPlanUserDto).collect(Collectors.toList());
    return new BenefitPlanRelatedUserListDto(unselectedUsers,selectedUsers);
  }


  static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object,Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }
}
