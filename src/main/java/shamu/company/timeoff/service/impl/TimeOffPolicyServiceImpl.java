package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.DENIED;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffBalanceItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.mapper.AccrualScheduleMilestoneMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyAccrualScheduleMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.pojo.TimeOffPolicyListPojo;
import shamu.company.timeoff.pojo.TimeOffRequestStatusPojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserRepository;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {

  private final TimeOffPolicyRepository timeOffPolicyRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  private final JobUserRepository jobUserRepository;

  private final UserRepository userRepository;

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  private final TimeOffDetailService timeOffDetailService;

  private final AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper;

  private final TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper;

  private final TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  private final TimeOffPolicyMapper timeOffPolicyMapper;

  private final JobUserMapper jobUserMapper;

  @Autowired
  public TimeOffPolicyServiceImpl(
      final TimeOffPolicyRepository timeOffPolicyRepository,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final JobUserRepository jobUserRepository, final TimeOffDetailService timeOffDetailService,
      final UserRepository userRepository,
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      final AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper,
      final TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper,
      final TimeOffPolicyUserMapper timeOffPolicyUserMapper,
      final TimeOffPolicyMapper timeOffPolicyMapper,
      final JobUserMapper jobUserMapper) {
    this.timeOffPolicyRepository = timeOffPolicyRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.jobUserRepository = jobUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.timeOffDetailService = timeOffDetailService;
    this.accrualScheduleMilestoneMapper = accrualScheduleMilestoneMapper;
    this.timeOffPolicyAccrualScheduleMapper = timeOffPolicyAccrualScheduleMapper;
    this.timeOffPolicyUserMapper = timeOffPolicyUserMapper;
    this.timeOffPolicyMapper = timeOffPolicyMapper;
    this.jobUserMapper = jobUserMapper;
  }

  @Override
  public void createTimeOffPolicy(final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto,
      final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos,
      final Company company) {

    final TimeOffPolicy timeOffPolicy = timeOffPolicyRepository
        .save(timeOffPolicyMapper
            .createFromTimeOffPolicyFrontendDtoAndCompany(timeOffPolicyFrontendDto, company));
    final Long policyId = timeOffPolicy.getId();
    final Long timeOffAccrualFrequencyId = timeOffPolicyAccrualScheduleDto
        .getTimeOffAccrualFrequencyId();

    if (timeOffPolicyFrontendDto.getIsLimited()) {
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          timeOffPolicyAccrualScheduleRepository
              .save(timeOffPolicyAccrualScheduleMapper
                  .createTimeOffPolicyAccrualSchedule(timeOffPolicyAccrualScheduleDto,
                      timeOffPolicy, timeOffAccrualFrequencyId));

      final Long scheduleId = timeOffPolicyAccrualSchedule.getId();
      createAccrualScheduleMilestones(accrualScheduleMilestoneDtoList, scheduleId);
    }

    createTimeOffPolicyUsers(timeOffPolicyUserFrontendDtos, policyId);
  }

  @Override
  public TimeOffBalanceDto getTimeOffBalances(final User user) {

    final List<TimeOffPolicyUser> policyUsers = timeOffPolicyUserRepository
        .findTimeOffPolicyUsersByUser(user);
    final Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    boolean showTotalBalance = true;
    final LocalDateTime currentTime = LocalDateTime.now();
    final List<TimeOffBalanceItemDto> timeOffBalanceItemDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      final TimeOffPolicyUser policyUser = policyUserIterator.next();
      final Long policyUserId = policyUser.getId();
      final TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
          .getTimeOffBreakdown(policyUserId, currentTime);
      final Integer balance = timeOffBreakdownDto.getBalance();

      final TimeOffBalanceItemDto timeOffBalanceItemDto = TimeOffBalanceItemDto.builder()
          .id(policyUserId)
          .balance(balance)
          .name(policyUser.getTimeOffPolicy().getName())
          .showBalance(timeOffBreakdownDto.isShowBalance())
          .build();

      timeOffBalanceItemDtos.add(timeOffBalanceItemDto);

      showTotalBalance = showTotalBalance && timeOffBreakdownDto.isShowBalance();
    }

    return TimeOffBalanceDto.builder()
        .timeOffBalanceItemDtos(timeOffBalanceItemDtos)
        .showTotalBalance(showTotalBalance)
        .build();
  }

  @Override
  public List<TimeOffPolicyUserDto> getTimeOffPolicyUser(final User user) {

    final List<TimeOffPolicyUser> policyUsers = timeOffPolicyUserRepository
        .findTimeOffPolicyUsersByUser(user);
    final Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    final LocalDateTime currentTime = LocalDateTime.now();
    final List<TimeOffPolicyUserDto> timeOffPolicyUserDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      final TimeOffPolicyUser policyUser = policyUserIterator.next();
      final Long policyUserId = policyUser.getId();
      final TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
          .getTimeOffBreakdown(policyUserId, currentTime);
      final Integer balance = timeOffBreakdownDto.getBalance();

      final TimeOffPolicyUserDto timeOffPolicyUserDto = timeOffPolicyUserMapper
          .convertToTimeOffPolicyUserDto(policyUser);
      timeOffPolicyUserDto.setBalance(balance);
      timeOffPolicyUserDtos.add(timeOffPolicyUserDto);
    }

    return timeOffPolicyUserDtos;
  }

  @Override
  public void createTimeOffPolicyUsers(final List<TimeOffPolicyUser> timeOffPolicyUsers) {
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  private void createTimeOffPolicyUsers(
      final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos,
      final Long policyId) {
    if (!timeOffPolicyUserFrontendDtos.isEmpty()) {
      timeOffPolicyUserRepository
          .saveAll(timeOffPolicyUserFrontendDtos.stream().map(timeOffPolicyUserFrontendDto ->
              timeOffPolicyUserMapper.createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(
                  timeOffPolicyUserFrontendDto, policyId))
              .collect(Collectors.toList()));
    }
  }

  @Override
  public Integer getTimeOffBalanceByUserAndPolicy(final User user,
      final TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(user, timeOffPolicy)
        .getBalance();
  }

  @Override
  public TimeOffPolicy getTimeOffPolicyById(final Long id) {
    return timeOffPolicyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Time off policy was not found."));
  }

  @Override
  public TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(final Long policyId) {
    final TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.findById(policyId).get();

    if (!timeOffPolicy.getIsLimited()) {
      return new TimeOffPolicyRelatedInfoDto(
          timeOffPolicy, null, null);
    }
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(timeOffPolicy);

    final List<AccrualScheduleMilestone> accrualScheduleMilestones =
        accrualScheduleMilestoneRepository
            .findByTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualSchedule.getId());

    return new TimeOffPolicyRelatedInfoDto(
        timeOffPolicy, timeOffPolicyAccrualSchedule, accrualScheduleMilestones);
  }

  @Override
  public TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      final Long timeOffPolicyId, final Long companyId) {
    final TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.findById(timeOffPolicyId)
        .orElseThrow(() -> new ResourceNotFoundException("Time off policy does not exist."));

    final Boolean isLimited = timeOffPolicy.getIsLimited();

    final List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);

    final List<User> selectableTimeOffPolicyUsers = userRepository
        .findAllByCompanyId(companyId);

    final ArrayList<Long> selectedUsersIds = new ArrayList<>();

    final List<TimeOffPolicyRelatedUserDto> selectedEmployees = timeOffPolicyUsers.stream().map(
        (timeOffPolicyUser) -> {
          JobUser employeeWithJobInfo = jobUserRepository
              .findJobUserByUser(timeOffPolicyUser.getUser());
          selectedUsersIds.add(timeOffPolicyUser.getUser().getId());
          return jobUserMapper.convertToTimeOffPolicyRelatedUserDto(timeOffPolicyUser,
              employeeWithJobInfo);
        }
    ).collect(Collectors.toList());

    final List<TimeOffPolicyRelatedUserDto> unselectedEmployees = selectableTimeOffPolicyUsers
        .stream().filter(user -> !selectedUsersIds.contains(user.getId()))
        .map(user -> {
          JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
          return jobUserMapper.convertToTimeOffPolicyRelatedUserDto(user, employeeWithJobInfo);
        }).collect(Collectors.toList());

    return new TimeOffPolicyRelatedUserListDto(
        isLimited, unselectedEmployees, selectedEmployees);
  }

  @Override
  public void updateTimeOffPolicy(final TimeOffPolicy timeOffPolicy) {
    timeOffPolicyRepository.save(timeOffPolicy);
  }

  @Override
  public TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      final TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(timeOffPolicy);
  }

  @Override
  public List<TimeOffPolicyUser> getAllPolicyUsersByUser(final User user) {
    return timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(user);
  }

  @Override
  public List<TimeOffPolicyListDto> getAllPolicies(final Long companyId) {
    final List<TimeOffPolicyListPojo> timeOffPolicies = timeOffPolicyRepository
        .getAllPolicies(companyId);
    final Iterator<TimeOffPolicyListPojo> timeOffPolicyIterator = timeOffPolicies.iterator();

    final List<TimeOffPolicyListDto> timeOffPolicyListDtoList = new ArrayList<>();
    while (timeOffPolicyIterator.hasNext()) {
      final TimeOffPolicyListDto timeOffPolicyListDto = new TimeOffPolicyListDto();
      final TimeOffPolicyListPojo timeOffPolicyListPojo = timeOffPolicyIterator.next();
      BeanUtils.copyProperties(timeOffPolicyListPojo, timeOffPolicyListDto);
      timeOffPolicyListDtoList.add(timeOffPolicyListDto);
    }

    return timeOffPolicyListDtoList;
  }

  @Override
  @Transactional
  public TimeOffPolicyAccrualSchedule updateTimeOffPolicySchedule(final TimeOffPolicy timeOffPolicy,
      final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto) {

    final TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicy);

    TimeOffPolicyAccrualSchedule newTimeOffSchedule = timeOffPolicyAccrualScheduleMapper
        .createTimeOffPolicyAccrualSchedule(timeOffPolicyAccrualScheduleDto, timeOffPolicy,
            originTimeOffSchedule.getTimeOffAccrualFrequency().getId());

    if (!isScheduleChanged(originTimeOffSchedule, newTimeOffSchedule)) {
      return originTimeOffSchedule;
    }

    originTimeOffSchedule.setExpiredAt(new Timestamp(new Date().getTime()));
    timeOffPolicyAccrualScheduleRepository.save(originTimeOffSchedule);
    newTimeOffSchedule = timeOffPolicyAccrualScheduleRepository.save(newTimeOffSchedule);

    accrualScheduleMilestoneRepository
        .updateMilestoneSchedule(originTimeOffSchedule.getId(), newTimeOffSchedule.getId());

    return newTimeOffSchedule;
  }

  private boolean isScheduleChanged(final TimeOffPolicyAccrualSchedule originalSchedule,
      final TimeOffPolicyAccrualSchedule newSchedule) {
    return originalSchedule.getDaysBeforeAccrualStarts() != newSchedule.getDaysBeforeAccrualStarts()
        || originalSchedule.getAccrualHours() != newSchedule.getAccrualHours()
        || originalSchedule.getCarryoverLimit() != newSchedule.getCarryoverLimit()
        || originalSchedule.getMaxBalance() != newSchedule.getMaxBalance();
  }

  @Override
  public List<AccrualScheduleMilestone> updateTimeOffPolicyMilestones(
      final TimeOffPolicy timeOffPolicyUpdated,
      final List<AccrualScheduleMilestoneDto> milestones) {
    final TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicyUpdated);

    if (originTimeOffSchedule == null && CollectionUtils.isEmpty(milestones)) {
      return null;
    }

    final List<AccrualScheduleMilestone> accrualScheduleMilestoneList;

    if (originTimeOffSchedule == null) {
      accrualScheduleMilestoneList = new ArrayList<>();
    } else {
      accrualScheduleMilestoneList = accrualScheduleMilestoneRepository
          .findByTimeOffPolicyAccrualScheduleId(originTimeOffSchedule.getId());
    }

    final Long accrualScheduleId = originTimeOffSchedule.getId();
    final List<AccrualScheduleMilestone> newAccrualMilestoneList = milestones.stream()
        .map(accrualScheduleMilestoneDto -> accrualScheduleMilestoneMapper
            .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                accrualScheduleMilestoneDto, accrualScheduleId))
        .collect(Collectors.toList());

    sortMilestoneList(accrualScheduleMilestoneList);
    sortMilestoneList(newAccrualMilestoneList);

    final HashMap<Integer, List<AccrualScheduleMilestone>> hashMap =
        transformMilestoneListToMap(accrualScheduleMilestoneList, newAccrualMilestoneList);

    return hashMap.entrySet()
        .stream()
        .map(Map.Entry::getValue)
        .map(this::expireAndSaveMilestones)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public void addTimeOffAdjustments(final User currentUser, final Long policyUserId,
      final Integer adjustment) {
    final TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserRepository.findById(policyUserId)
        .get();

    final TimeOffAdjustment timeOffAdjustment =
        new TimeOffAdjustment(timeOffPolicyUser, timeOffPolicyUser.getTimeOffPolicy(), currentUser);
    timeOffAdjustment.setAmount(adjustment);

    final UserPersonalInformation userPersonalInformation = currentUser
        .getUserPersonalInformation();
    timeOffAdjustment.setComment("Adjusted by User " + userPersonalInformation.getName());
    timeOffAdjustmentRepository.save(timeOffAdjustment);
  }

  private void sortMilestoneList(
      final List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    accrualScheduleMilestoneList
        .sort(Comparator.comparingInt(AccrualScheduleMilestone::getAnniversaryYear));
  }

  private HashMap<Integer, List<AccrualScheduleMilestone>> transformMilestoneListToMap(
      final List<AccrualScheduleMilestone> originMilestones,
      final List<AccrualScheduleMilestone> newMilestones) {
    final HashMap<Integer, List<AccrualScheduleMilestone>> milestoneMap = new HashMap<>();
    for (final AccrualScheduleMilestone accrualMilestone : originMilestones) {
      final List<AccrualScheduleMilestone> milestoneList = new ArrayList<>(
          Collections.singletonList(accrualMilestone));
      milestoneMap.put(accrualMilestone.getAnniversaryYear(), milestoneList);
    }

    for (final AccrualScheduleMilestone accrualScheduleMilestone : newMilestones) {
      final List<AccrualScheduleMilestone> accrualScheduleMilestones = milestoneMap
          .get(accrualScheduleMilestone.getAnniversaryYear());
      if (accrualScheduleMilestones != null) {
        accrualScheduleMilestones.add(accrualScheduleMilestone);
      } else {
        final List<AccrualScheduleMilestone> milestoneList = new ArrayList<>(
            Collections.singletonList(accrualScheduleMilestone));
        milestoneMap.put(accrualScheduleMilestone.getAnniversaryYear(), milestoneList);
      }
    }

    return milestoneMap;
  }

  private AccrualScheduleMilestone expireAndSaveMilestones(
      final List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    if (accrualScheduleMilestoneList.size() == 1
        && accrualScheduleMilestoneList.get(0).getId() == null) {
      return accrualScheduleMilestoneRepository.save(accrualScheduleMilestoneList.get(0));
    } else if (accrualScheduleMilestoneList.size() == 1
        && accrualScheduleMilestoneList.get(0).getId() != null) {
      accrualScheduleMilestoneRepository.delete(accrualScheduleMilestoneList.get(0).getId());
      return null;
    }

    final AccrualScheduleMilestone originalMilestone = accrualScheduleMilestoneList.get(0);
    final AccrualScheduleMilestone nowMilestone = accrualScheduleMilestoneList.get(1);

    if (!isMilestoneChanged(originalMilestone, nowMilestone)) {
      return originalMilestone;
    }

    originalMilestone.setExpiredAt(new Timestamp(new Date().getTime()));
    accrualScheduleMilestoneRepository.save(originalMilestone);
    return accrualScheduleMilestoneRepository.save(nowMilestone);
  }

  private boolean isMilestoneChanged(final AccrualScheduleMilestone originalMilestone,
      final AccrualScheduleMilestone newMilestone) {
    return originalMilestone.getAccrualHours() != newMilestone.getAccrualHours()
        || originalMilestone.getCarryoverLimit() != newMilestone.getCarryoverLimit()
        || originalMilestone.getMaxBalance() != newMilestone.getMaxBalance();
  }

  private void createAccrualScheduleMilestones(
      final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      final Long scheduleId) {
    if (!accrualScheduleMilestoneDtoList.isEmpty()) {
      accrualScheduleMilestoneRepository.saveAll(
          accrualScheduleMilestoneDtoList.stream()
              .map(accrualScheduleMilestoneDto ->
                  accrualScheduleMilestoneMapper
                      .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                          accrualScheduleMilestoneDto, scheduleId))
              .collect(Collectors.toList()));
    }
  }

  @Override
  public void updateMilestones(
      final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList, final Long scheduleId
  ) {
    final List<Integer> anniversaryYears = accrualScheduleMilestoneDtoList.stream()
        .map(AccrualScheduleMilestoneDto::getAnniversaryYear)
        .collect(Collectors.toList());
    final List<AccrualScheduleMilestone> all = accrualScheduleMilestoneRepository
        .findByTimeOffPolicyAccrualScheduleId(scheduleId);
    final List<Integer> anniversaryYearsBefore = all.stream()
        .map(AccrualScheduleMilestone::getAnniversaryYear)
        .collect(Collectors.toList());
    all.stream().forEach(
        accrualScheduleMilestone -> {
          if (!anniversaryYears.contains(accrualScheduleMilestone.getAnniversaryYear())) {
            accrualScheduleMilestoneRepository.delete(accrualScheduleMilestone);
          }
          final Optional<AccrualScheduleMilestoneDto> updateAccrualScheduleMilestoneDto =
              accrualScheduleMilestoneDtoList.stream().filter(a -> a.getAnniversaryYear()
                  == accrualScheduleMilestone.getAnniversaryYear()).findFirst();
          if (updateAccrualScheduleMilestoneDto.isPresent()) {
            accrualScheduleMilestoneMapper
                .updateFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                    accrualScheduleMilestone, updateAccrualScheduleMilestoneDto.get(), scheduleId);
            accrualScheduleMilestoneRepository.save(accrualScheduleMilestone);
          }
        });
    // add completed new milestones info
    final List<AccrualScheduleMilestone> newAccrualMileStones = accrualScheduleMilestoneDtoList
        .stream()
        .filter(milestoneDto -> !anniversaryYearsBefore.contains(milestoneDto.getAnniversaryYear()))
        .map(milestoneDto -> accrualScheduleMilestoneMapper
            .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(milestoneDto,
                scheduleId)).collect(
            Collectors.toList());
    accrualScheduleMilestoneRepository.saveAll(newAccrualMileStones);
  }

  @Override
  public void updateTimeOffPolicyUserInfo(
      final List<TimeOffPolicyUserFrontendDto> userStatBalances, final Long timeOffPolicyId) {
    final List<Long> newUserIds = userStatBalances.stream()
        .map(TimeOffPolicyUserFrontendDto::getUserId)
        .collect(Collectors.toList());
    final List<TimeOffPolicyUser> oldUsersStartBalanceList = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);
    final List<Long> oldUserIds = oldUsersStartBalanceList.stream()
        .map(user -> user.getUser().getId())
        .collect(Collectors.toList());
    oldUsersStartBalanceList.stream()
        .forEach(
            oldUsersStartBalance -> {
              if (newUserIds.contains(oldUsersStartBalance.getUser().getId())) {
                // update
                final Optional<TimeOffPolicyUserFrontendDto> updateUserStartBalance =
                    userStatBalances.stream().filter(u -> u.getUserId()
                        == oldUsersStartBalance.getUser().getId()).findFirst();
                if (updateUserStartBalance.isPresent()) {
                  final TimeOffPolicyUserFrontendDto newUserStartBalance = updateUserStartBalance
                      .get();
                  oldUsersStartBalance.setBalance(newUserStartBalance.getBalance());
                  timeOffPolicyUserRepository.save(oldUsersStartBalance);
                }
                return;
              }
              // delete
              timeOffPolicyUserRepository.delete(oldUsersStartBalance);
            });
    // add new user
    final List<TimeOffPolicyUser> timeOffPolicyUsers = userStatBalances.stream()
        .filter(user -> !oldUserIds.contains(user.getUserId()))
        .map(statBalance -> timeOffPolicyUserMapper
            .createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(statBalance, timeOffPolicyId))
        .collect(Collectors.toList());
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  @Override
  public void deleteTimeOffPolicy(final Long timeOffPolicyId) {
    final TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.getOne(timeOffPolicyId);
    final List<TimeOffRequestStatusPojo> requests = timeOffRequestRepository
        .findByTimeOffPolicyId(timeOffPolicyId);
    requests.stream().filter(request ->
        request.getTimeOffApprovalStatus() != APPROVED
            && request.getTimeOffApprovalStatus() != DENIED)
        .forEach(request -> timeOffRequestRepository.delete(request.getId()));

    final List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);
    timeOffPolicyUserRepository.delete(timeOffPolicyUsers);

    final Long accrualScheduleId = timeOffPolicyAccrualScheduleRepository
        .findIdByTimeOffPolicyId(timeOffPolicy.getId());
    if (accrualScheduleId != null) {
      timeOffPolicyAccrualScheduleRepository.delete(accrualScheduleId);

      final List<AccrualScheduleMilestone> milestones = accrualScheduleMilestoneRepository
          .findByTimeOffPolicyAccrualScheduleId(accrualScheduleId);
      accrualScheduleMilestoneRepository.deleteAll(milestones);
    }

    timeOffPolicyRepository.delete(timeOffPolicyId);
  }

  @Override
  public List<TimeOffPolicyUser> getAllPolicyUsersByPolicyId(final Long id) {
    return timeOffPolicyUserRepository.findAllByTimeOffPolicyId(id);
  }

  @Override
  public void enrollTimeOffHours(final List<TimeOffPolicyUser> policyUsers,
      final TimeOffPolicy enrollPolicy, final Long currentUserId) {
    policyUsers.stream().forEach(policyUser -> {
      final TimeOffBreakdownDto timeOffBreakdown = timeOffDetailService
          .getTimeOffBreakdown(policyUser.getId(), LocalDateTime.now());

      final Integer remainingBalance = timeOffBreakdown.getBalance();
      final TimeOffAdjustment timeOffAdjustment = TimeOffAdjustment.builder()
          .timeOffPolicy(enrollPolicy)
          .adjusterUserId(currentUserId)
          .amount(remainingBalance)
          .comment(String.format("Rolled from policy %s", policyUser.getTimeOffPolicy().getName()))
          .company(policyUser.getUser().getCompany())
          .user(policyUser.getUser())
          .build();
      timeOffAdjustmentRepository.save(timeOffAdjustment);
    });
  }
}
