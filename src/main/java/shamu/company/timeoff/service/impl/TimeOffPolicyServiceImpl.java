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
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffBalanceItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyList;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
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

  @Autowired
  public TimeOffPolicyServiceImpl(
      TimeOffPolicyRepository timeOffPolicyRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      JobUserRepository jobUserRepository, TimeOffDetailService timeOffDetailService,
      UserRepository userRepository,
      TimeOffRequestRepository timeOffRequestRepository,
      TimeOffAdjustmentRepository timeOffAdjustmentRepository) {
    this.timeOffPolicyRepository = timeOffPolicyRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.jobUserRepository = jobUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.timeOffDetailService = timeOffDetailService;
  }

  @Override
  public void createTimeOffPolicy(TimeOffPolicyPojo timeOffPolicyPojo,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos,
      Company company) {

    TimeOffPolicy timeOffPolicy = timeOffPolicyRepository
        .save(timeOffPolicyPojo.getTimeOffPolicy(company));
    Long policyId = timeOffPolicy.getId();
    Long timeOffAccrualFrequencyId = timeOffPolicyAccrualScheduleDto.getTimeOffAccrualFrequencyId();

    if (timeOffPolicyPojo.getIsLimited()) {
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          timeOffPolicyAccrualScheduleRepository
              .save(timeOffPolicyAccrualScheduleDto
                  .getTimeOffPolicyAccrualSchedule(timeOffPolicy, timeOffAccrualFrequencyId));

      Long scheduleId = timeOffPolicyAccrualSchedule.getId();
      createAccrualScheduleMilestones(accrualScheduleMilestoneDtoList, scheduleId);
    }

    createTimeOffPolicyUsers(timeOffPolicyUserPojos, policyId);
  }

  @Override
  public TimeOffBalanceDto getTimeOffBalances(User user) {

    List<TimeOffPolicyUser> policyUsers = timeOffPolicyUserRepository
        .findTimeOffPolicyUsersByUser(user);
    Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    boolean showTotalBalance = true;
    LocalDateTime currentTime = LocalDateTime.now();
    List<TimeOffBalanceItemDto> timeOffBalanceItemDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      TimeOffPolicyUser policyUser = policyUserIterator.next();
      Long policyUserId = policyUser.getId();
      TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
          .getTimeOffBreakdown(policyUserId, currentTime);
      Integer balance = timeOffBreakdownDto.getBalance();

      TimeOffBalanceItemDto timeOffBalanceItemDto = TimeOffBalanceItemDto.builder()
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
  public List<TimeOffPolicyUserDto> getTimeOffPolicyUser(User user) {

    List<TimeOffPolicyUser> policyUsers = timeOffPolicyUserRepository
        .findTimeOffPolicyUsersByUser(user);
    Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    LocalDateTime currentTime = LocalDateTime.now();
    List<TimeOffPolicyUserDto> timeOffPolicyUserDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      TimeOffPolicyUser policyUser = policyUserIterator.next();
      Long policyUserId = policyUser.getId();
      TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
          .getTimeOffBreakdown(policyUserId, currentTime);
      Integer balance = timeOffBreakdownDto.getBalance();

      TimeOffPolicyUserDto timeOffPolicyUserDto = new TimeOffPolicyUserDto(policyUser);
      timeOffPolicyUserDto.setBalance(balance);
      timeOffPolicyUserDtos.add(timeOffPolicyUserDto);
    }

    return timeOffPolicyUserDtos;
  }

  @Override
  public void createTimeOffPolicyUsers(List<TimeOffPolicyUser> timeOffPolicyUsers) {
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  private void createTimeOffPolicyUsers(List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos,
      Long policyId) {
    if (!timeOffPolicyUserPojos.isEmpty()) {
      timeOffPolicyUserRepository
          .saveAll(timeOffPolicyUserPojos.stream().map(timeOffPolicyUserPojo ->
              timeOffPolicyUserPojo.getTimeOffPolicyUser(policyId)).collect(Collectors.toList()));
    }
  }

  @Override
  public Integer getTimeOffBalanceByUserId(Long userId) {
    return timeOffPolicyUserRepository.getBalanceByUserId(userId);
  }

  @Override
  public TimeOffPolicy getTimeOffPolicyById(Long id) {
    return timeOffPolicyRepository.findById(id).get();
  }

  @Override
  public TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(Long policyId) {
    TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.findById(policyId).get();
    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        timeOffPolicyAccrualScheduleRepository.findAllByTimeOffPolicy(timeOffPolicy);

    List<AccrualScheduleMilestone> accrualScheduleMilestones = accrualScheduleMilestoneRepository
        .findByTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualSchedule.getId());

    return new TimeOffPolicyRelatedInfoDto(
        timeOffPolicy, timeOffPolicyAccrualSchedule, accrualScheduleMilestones);
  }

  @Override
  public TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      Long timeOffPolicyId, Company company) {
    Optional<TimeOffPolicy> timeOffPolicy = timeOffPolicyRepository.findById(timeOffPolicyId);

    Boolean isLimited = timeOffPolicy.get().getIsLimited();

    List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);

    List<User> selectableTimeOffPolicyUsers = userRepository.findAllByCompany(company);

    ArrayList<Long> selectedUsersIds = new ArrayList<>();

    List<TimeOffPolicyRelatedUserDto> selectedEmployees = timeOffPolicyUsers.stream().map(
        (user) -> {
          JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user.getUser());
          JobUserDto employeeWithJobInfoDto = new JobUserDto(user.getUser(), employeeWithJobInfo);
          Integer balance = user.getBalance();
          selectedUsersIds.add(user.getUser().getId());
          return new TimeOffPolicyRelatedUserDto(balance, employeeWithJobInfoDto);
        }
    ).collect(Collectors.toList());

    List<TimeOffPolicyRelatedUserDto> unselectedEmployees = selectableTimeOffPolicyUsers
        .stream().filter(user -> !selectedUsersIds.contains(user.getId()))
        .map(user -> {
          JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
          JobUserDto employeeWithJobInfoDto = new JobUserDto(user, employeeWithJobInfo);
          Integer balance = 0;
          return new TimeOffPolicyRelatedUserDto(balance, employeeWithJobInfoDto);
        }).collect(Collectors.toList());

    return new TimeOffPolicyRelatedUserListDto(
        isLimited, unselectedEmployees, selectedEmployees);
  }

  @Override
  public void updateTimeOffPolicy(TimeOffPolicy timeOffPolicy) {
    timeOffPolicyRepository.save(timeOffPolicy);
  }

  @Override
  public TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyAccrualScheduleRepository.findAllByTimeOffPolicy(timeOffPolicy);
  }

  @Override
  public void updateTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule) {
    timeOffPolicyAccrualScheduleRepository.save(timeOffPolicyAccrualSchedule);
  }

  @Override
  public List<TimeOffPolicyUser> getAllPolicyUsersByUser(User user) {
    return timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(user);
  }

  @Override
  public TimeOffPolicyUser updateTimeOffBalance(Long timeOffPolicyUserId, Integer totalHours) {
    TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserRepository.findById(timeOffPolicyUserId)
        .get();
    Integer prevBalance = timeOffPolicyUser.getBalance();
    timeOffPolicyUser.setBalance(prevBalance - totalHours);
    return timeOffPolicyUserRepository.save(timeOffPolicyUser);
  }

  @Override
  public List<TimeOffPolicyListDto> getAllPolicies(Long companyId) {
    List<TimeOffPolicyList> timeOffPolicies = timeOffPolicyRepository.getAllPolicies(companyId);
    Iterator<TimeOffPolicyList> timeOffPolicyIterator = timeOffPolicies.iterator();

    List<TimeOffPolicyListDto> timeOffPolicyListDtoList = new ArrayList<>();
    while (timeOffPolicyIterator.hasNext()) {
      TimeOffPolicyListDto timeOffPolicyListDto = new TimeOffPolicyListDto();
      TimeOffPolicyList timeOffPolicyList = timeOffPolicyIterator.next();
      BeanUtils.copyProperties(timeOffPolicyList, timeOffPolicyListDto);
      timeOffPolicyListDtoList.add(timeOffPolicyListDto);
    }

    return timeOffPolicyListDtoList;
  }

  @Override
  @Transactional
  public TimeOffPolicyAccrualSchedule updateTimeOffPolicySchedule(TimeOffPolicy timeOffPolicy,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto) {

    TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicy);

    TimeOffPolicyAccrualSchedule newTimeOffSchedule = timeOffPolicyAccrualScheduleDto
        .getTimeOffPolicyAccrualSchedule(timeOffPolicy,
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

  private boolean isScheduleChanged(TimeOffPolicyAccrualSchedule originalSchedule,
      TimeOffPolicyAccrualSchedule newSchedule) {
    return originalSchedule.getDaysBeforeAccrualStarts() != newSchedule.getDaysBeforeAccrualStarts()
        || originalSchedule.getAccrualHours() != newSchedule.getAccrualHours()
        || originalSchedule.getCarryoverLimit() != newSchedule.getCarryoverLimit()
        || originalSchedule.getMaxBalance() != newSchedule.getMaxBalance();
  }

  @Override
  public List<AccrualScheduleMilestone> updateTimeOffPolicyMilestones(
      TimeOffPolicy timeOffPolicyUpdated, List<AccrualScheduleMilestoneDto> milestones) {
    TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicyUpdated);

    if (originTimeOffSchedule == null && CollectionUtils.isEmpty(milestones)) {
      return null;
    }

    List<AccrualScheduleMilestone> accrualScheduleMilestoneList;

    if (originTimeOffSchedule == null) {
      accrualScheduleMilestoneList = new ArrayList<>();
    } else {
      accrualScheduleMilestoneList = accrualScheduleMilestoneRepository
          .findByTimeOffPolicyAccrualScheduleId(originTimeOffSchedule.getId());
    }

    Long accrualScheduleId = originTimeOffSchedule.getId();
    List<AccrualScheduleMilestone> newAccrualMilestoneList = milestones.stream()
        .map(accrualScheduleMilestoneDto -> accrualScheduleMilestoneDto
            .getAccrualScheduleMilestone(accrualScheduleId))
        .collect(Collectors.toList());

    sortMilestoneList(accrualScheduleMilestoneList);
    sortMilestoneList(newAccrualMilestoneList);

    HashMap<Integer, List<AccrualScheduleMilestone>> hashMap =
        transformMilestoneListToMap(accrualScheduleMilestoneList, newAccrualMilestoneList);

    return hashMap.entrySet()
        .stream()
        .map(Map.Entry::getValue)
        .map(this::expireAndSaveMilestones)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public void addTimeOffAdjustments(User currentUser, Long policyUserId, Integer adjustment) {
    TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserRepository.findById(policyUserId).get();

    TimeOffAdjustment timeOffAdjustment =
        new TimeOffAdjustment(timeOffPolicyUser, timeOffPolicyUser.getTimeOffPolicy(), currentUser);
    timeOffAdjustment.setAmount(adjustment);

    UserPersonalInformation userPersonalInformation = currentUser.getUserPersonalInformation();
    timeOffAdjustment.setComment("Adjusted by User " + userPersonalInformation.getName());
    timeOffAdjustmentRepository.save(timeOffAdjustment);
  }

  private void sortMilestoneList(List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    accrualScheduleMilestoneList
        .sort(Comparator.comparingInt(AccrualScheduleMilestone::getAnniversaryYear));
  }

  private HashMap<Integer, List<AccrualScheduleMilestone>> transformMilestoneListToMap(
      List<AccrualScheduleMilestone> originMilestones,
      List<AccrualScheduleMilestone> newMilestones) {
    HashMap<Integer, List<AccrualScheduleMilestone>> milestoneMap = new HashMap<>();
    for (AccrualScheduleMilestone accrualMilestone : originMilestones) {
      List<AccrualScheduleMilestone> milestoneList = new ArrayList<>(
          Collections.singletonList(accrualMilestone));
      milestoneMap.put(accrualMilestone.getAnniversaryYear(), milestoneList);
    }

    for (AccrualScheduleMilestone accrualScheduleMilestone : newMilestones) {
      List<AccrualScheduleMilestone> accrualScheduleMilestones = milestoneMap
          .get(accrualScheduleMilestone.getAnniversaryYear());
      if (accrualScheduleMilestones != null) {
        accrualScheduleMilestones.add(accrualScheduleMilestone);
      } else {
        List<AccrualScheduleMilestone> milestoneList = new ArrayList<>(
            Collections.singletonList(accrualScheduleMilestone));
        milestoneMap.put(accrualScheduleMilestone.getAnniversaryYear(), milestoneList);
      }
    }

    return milestoneMap;
  }

  private AccrualScheduleMilestone expireAndSaveMilestones(
      List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    if (accrualScheduleMilestoneList.size() == 1
        && accrualScheduleMilestoneList.get(0).getId() == null) {
      return accrualScheduleMilestoneRepository.save(accrualScheduleMilestoneList.get(0));
    } else if (accrualScheduleMilestoneList.size() == 1
        && accrualScheduleMilestoneList.get(0).getId() != null) {
      accrualScheduleMilestoneRepository.delete(accrualScheduleMilestoneList.get(0).getId());
      return null;
    }

    AccrualScheduleMilestone originalMilestone = accrualScheduleMilestoneList.get(0);
    AccrualScheduleMilestone nowMilestone = accrualScheduleMilestoneList.get(1);

    if (!isMilestoneChanged(originalMilestone, nowMilestone)) {
      return originalMilestone;
    }

    originalMilestone.setExpiredAt(new Timestamp(new Date().getTime()));
    accrualScheduleMilestoneRepository.save(originalMilestone);
    return accrualScheduleMilestoneRepository.save(nowMilestone);
  }

  private boolean isMilestoneChanged(AccrualScheduleMilestone originalMilestone,
      AccrualScheduleMilestone newMilestone) {
    return originalMilestone.getAccrualHours() != newMilestone.getAccrualHours()
        || originalMilestone.getCarryoverLimit() != newMilestone.getCarryoverLimit()
        || originalMilestone.getMaxBalance() != newMilestone.getMaxBalance();
  }

  private void createAccrualScheduleMilestones(
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList, Long scheduleId) {
    if (!accrualScheduleMilestoneDtoList.isEmpty()) {
      accrualScheduleMilestoneRepository.saveAll(
          accrualScheduleMilestoneDtoList.stream()
              .map(accrualScheduleMilestoneDto ->
                  accrualScheduleMilestoneDto.getAccrualScheduleMilestone(scheduleId))
              .collect(Collectors.toList()));
    }
  }

  @Override
  public void updateMilestones(
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList, Long scheduleId
  ) {
    List<Integer> anniversaryYears = accrualScheduleMilestoneDtoList.stream()
        .map(AccrualScheduleMilestoneDto::getAnniversaryYear)
        .collect(Collectors.toList());
    List<AccrualScheduleMilestone> all = accrualScheduleMilestoneRepository
        .findByTimeOffPolicyAccrualScheduleId(scheduleId);
    List<Integer> anniversaryYearsBefore = all.stream()
        .map(AccrualScheduleMilestone::getAnniversaryYear)
        .collect(Collectors.toList());
    all.stream().forEach(
        accrualScheduleMilestone -> {
          if (!anniversaryYears.contains(accrualScheduleMilestone.getAnniversaryYear())) {
            accrualScheduleMilestoneRepository.delete(accrualScheduleMilestone);
          }
          Optional<AccrualScheduleMilestoneDto> updateAccrualScheduleMilestoneDto =
              accrualScheduleMilestoneDtoList.stream().filter(a -> a.getAnniversaryYear()
                  == accrualScheduleMilestone.getAnniversaryYear()).findFirst();
          if (updateAccrualScheduleMilestoneDto.isPresent()) {
            AccrualScheduleMilestone updatedAccrualMileStones =
                updateAccrualScheduleMilestoneDto
                    .get().updateAccrualScheduleMilestone(accrualScheduleMilestone, scheduleId);
            accrualScheduleMilestoneRepository.save(updatedAccrualMileStones);
          }
        });
    // add completed new milestones info
    List<AccrualScheduleMilestone> newAccrualMileStones = accrualScheduleMilestoneDtoList.stream()
        .filter(milestoneDto -> !anniversaryYearsBefore.contains(milestoneDto.getAnniversaryYear()))
        .map(milestoneDto -> milestoneDto.getAccrualScheduleMilestone(scheduleId)).collect(
            Collectors.toList());
    accrualScheduleMilestoneRepository.saveAll(newAccrualMileStones);
  }

  @Override
  public void updateTimeOffPolicyUserInfo(
      List<TimeOffPolicyUserPojo> userStatBalances, Long timeOffPolicyId) {
    List<Long> newUserIds = userStatBalances.stream()
        .map(TimeOffPolicyUserPojo::getUserId)
        .collect(Collectors.toList());
    List<TimeOffPolicyUser> oldUsersStartBalanceList = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);
    List<Long> oldUserIds = oldUsersStartBalanceList.stream()
        .map(user -> user.getUser().getId())
        .collect(Collectors.toList());
    oldUsersStartBalanceList.stream()
        .forEach(
            oldUsersStartBalance -> {
              if (newUserIds.contains(oldUsersStartBalance.getUser().getId())) {
                // update
                Optional<TimeOffPolicyUserPojo> updateUserStartBalance =
                    userStatBalances.stream().filter(u -> u.getUserId()
                        == oldUsersStartBalance.getUser().getId()).findFirst();
                if (updateUserStartBalance.isPresent()) {
                  TimeOffPolicyUserPojo newUserStartBalance = updateUserStartBalance.get();
                  oldUsersStartBalance.setBalance(newUserStartBalance.getBalance());
                  timeOffPolicyUserRepository.save(oldUsersStartBalance);
                }
                return;
              }
              // delete
              timeOffPolicyUserRepository.delete(oldUsersStartBalance);
            });
    // add new user
    List<TimeOffPolicyUser> timeOffPolicyUsers = userStatBalances.stream()
        .filter(user -> !oldUserIds.contains(user.getUserId()))
        .map(statBalance -> statBalance.getTimeOffPolicyUser(timeOffPolicyId)).collect(
            Collectors.toList());
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  @Override
  public void deleteTimeOffPolicy(Long timeOffPolicyId) {
    List<TimeOffRequest> requests = timeOffRequestRepository.findByTimeOffPolicyId(timeOffPolicyId);
    requests.stream().filter(request ->
        request.getTimeOffApprovalStatus() != APPROVED
            && request.getTimeOffApprovalStatus() != DENIED)
        .forEach(request -> timeOffRequestRepository.delete(request.getId()));

    List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);
    timeOffPolicyUserRepository.delete(timeOffPolicyUsers);

    TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.getOne(timeOffPolicyId);
    TimeOffPolicyAccrualSchedule accrualSchedule = timeOffPolicyAccrualScheduleRepository
        .findAllByTimeOffPolicy(timeOffPolicy);
    timeOffPolicyAccrualScheduleRepository.delete(accrualSchedule);

    List<AccrualScheduleMilestone> milestones = accrualScheduleMilestoneRepository
        .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
    accrualScheduleMilestoneRepository.deleteAll(milestones);

    timeOffPolicyRepository.delete(timeOffPolicyId);
  }

  @Override
  public List<TimeOffPolicyUser> getAllPolicyUsersByPolicyId(Long id) {
    return timeOffPolicyUserRepository.findAllByTimeOffPolicyId(id);
  }

  @Override
  public void enrollTimeOffHours(List<TimeOffPolicyUser> policyUsers,
      TimeOffPolicy enrollPolicy, User currentUser) {
    policyUsers.stream().forEach(policyUser -> {
      TimeOffBreakdownDto timeOffBreakdown = timeOffDetailService
          .getTimeOffBreakdown(policyUser.getId(), LocalDateTime.now());

      Integer remainingBalance = timeOffBreakdown.getBalance();
      TimeOffAdjustment timeOffAdjustment = TimeOffAdjustment.builder()
          .timeOffPolicy(enrollPolicy)
          .adjusterUserId(currentUser.getId())
          .amount(remainingBalance)
          .comment(String.format("Rolled from policy %s", policyUser.getTimeOffPolicy().getName()))
          .company(policyUser.getUser().getCompany())
          .user(policyUser.getUser())
          .build();
      timeOffAdjustmentRepository.save(timeOffAdjustment);
    });
  }
}
