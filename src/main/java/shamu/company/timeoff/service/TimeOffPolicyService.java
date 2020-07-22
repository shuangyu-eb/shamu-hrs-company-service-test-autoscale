package shamu.company.timeoff.service;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffAdjustmentCheckDto;
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
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.PaidHolidayUser;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.entity.mapper.AccrualScheduleMilestoneMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyAccrualScheduleMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.exception.TimeOffExceedException;
import shamu.company.timeoff.pojo.TimeOffPolicyListPojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.PaidHolidayUserRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@Service
@Transactional
public class TimeOffPolicyService {

  private final TimeOffPolicyRepository timeOffPolicyRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  private final JobUserRepository jobUserRepository;

  private final UserRepository userRepository;

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  private final PaidHolidayUserRepository paidHolidayUserRepository;

  private final TimeOffDetailService timeOffDetailService;

  private final CompanyService companyService;

  private final AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper;

  private final TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper;

  private final TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  private final TimeOffPolicyMapper timeOffPolicyMapper;

  private final JobUserMapper jobUserMapper;

  private final UserService userService;

  private final UserAddressService userAddressService;

  @Autowired
  public TimeOffPolicyService(
      final TimeOffPolicyRepository timeOffPolicyRepository,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final JobUserRepository jobUserRepository,
      final TimeOffDetailService timeOffDetailService,
      final UserRepository userRepository,
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      final PaidHolidayUserRepository paidHolidayUserRepository,
      final AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper,
      final TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper,
      final TimeOffPolicyUserMapper timeOffPolicyUserMapper,
      final TimeOffPolicyMapper timeOffPolicyMapper,
      final JobUserMapper jobUserMapper,
      final CompanyService companyService,
      final UserService userService,
      final UserAddressService userAddressService) {
    this.timeOffPolicyRepository = timeOffPolicyRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.jobUserRepository = jobUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
    this.timeOffDetailService = timeOffDetailService;
    this.accrualScheduleMilestoneMapper = accrualScheduleMilestoneMapper;
    this.timeOffPolicyAccrualScheduleMapper = timeOffPolicyAccrualScheduleMapper;
    this.timeOffPolicyUserMapper = timeOffPolicyUserMapper;
    this.timeOffPolicyMapper = timeOffPolicyMapper;
    this.jobUserMapper = jobUserMapper;
    this.companyService = companyService;
    this.userService = userService;
    this.userAddressService = userAddressService;
  }

  public void createTimeOffPolicy(
      final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto, final String companyId) {

    final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto =
        timeOffPolicyWrapperDto.getTimeOffPolicy();
    final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto =
        timeOffPolicyWrapperDto.getTimeOffPolicyAccrualSchedule();
    final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList =
        timeOffPolicyWrapperDto.getMilestones();
    final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos =
        timeOffPolicyWrapperDto.getUserStartBalances();
    final Company company = companyService.findById(companyId);

    createTimeOffPolicy(
        timeOffPolicyFrontendDto,
        timeOffPolicyAccrualScheduleDto,
        accrualScheduleMilestoneDtoList,
        timeOffPolicyUserFrontendDtos,
        company);
  }

  private void createTimeOffPolicy(
      final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto,
      final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos,
      final Company company) {

    checkPolicyNameIsExists(timeOffPolicyFrontendDto, company.getId(), 0);
    final TimeOffPolicy timeOffPolicy =
        timeOffPolicyRepository.save(
            timeOffPolicyMapper.createFromTimeOffPolicyFrontendDtoAndCompany(
                timeOffPolicyFrontendDto, company));
    final String policyId = timeOffPolicy.getId();
    final String timeOffAccrualFrequencyId =
        timeOffPolicyAccrualScheduleDto.getTimeOffAccrualFrequencyId();

    if (timeOffPolicyFrontendDto.getIsLimited()) {
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          timeOffPolicyAccrualScheduleRepository.save(
              timeOffPolicyAccrualScheduleMapper.createTimeOffPolicyAccrualSchedule(
                  timeOffPolicyAccrualScheduleDto, timeOffPolicy, timeOffAccrualFrequencyId));

      final String scheduleId = timeOffPolicyAccrualSchedule.getId();
      createAccrualScheduleMilestones(accrualScheduleMilestoneDtoList, scheduleId);
    }

    createTimeOffPolicyUsers(timeOffPolicyUserFrontendDtos, policyId);
  }

  private void checkPolicyNameIsExists(
      final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto,
      final String companyId,
      final Integer existNumber) {
    final Integer existSamePolicyName =
        timeOffPolicyRepository.findByPolicyNameAndCompanyId(
            timeOffPolicyFrontendDto.getPolicyName(), companyId);
    if (existSamePolicyName > existNumber) {
      throw new AlreadyExistsException(
          "Time Off policy name already exists", "time off policy name");
    }
  }

  public TimeOffBalanceDto getTimeOffBalances(final String userId) {
    final User user = userService.findById(userId);

    final List<TimeOffPolicyUser> policyUsers =
        timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(user);
    final Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    boolean showTotalBalance = true;
    final LocalDateTime currentTime = LocalDateTime.now();
    final List<TimeOffBalanceItemDto> timeOffBalanceItemDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      final TimeOffPolicyUser policyUser = policyUserIterator.next();
      final String policyUserId = policyUser.getId();
      final TimeOffBreakdownDto timeOffBreakdownDto =
          timeOffDetailService.getTimeOffBreakdown(policyUserId, null);
      final Integer balance = timeOffBreakdownDto.getBalance();

      final Integer pendingHours =
          getTimeOffRequestHoursFromStatus(
              user.getId(), policyUser.getTimeOffPolicy().getId(), AWAITING_REVIEW, null, null);
      final Integer approvedHours =
          getTimeOffRequestHoursFromStatus(
              user.getId(),
              policyUser.getTimeOffPolicy().getId(),
              APPROVED,
              Timestamp.valueOf(currentTime),
              TimeOffRequestDate.Operator.MORE_THAN);

      final Integer approvalBalance = (null == balance ? null : (balance - approvedHours));
      final Integer availableBalance =
          (null == balance ? null : (balance - pendingHours - approvedHours));

      final TimeOffBalanceItemDto timeOffBalanceItemDto =
          TimeOffBalanceItemDto.builder()
              .id(policyUserId)
              .currentBalance(balance)
              .approvalBalance(approvalBalance)
              .availableBalance(availableBalance)
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

  public Integer getTimeOffRequestHoursFromStatus(
      final String userId,
      final String policyId,
      final TimeOffApprovalStatus status,
      final Timestamp currentTime,
      final TimeOffRequestDate.Operator operator) {
    final List<TimeOffRequest> timeOffRequestList =
        timeOffRequestRepository.findByTimeOffPolicyUserAndStatus(
            userId, policyId, status, currentTime, operator);

    return timeOffRequestList.stream().mapToInt(TimeOffRequest::getHours).sum();
  }

  public List<TimeOffPolicyUserDto> getTimeOffPolicyUser(
      final String userId, final Long untilDate) {

    final User user = userService.findById(userId);

    LocalDateTime endDateTime = LocalDateTime.now();

    if (untilDate != null) {
      endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(untilDate), ZoneId.of("UTC"));
    }

    final List<TimeOffPolicyUser> policyUsers =
        timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(user);
    final Iterator<TimeOffPolicyUser> policyUserIterator = policyUsers.iterator();

    final List<TimeOffPolicyUserDto> timeOffPolicyUserDtos = new ArrayList<>();
    while (policyUserIterator.hasNext()) {
      final TimeOffPolicyUser policyUser = policyUserIterator.next();
      final String policyUserId = policyUser.getId();
      final TimeOffBreakdownDto timeOffBreakdownDto =
          timeOffDetailService.getTimeOffBreakdown(policyUserId, untilDate);
      final Integer balance = timeOffBreakdownDto.getBalance();

      final Integer pendingHours =
          getTimeOffRequestHoursFromStatus(
              user.getId(), policyUser.getTimeOffPolicy().getId(), AWAITING_REVIEW, null, null);
      final Integer approvedHours =
          getTimeOffRequestHoursFromStatus(
              user.getId(),
              policyUser.getTimeOffPolicy().getId(),
              APPROVED,
              Timestamp.valueOf(endDateTime),
              TimeOffRequestDate.Operator.MORE_THAN);
      final Integer availableBalance =
          (null == balance ? null : (balance - pendingHours - approvedHours));

      final TimeOffPolicyUserDto timeOffPolicyUserDto =
          timeOffPolicyUserMapper.convertToTimeOffPolicyUserDto(policyUser);
      timeOffPolicyUserDto.setBalance(availableBalance);
      timeOffPolicyUserDtos.add(timeOffPolicyUserDto);
    }

    return timeOffPolicyUserDtos;
  }

  public void createTimeOffPolicyUsers(final List<TimeOffPolicyUser> timeOffPolicyUsers) {
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  private void createTimeOffPolicyUsers(
      final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos,
      final String policyId) {
    if (!timeOffPolicyUserFrontendDtos.isEmpty()) {
      timeOffPolicyUserRepository.saveAll(
          timeOffPolicyUserFrontendDtos.stream()
              .map(
                  timeOffPolicyUserFrontendDto ->
                      timeOffPolicyUserMapper
                          .createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(
                              timeOffPolicyUserFrontendDto, policyId))
              .collect(Collectors.toList()));
    }
  }

  public void addUserToAutoEnrolledPolicy(final String userId, final String companyId) {
    final List<TimeOffPolicy> timeOffPolicyList =
        timeOffPolicyRepository.findByCompanyIdAndIsAutoEnrollEnabledIsTrue(companyId);
    final Company company = companyService.findById(companyId);

    final List<TimeOffPolicyUser> timeOffPolicyUserList =
        timeOffPolicyList.stream()
            .map(
                timeOffPolicy ->
                    timeOffPolicyUserMapper.createFromTimeOffPolicyAndUserId(
                        timeOffPolicy, userId, timeOffPolicy.getIsLimited() ? 0 : null))
            .collect(Collectors.toList());

    if (!timeOffPolicyList.isEmpty()) {
      timeOffPolicyUserRepository.saveAll(timeOffPolicyUserList);
    }

    if (BooleanUtils.isTrue(company.getIsPaidHolidaysAutoEnroll())) {
      final PaidHolidayUser paidHolidayUser = new PaidHolidayUser(userId, true);
      paidHolidayUserRepository.save(paidHolidayUser);
    }
  }

  public Integer getTimeOffBalanceByUserAndPolicy(
      final User user, final TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(user, timeOffPolicy)
        .getInitialBalance();
  }

  public TimeOffPolicy getTimeOffPolicyById(final String id) {
    return timeOffPolicyRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Time off policy with id %s not found!", id),
                    id,
                    "time off policy"));
  }

  public TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(final String policyId) {
    final TimeOffPolicy timeOffPolicy =
        timeOffPolicyRepository
            .findById(policyId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("Time off policy with id %s not found!", policyId),
                        policyId,
                        "time off policy"));

    if (!timeOffPolicy.getIsLimited()) {
      return new TimeOffPolicyRelatedInfoDto(timeOffPolicy, null, null);
    }
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(timeOffPolicy);

    final List<AccrualScheduleMilestone> accrualScheduleMilestones =
        accrualScheduleMilestoneRepository.findByTimeOffPolicyAccrualScheduleId(
            timeOffPolicyAccrualSchedule.getId());

    return timeOffPolicyMapper
        .createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(
            timeOffPolicy, timeOffPolicyAccrualSchedule, accrualScheduleMilestones);
  }

  public TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      final String timeOffPolicyId, final String companyId) {

    final TimeOffPolicy timeOffPolicy = getTimeOffPolicyById(timeOffPolicyId);

    final List<TimeOffPolicyUser> timeOffPolicyUsers =
        timeOffPolicyUserRepository.findAllByTimeOffPolicyId(timeOffPolicyId);

    final List<User> selectableTimeOffPolicyUsers = userRepository.findAllByCompanyId(companyId);

    final ArrayList<String> selectedUsersIds = new ArrayList<>();

    final List<TimeOffPolicyRelatedUserDto> selectedEmployees =
        timeOffPolicyUsers.stream()
            .map(
                timeOffPolicyUser -> {
                  final User user = timeOffPolicyUser.getUser();

                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);

                  selectedUsersIds.add(user.getId());

                  final boolean isEmployeeAddable =
                      verifyIntegrityOfEmployeeInformation(user, employeeWithJobInfo);

                  final String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, selectableTimeOffPolicyUsers);

                  return jobUserMapper.convertToTimeOffPolicyRelatedUserDto(
                      timeOffPolicyUser,
                      employeeWithJobInfo,
                      userNameOrUserNameWithEmailAddress,
                      !isEmployeeAddable);
                })
            .collect(Collectors.toList());

    final List<TimeOffPolicyRelatedUserDto> unselectedEmployees =
        selectableTimeOffPolicyUsers.stream()
            .filter(user -> !selectedUsersIds.contains(user.getId()))
            .map(
                user -> {
                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
                  final boolean isEmployeeAddable =
                      verifyIntegrityOfEmployeeInformation(user, employeeWithJobInfo);
                  final String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, selectableTimeOffPolicyUsers);
                  return jobUserMapper.convertToTimeOffPolicyRelatedUserDto(
                      user,
                      employeeWithJobInfo,
                      userNameOrUserNameWithEmailAddress,
                      !isEmployeeAddable);
                })
            .collect(Collectors.toList());

    return new TimeOffPolicyRelatedUserListDto(
        timeOffPolicy.getIsLimited(), unselectedEmployees, selectedEmployees);
  }

  public List<TimeOffPolicyRelatedUserDto> getEmployeesOfNewPolicyOrPaidHoliday() {
    final List<User> allUsers = userRepository.findAllActiveUsers();
    return allUsers.stream()
        .map(
            user -> {
              final JobUser jobUser = jobUserRepository.findJobUserByUser(user);
              final boolean isEmployeeAddable = verifyIntegrityOfEmployeeInformation(user, jobUser);
              final String userNameOrUserNameWithEmailAddress =
                  userService.getUserNameInUsers(user, allUsers);
              return jobUserMapper.convertToTimeOffPolicyRelatedUserDto(
                  user, jobUser, userNameOrUserNameWithEmailAddress, !isEmployeeAddable);
            })
        .collect(Collectors.toList());
  }

  public boolean verifyIntegrityOfEmployeeInformation(
      final User user, final JobUser employeeWithJobInfo) {
    final UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
    final UserAddress userAddress = userAddressService.findUserAddressByUserId(user.getId());
    return userPersonalInformation.getBirthDate() != null
        && userPersonalInformation.getSsn() != null
        && userAddress.getCountry() != null
        && userAddress.getStateProvince() != null
        && userAddress.getCity() != null
        && userAddress.getStreet1() != null
        && employeeWithJobInfo.getEmployeeType() != null
        && employeeWithJobInfo.getStartDate() != null
        && employeeWithJobInfo.getOffice() != null
        && employeeWithJobInfo.getUserCompensation() != null;
  }

  public boolean checkIsPolicyCalculationRelatedToHireDate(
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule) {
    return timeOffPolicyAccrualSchedule.getDaysBeforeAccrualStarts() != null
        || !AccrualFrequencyType.FREQUENCY_TYPE_ONE
            .getValue()
            .equals(timeOffPolicyAccrualSchedule.getTimeOffAccrualFrequency().getName())
        || !accrualScheduleMilestoneRepository
            .findByTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualSchedule.getId())
            .isEmpty();
  }

  public void updateTimeOffPolicy(
      final String id, final TimeOffPolicyWrapperDto infoWrapper, final String companyId) {

    final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto = infoWrapper.getTimeOffPolicy();
    final TimeOffPolicy origin = getTimeOffPolicyById(id);

    checkPolicyNameIsExists(timeOffPolicyFrontendDto, companyId, 1);

    timeOffPolicyMapper.updateFromTimeOffPolicyFrontendDto(origin, timeOffPolicyFrontendDto);

    updateTimeOffPolicy(origin);

    if (BooleanUtils.isTrue(origin.getIsLimited())) {
      updateTimeOffPolicyMilestones(origin, infoWrapper.getMilestones());

      updateTimeOffPolicySchedule(origin, infoWrapper.getTimeOffPolicyAccrualSchedule());
    }

    updateTimeOffPolicyUserInfo(infoWrapper, id);
  }

  private void updateTimeOffPolicy(final TimeOffPolicy timeOffPolicy) {
    timeOffPolicyRepository.save(timeOffPolicy);
  }

  private TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      final TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(timeOffPolicy);
  }

  public List<TimeOffPolicyListDto> getAllPolicies(final String companyId) {
    final List<TimeOffPolicyListPojo> timeOffPolicies =
        timeOffPolicyRepository.getAllPolicies(companyId);
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

  private TimeOffPolicyAccrualSchedule updateTimeOffPolicySchedule(
      final TimeOffPolicy timeOffPolicy,
      final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto) {

    final TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicy);

    TimeOffPolicyAccrualSchedule newTimeOffSchedule =
        timeOffPolicyAccrualScheduleMapper.createTimeOffPolicyAccrualSchedule(
            timeOffPolicyAccrualScheduleDto,
            timeOffPolicy,
            timeOffPolicyAccrualScheduleDto.getTimeOffAccrualFrequencyId());

    if (!isScheduleChanged(originTimeOffSchedule, newTimeOffSchedule)) {
      return originTimeOffSchedule;
    }

    originTimeOffSchedule.setExpiredAt(DateUtil.getToday());
    timeOffPolicyAccrualScheduleRepository.save(originTimeOffSchedule);

    newTimeOffSchedule = timeOffPolicyAccrualScheduleRepository.save(newTimeOffSchedule);

    accrualScheduleMilestoneRepository.updateMilestoneSchedule(
        originTimeOffSchedule.getId(), newTimeOffSchedule.getId());

    return newTimeOffSchedule;
  }

  private boolean isScheduleChanged(
      final TimeOffPolicyAccrualSchedule originalSchedule,
      final TimeOffPolicyAccrualSchedule newSchedule) {
    return !(Objects.equals(
            originalSchedule.getDaysBeforeAccrualStarts(), newSchedule.getDaysBeforeAccrualStarts())
        && Objects.equals(originalSchedule.getAccrualHours(), newSchedule.getAccrualHours())
        && Objects.equals(originalSchedule.getCarryoverLimit(), newSchedule.getCarryoverLimit())
        && Objects.equals(originalSchedule.getMaxBalance(), newSchedule.getMaxBalance())
        && Objects.equals(
            originalSchedule.getTimeOffAccrualFrequency().getId(),
            newSchedule.getTimeOffAccrualFrequency().getId()));
  }

  public List<AccrualScheduleMilestone> updateTimeOffPolicyMilestones(
      final TimeOffPolicy timeOffPolicyUpdated,
      final List<AccrualScheduleMilestoneDto> milestones) {
    final TimeOffPolicyAccrualSchedule originTimeOffSchedule =
        getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicyUpdated);

    final List<AccrualScheduleMilestone> accrualScheduleMilestoneList;

    accrualScheduleMilestoneList =
        accrualScheduleMilestoneRepository.findByTimeOffPolicyAccrualScheduleId(
            originTimeOffSchedule.getId());

    if (CollectionUtils.isEmpty(milestones)
        && CollectionUtils.isEmpty(accrualScheduleMilestoneList)) {
      return new LinkedList<>();
    }

    final String accrualScheduleId = originTimeOffSchedule.getId();
    final List<AccrualScheduleMilestone> newAccrualMilestoneList =
        milestones.stream()
            .map(
                accrualScheduleMilestoneDto ->
                    accrualScheduleMilestoneMapper
                        .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                            accrualScheduleMilestoneDto, accrualScheduleId))
            .collect(Collectors.toList());

    sortMilestoneList(accrualScheduleMilestoneList);
    sortMilestoneList(newAccrualMilestoneList);

    final HashMap<Integer, List<AccrualScheduleMilestone>> hashMap =
        transformMilestoneListToMap(accrualScheduleMilestoneList, newAccrualMilestoneList);

    return hashMap.values().stream()
        .map(this::expireAndSaveMilestones)
        .collect(Collectors.toList());
  }

  public void addTimeOffAdjustments(
      final User currentUser, final String policyUserId, final Integer newBalance) {

    final TimeOffAdjustmentCheckDto checkResult =
        timeOffDetailService.checkTimeOffAdjustments(policyUserId, newBalance);
    if (checkResult.getExceed()) {
      throw new TimeOffExceedException(
          String.format("Amount exceeds max balance of %s hours.", checkResult.getMaxBalance()),
          checkResult.getMaxBalance().toString());
    }

    final TimeOffPolicyUser timeOffPolicyUser =
        timeOffPolicyUserRepository
            .findById(policyUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("Time off policy user with id %s not found!", policyUserId),
                        policyUserId,
                        "time off policy user"));

    final TimeOffBreakdownDto timeOffBreakdownDto =
        timeOffDetailService.getTimeOffBreakdown(policyUserId, null);
    final Integer currentBalance = timeOffBreakdownDto.getBalance();
    final Integer adjustment = newBalance - currentBalance;
    final TimeOffAdjustment timeOffAdjustment =
        new TimeOffAdjustment(timeOffPolicyUser, timeOffPolicyUser.getTimeOffPolicy(), currentUser);
    timeOffAdjustment.setAmount(adjustment);

    final String adjusterName = currentUser.getUserPersonalInformation().getName();
    timeOffAdjustment.setComment("Adjusted by User " + adjusterName);
    timeOffAdjustmentRepository.save(timeOffAdjustment);
  }

  private void sortMilestoneList(
      final List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    accrualScheduleMilestoneList.sort(
        Comparator.comparingInt(AccrualScheduleMilestone::getAnniversaryYear));
  }

  private HashMap<Integer, List<AccrualScheduleMilestone>> transformMilestoneListToMap(
      final List<AccrualScheduleMilestone> originMilestones,
      final List<AccrualScheduleMilestone> newMilestones) {
    final HashMap<Integer, List<AccrualScheduleMilestone>> milestoneMap = new HashMap<>();
    for (final AccrualScheduleMilestone accrualMilestone : originMilestones) {
      final List<AccrualScheduleMilestone> milestoneList =
          new ArrayList<>(Collections.singletonList(accrualMilestone));
      milestoneMap.put(accrualMilestone.getAnniversaryYear(), milestoneList);
    }

    for (final AccrualScheduleMilestone accrualScheduleMilestone : newMilestones) {
      final List<AccrualScheduleMilestone> accrualScheduleMilestones =
          milestoneMap.get(accrualScheduleMilestone.getAnniversaryYear());
      if (accrualScheduleMilestones != null) {
        accrualScheduleMilestones.add(accrualScheduleMilestone);
      } else {
        final List<AccrualScheduleMilestone> milestoneList =
            new ArrayList<>(Collections.singletonList(accrualScheduleMilestone));
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

    originalMilestone.setExpiredAt(DateUtil.getToday());
    accrualScheduleMilestoneRepository.save(originalMilestone);
    return accrualScheduleMilestoneRepository.save(nowMilestone);
  }

  private boolean isMilestoneChanged(
      final AccrualScheduleMilestone originalMilestone,
      final AccrualScheduleMilestone newMilestone) {
    return !(Objects.equals(originalMilestone.getAccrualHours(), newMilestone.getAccrualHours())
        && Objects.equals(originalMilestone.getCarryoverLimit(), newMilestone.getCarryoverLimit())
        && Objects.equals(originalMilestone.getMaxBalance(), newMilestone.getMaxBalance()));
  }

  private void createAccrualScheduleMilestones(
      final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      final String scheduleId) {
    if (!accrualScheduleMilestoneDtoList.isEmpty()) {
      accrualScheduleMilestoneRepository.saveAll(
          accrualScheduleMilestoneDtoList.stream()
              .map(
                  accrualScheduleMilestoneDto ->
                      accrualScheduleMilestoneMapper
                          .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                              accrualScheduleMilestoneDto, scheduleId))
              .collect(Collectors.toList()));
    }
  }

  public void updateTimeOffPolicyUserInfo(
      final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto, final String timeOffPolicyId) {
    final List<TimeOffPolicyUserFrontendDto> userStartBalances =
        timeOffPolicyWrapperDto.getUserStartBalances();
    final List<String> newUserIds =
        userStartBalances.stream()
            .map(TimeOffPolicyUserFrontendDto::getUserId)
            .collect(Collectors.toList());
    final List<TimeOffPolicyUser> oldUsersStartBalanceList =
        timeOffPolicyUserRepository.findAllByTimeOffPolicyId(timeOffPolicyId);
    final List<String> oldUserIds =
        oldUsersStartBalanceList.stream()
            .map(user -> user.getUser().getId())
            .collect(Collectors.toList());
    oldUsersStartBalanceList.forEach(
        oldUsersStartBalance -> {
          if (newUserIds.contains(oldUsersStartBalance.getUser().getId())) {
            // update
            final Optional<TimeOffPolicyUserFrontendDto> updateUserStartBalance =
                userStartBalances.stream()
                    .filter(u -> u.getUserId().equals(oldUsersStartBalance.getUser().getId()))
                    .findFirst();
            if (updateUserStartBalance.isPresent()) {
              final TimeOffPolicyUserFrontendDto newUserStartBalance = updateUserStartBalance.get();
              oldUsersStartBalance.setInitialBalance(newUserStartBalance.getBalance());
              timeOffPolicyUserRepository.save(oldUsersStartBalance);
            }
            return;
          }
          // delete
          timeOffPolicyUserRepository.delete(oldUsersStartBalance);
        });
    // add new user
    final List<TimeOffPolicyUser> timeOffPolicyUsers =
        userStartBalances.stream()
            .filter(user -> !oldUserIds.contains(user.getUserId()))
            .map(
                statBalance ->
                    timeOffPolicyUserMapper
                        .createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(
                            statBalance, timeOffPolicyId))
            .collect(Collectors.toList());
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  public void deleteTimeOffPolicy(final String timeOffPolicyId) {
    timeOffPolicyRepository.delete(timeOffPolicyId);
  }

  public List<TimeOffPolicyUser> getAllPolicyUsersByPolicyId(final String id) {
    return timeOffPolicyUserRepository.findAllByTimeOffPolicyId(id);
  }

  public void enrollTimeOffHours(
      final String policyId, final String rollId, final String currentUserId) {

    final List<TimeOffPolicyUser> policyUsers = getAllPolicyUsersByPolicyId(policyId);

    final TimeOffPolicy enrollPolicy = getTimeOffPolicyById(rollId);

    policyUsers.forEach(
        policyUser -> {
          final TimeOffPolicyUser timeOffPolicyUser =
              timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
                  policyUser.getUser(), enrollPolicy);

          if (timeOffPolicyUser == null) {
            final TimeOffPolicyUser newPolicyUserRecord =
                new TimeOffPolicyUser(policyUser.getUser(), enrollPolicy, 0);
            timeOffPolicyUserRepository.save(newPolicyUserRecord);
          }

          final TimeOffBreakdownDto timeOffBreakdown =
              timeOffDetailService.getTimeOffBreakdown(policyUser.getId(), null);

          final Integer remainingBalance = timeOffBreakdown.getBalance();
          final TimeOffAdjustment timeOffAdjustment =
              TimeOffAdjustment.builder()
                  .timeOffPolicy(enrollPolicy)
                  .adjusterUserId(currentUserId)
                  .amount(remainingBalance)
                  .comment(
                      String.format(
                          "Rolled from policy %s", policyUser.getTimeOffPolicy().getName()))
                  .user(policyUser.getUser())
                  .build();
          timeOffAdjustmentRepository.save(timeOffAdjustment);
        });
  }

  public boolean checkHasTimeOffPolicies(final String id) {
    return timeOffPolicyUserRepository.existsByUserId(id);
  }
}
