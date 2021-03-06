package shamu.company.user.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.utils.DateUtil;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static shamu.company.attendance.entity.OvertimePolicy.ELIGIBLE_POLICY_NAME;
import static shamu.company.attendance.entity.OvertimePolicy.NOT_ELIGIBLE_POLICY_NAME;

@Service
@Transactional
public class UserCompensationService {

  private static final String HOURLY_PAY_TYPE = "Hourly";

  private final UserCompensationRepository userCompensationRepository;

  private final UserCompensationMapper userCompensationMapper;

  private final CompensationFrequencyService compensationFrequencyService;

  private final TimeSheetService timeSheetService;

  private final OvertimePolicyRepository overtimePolicyRepository;

  private final JobUserRepository jobUserRepository;

  private final TimePeriodService timePeriodService;

  @Autowired
  public UserCompensationService(
      final UserCompensationRepository userCompensationRepository,
      final UserCompensationMapper userCompensationMapper,
      final CompensationFrequencyService compensationFrequencyService,
      final TimeSheetService timeSheetService,
      final OvertimePolicyRepository overtimePolicyRepository,
      final JobUserRepository jobUserRepository,
      final TimePeriodService timePeriodService) {
    this.userCompensationRepository = userCompensationRepository;
    this.userCompensationMapper = userCompensationMapper;
    this.compensationFrequencyService = compensationFrequencyService;
    this.timeSheetService = timeSheetService;
    this.overtimePolicyRepository = overtimePolicyRepository;
    this.jobUserRepository = jobUserRepository;
    this.timePeriodService = timePeriodService;
  }

  public UserCompensation save(final UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
  }

  public void deleteAll(final List<UserCompensation> userCompensations) {
    userCompensationRepository.deleteAll(userCompensations);
  }

  public void delete(final UserCompensation userCompensation) {
    userCompensationRepository.delete(userCompensation);
  }

  public UserCompensation findCompensationById(final String compensationId) {
    return userCompensationRepository
        .findById(compensationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Compensation with id %s not found!", compensationId),
                    compensationId,
                    "compensation"));
  }

  public CompensationDto findCompensationByUserId(final String userId) {
    return userCompensationMapper.convertToCompensationDto(
        userCompensationRepository.findByUserId(userId));
  }

  public List<UserCompensation> listNewestEnrolledCompensation() {
    return userCompensationRepository.listNewestEnrolledUserCompensationByCompanyId();
  }

  public boolean existsByUserId(final String userId) {
    return userCompensationRepository.existsByUserId(userId);
  }

  public UserCompensation findByUserId(final String userId) {
    return userCompensationRepository.findByUserId(userId);
  }

  public List<UserCompensation> saveAll(final List<UserCompensation> userCompensationList) {
    return userCompensationRepository.saveAll(userCompensationList);
  }

  public void removeUsersFromAttendance(final List<String> userIds) {
    final List<UserCompensation> activeUserCompensationList =
        userCompensationRepository.findActiveByUserIdIn(userIds);
    final Timestamp nowTime = DateUtil.getCurrentTime();
    activeUserCompensationList.forEach(
        activeUserCompensation -> {
          activeUserCompensation.setEndDate(nowTime);
          save(activeUserCompensation);
          final Optional<UserCompensation> futureUserCompensation =
              findNewestFutureUserCompensation(activeUserCompensation.getUserId());
          final UserCompensation newUserCompensation =
              (futureUserCompensation
                  .map(this::createNonTaCompensationFromTaCompensation)
                  .orElseGet(
                      () -> createNonTaCompensationFromTaCompensation(activeUserCompensation)));
          deleteFutureUserCompensations(activeUserCompensation.getUserId());
          final JobUser jobUser =
              jobUserRepository.findByUserId(activeUserCompensation.getUserId());
          jobUser.setUserCompensation(newUserCompensation);
          jobUserRepository.save(jobUser);
        });
  }

  private Optional<UserCompensation> findNewestFutureUserCompensation(final String userId) {
    return userCompensationRepository.findNewestFutureUserCompensation(userId);
  }

  private List<UserCompensation> findAllFutureCompansations(final String userId) {
    return userCompensationRepository.findAllFutureCompensations(userId);
  }

  private void deleteFutureUserCompensations(final String userId) {
    final List<UserCompensation> futureCompensations = findAllFutureCompansations(userId);
    deleteAll(futureCompensations);
  }

  private UserCompensation createNonTaCompensationFromTaCompensation(
      final UserCompensation taComp) {
    final UserCompensation nonTaCompensation = new UserCompensation();
    BeanUtils.copyProperties(taComp, nonTaCompensation);
    nonTaCompensation.setId(null);
    nonTaCompensation.setStartDate(null);
    nonTaCompensation.setEndDate(null);
    nonTaCompensation.setOvertimePolicy(null);
    return save(nonTaCompensation);
  }

  @Transactional
  public void updateByEditEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList) {
    final ArrayList<UserCompensation> newCompensations = new ArrayList<>();
    for (final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto : overtimeDetailsDtoList) {
      final UserCompensation currentCompensation =
          findCurrentByUserId(employeeOvertimeDetailsDto.getEmployeeId());

      if (!compensationOrPolicyChanged(employeeOvertimeDetailsDto)) {
        continue;
      }

      final Date startDate = new Date();
      final UserCompensation newCompensation =
          assembleFromEmployeeOvertimeDetailsDto(
              new UserCompensation(), employeeOvertimeDetailsDto, startDate);
      final List<UserCompensation> futureCompensations =
          findAllFutureCompensations(employeeOvertimeDetailsDto.getEmployeeId());
      if (currentCompensation != null) {
        currentCompensation.setEndDate(new Timestamp(startDate.getTime()));
      }
      newCompensations.add(newCompensation);
      save(newCompensation);
      timeSheetService.updateCurrentOvertimePolicyByUser(newCompensation);
      final JobUser jobUser = jobUserRepository.findByUserId(newCompensation.getUserId());
      jobUser.setUserCompensation(newCompensation);
      deleteAll(futureCompensations);
    }
  }

  private boolean compensationOrPolicyChanged(
      final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto) {
    final UserCompensation userCompensation =
        userCompensationRepository.findStartNumberNLatestByUserId(
            0, employeeOvertimeDetailsDto.getEmployeeId());
    return userCompensation == null
        || compensationPaymentChanged(
            userCompensation,
            employeeOvertimeDetailsDto.getRegularPay(),
            employeeOvertimeDetailsDto.getCompensationUnit())
        || overtimePolicyChanged(employeeOvertimeDetailsDto, userCompensation);
  }

  private boolean overtimePolicyChanged(
      final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto,
      final UserCompensation userCompensation) {
    if (userCompensation.getOvertimePolicy() == null) {
      return employeeOvertimeDetailsDto.getOvertimePolicy() != null;
    } else {
      return !employeeOvertimeDetailsDto
          .getOvertimePolicy()
          .equals(userCompensation.getOvertimePolicy().getPolicyName());
    }
  }

  private List<UserCompensation> findAllFutureCompensations(final String employeeId) {
    return userCompensationRepository.findAllFutureCompensations(employeeId);
  }

  private UserCompensation findCurrentByUserId(final String employeeId) {
    return userCompensationRepository.findCurrentByUserId(employeeId);
  }

  public List<UserCompensation> updateByCreateEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList, final Date startDate) {
    final List<UserCompensation> userCompensationList =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  UserCompensation userCompensation =
                      findByUserId(employeeOvertimeDetailsDto.getEmployeeId());
                  return assembleFromEmployeeOvertimeDetailsDto(
                      userCompensation == null ? new UserCompensation() : userCompensation,
                      employeeOvertimeDetailsDto,
                      startDate);
                })
            .collect(Collectors.toList());
    final List<UserCompensation> savedCompensations = saveAll(userCompensationList);
    updateUserCompensations(savedCompensations);
    return savedCompensations;
  }

  private void updateUserCompensations(final List<UserCompensation> compensations) {
    final ArrayList<JobUser> jobUsers = new ArrayList<>();
    for (final UserCompensation compensation : compensations) {
      final JobUser user = jobUserRepository.findByUserId(compensation.getUserId());
      user.setUserCompensation(compensation);
      jobUsers.add(user);
    }
    jobUserRepository.saveAll(jobUsers);
  }

  private UserCompensation assembleFromEmployeeOvertimeDetailsDto(
      final UserCompensation userCompensation,
      final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto,
      final Date startDate) {
    final BigInteger wageCents =
        userCompensationMapper.updateCompensationCents(employeeOvertimeDetailsDto.getRegularPay());
    final CompensationFrequency compensationFrequency =
        compensationFrequencyService.findById(employeeOvertimeDetailsDto.getCompensationUnit());
    final Optional<OvertimePolicy> overtimePolicy =
        overtimePolicyRepository.findAllActive().stream()
            .filter(
                savedPolicy ->
                    employeeOvertimeDetailsDto
                        .getOvertimePolicy()
                        .equals(savedPolicy.getPolicyName()))
            .findFirst();

    final Timestamp nowTime = new Timestamp(startDate.getTime());

    userCompensation.setUserId(employeeOvertimeDetailsDto.getEmployeeId());
    userCompensation.setCompensationFrequency(compensationFrequency);
    overtimePolicy.ifPresent(userCompensation::setOvertimePolicy);
    userCompensation.setWageCents(wageCents);
    userCompensation.setStartDate(nowTime);
    return userCompensation;
  }

  @Transactional
  public void updateByEditOvertimePolicyDetails(
      final String oldPolicyId, final OvertimePolicy newPolicy) {
    final List<UserCompensation> currentUserCompensations =
        userCompensationRepository.findCurrentByOvertimePolicyId(oldPolicyId);
    final List<UserCompensation> futureUserCompensations =
        userCompensationRepository.findFutureByOvertimePolicyId(oldPolicyId);
    final List<OldAndNewCompensation> oldAndNewCompensations = new ArrayList<>();
    currentUserCompensations.forEach(
        oldUserCompensation -> {
          final UserCompensation newUserCompensation = new UserCompensation();
          BeanUtils.copyProperties(oldUserCompensation, newUserCompensation);

          newUserCompensation.setOvertimePolicy(newPolicy);

          oldAndNewCompensations.add(
              new OldAndNewCompensation(oldUserCompensation, newUserCompensation));
        });
    updateCompensationsByAttendance(oldAndNewCompensations);
    for (final UserCompensation userCompensation : futureUserCompensations) {
      userCompensation.setOvertimePolicy(newPolicy);
    }
  }

  private List<UserCompensation> updateCompensationsByAttendance(
      final List<OldAndNewCompensation> oldAndNewCompensationList) {
    final List<Timesheet> timesheets = new ArrayList<>();
    final List<JobUser> jobUsers = new ArrayList<>();

    final Timestamp nowTime = new Timestamp(new Date().getTime());

    final List<UserCompensation> newUserCompensationList = new ArrayList<>();
    final List<UserCompensation> oldUserCompensationList = new ArrayList<>();

    oldAndNewCompensationList.forEach(
        oldAndNewCompensation -> {
          final UserCompensation newUserCompensation = oldAndNewCompensation.getNewCompensation();
          final UserCompensation oldUserCompensation = oldAndNewCompensation.getOldCompensation();

          newUserCompensation.setId(null);
          newUserCompensation.setStartDate(nowTime);
          newUserCompensation.setEndDate(oldUserCompensation.getEndDate());
          final UserCompensation savedNewUserCompensation = save(newUserCompensation);
          newUserCompensationList.add(savedNewUserCompensation);

          oldUserCompensation.setEndDate(nowTime);
          oldUserCompensationList.add(oldUserCompensation);

          final Timesheet timesheet =
              timeSheetService.findCurrentByUseCompensation(oldUserCompensation);

          if (timesheet != null) {
            timesheet.setUserCompensation(savedNewUserCompensation);
            timesheets.add(timesheet);
          }

          final JobUser jobUser = jobUserRepository.findByUserId(oldUserCompensation.getUserId());
          jobUser.setUserCompensation(savedNewUserCompensation);
        });

    jobUserRepository.saveAll(jobUsers);
    timeSheetService.saveAll(timesheets);
    saveAll(oldUserCompensationList);
    return newUserCompensationList;
  }

  private boolean enrolledInTa(final UserCompensation compensation) {
    final Timestamp startDate = compensation.getStartDate();
    final Timestamp endDate = compensation.getEndDate();
    final Timestamp nowTime = DateUtil.getCurrentTime();
    final boolean isRemovedFromStartedPeriod = endDate != null && endDate.before(nowTime);
    final boolean isNotEnrolled =
        isRemovedFromStartedPeriod || (startDate == null && endDate == null);
    return !isNotEnrolled;
  }

  public UserCompensation updateCompensationPaymentFromJobUser(
      final String userId, final JobUpdateDto jobUpdateDto) {
    final UserCompensation compensation =
        userCompensationRepository.findStartNumberNLatestByUserId(0, userId);

    if (!compensationPaymentChanged(
            compensation,
            jobUpdateDto.getCompensationWage(),
            jobUpdateDto.getCompensationFrequencyId())
        && !compensationPayTypeChanged(compensation, jobUpdateDto.getPayTypeName())) {
      return compensation;
    }

    final boolean isCreateCompensation = compensation == null;
    if (isCreateCompensation) {
      return createNewNotEnrolledCompensation(jobUpdateDto, userId);
    }
    final Timestamp endDate = compensation.getEndDate();
    final Timestamp nowTime = DateUtil.getCurrentTime();
    final boolean isRemovedFromStartedPeriod = endDate != null && endDate.before(nowTime);

    if (!enrolledInTa(compensation)) {
      if (isRemovedFromStartedPeriod) {
        return createNewNotEnrolledCompensation(jobUpdateDto, userId);
      }

      setPolicyToCompensation(jobUpdateDto, compensation);

      return save(
          assembleFromCompensationPayment(
              compensation,
              jobUpdateDto.getCompensationWage(),
              jobUpdateDto.getCompensationFrequencyId()));
    } else {
      return updateEnrolledCompensationPayment(compensation, jobUpdateDto, userId, nowTime);
    }
  }

  private void setPolicyToCompensation(
      final JobUpdateDto jobUpdateDto, final UserCompensation compensation) {
    if (NOT_ELIGIBLE_POLICY_NAME.equals(jobUpdateDto.getPayTypeName())) {
      final OvertimePolicy overtimePolicy =
          overtimePolicyRepository.findByPolicyName(NOT_ELIGIBLE_POLICY_NAME);
      compensation.setOvertimePolicy(overtimePolicy);
    } else if (compensation.getOvertimePolicy() != null
        && NOT_ELIGIBLE_POLICY_NAME.equals(compensation.getOvertimePolicy().getPolicyName())
        && !enrolledInTa(compensation)) {
      compensation.setOvertimePolicy(null);
    }
  }

  private boolean compensationPaymentChanged(
      final UserCompensation oldCompensation,
      final Double regularPay,
      final String compensationUnitId) {
    return oldCompensation == null
        || !compensationUnitId.equals(oldCompensation.getCompensationFrequency().getId())
        || !userCompensationMapper
            .updateCompensationCents(regularPay)
            .equals(oldCompensation.getWageCents());
  }

  private boolean compensationPayTypeChanged(
      final UserCompensation oldCompensation, final String payTypeName) {
    if (oldCompensation.getOvertimePolicy() != null
        && oldCompensation.getOvertimePolicy().getPolicyName().equals(NOT_ELIGIBLE_POLICY_NAME)) {
      return !payTypeName.equals(NOT_ELIGIBLE_POLICY_NAME);
    }
    if (oldCompensation.getCompensationFrequency() != null) {
      if (oldCompensation
          .getCompensationFrequency()
          .getName()
          .equals(CompensationFrequency.Frequency.PER_HOUR.getValue())) {
        return !payTypeName.equals(HOURLY_PAY_TYPE);
      } else {
        return !payTypeName.equals(ELIGIBLE_POLICY_NAME);
      }
    }
    return true;
  }

  private UserCompensation updateEnrolledCompensationPayment(
      final UserCompensation compensation,
      final JobUpdateDto jobUpdateDto,
      final String userId,
      final Timestamp nowTime) {

    final Double regularPay = jobUpdateDto.getCompensationWage();
    final String compensationUnitId = jobUpdateDto.getCompensationFrequencyId();
    final TimePeriod userCurrentPeriod = timePeriodService.findUserCurrentPeriod(userId).get();
    final boolean periodStarted = nowTime.after(userCurrentPeriod.getStartDate());

    if (periodStarted && nowTime.after(compensation.getStartDate())) {
      final UserCompensation newCompensation = new UserCompensation();
      BeanUtils.copyProperties(compensation, newCompensation);
      setPolicyToCompensation(jobUpdateDto, newCompensation);
      return closeOldCreateNewAndDeleteFutureCompensations(
          compensation,
          assembleFromCompensationPayment(newCompensation, regularPay, compensationUnitId),
          userCurrentPeriod.getEndDate());
    }

    setPolicyToCompensation(jobUpdateDto, compensation);
    return save(assembleFromCompensationPayment(compensation, regularPay, compensationUnitId));
  }

  private UserCompensation assembleFromCompensationPayment(
      final UserCompensation userCompensation,
      final Double regularPay,
      final String compensationUnitId) {
    final BigInteger wageCents = userCompensationMapper.updateCompensationCents(regularPay);
    final CompensationFrequency compensationFrequency =
        compensationFrequencyService.findById(compensationUnitId);

    userCompensation.setWageCents(wageCents);
    userCompensation.setCompensationFrequency(compensationFrequency);

    return userCompensation;
  }

  private UserCompensation closeOldCreateNewAndDeleteFutureCompensations(
      final UserCompensation oldCompensation,
      final UserCompensation newCompensation,
      final Timestamp timestamp) {
    oldCompensation.setEndDate(timestamp);
    newCompensation.setId(null);
    newCompensation.setStartDate(timestamp);
    newCompensation.setEndDate(null);
    save(oldCompensation);
    deleteFutureUserCompensations(oldCompensation.getUserId());
    return save(newCompensation);
  }

  private UserCompensation createNewNotEnrolledCompensation(
      final JobUpdateDto jobUpdateDto, final String userId) {
    final UserCompensation compensation = new UserCompensation();
    final String payTypeName = jobUpdateDto.getPayTypeName();
    if (NOT_ELIGIBLE_POLICY_NAME.equals(payTypeName)) {
      final OvertimePolicy overtimePolicy =
          overtimePolicyRepository.findByPolicyName(NOT_ELIGIBLE_POLICY_NAME);
      compensation.setOvertimePolicy(overtimePolicy);
    }

    userCompensationMapper.updateFromJobUpdateDto(compensation, jobUpdateDto);
    compensation.setUserId(userId);
    return save(compensation);
  }

  private class OldAndNewCompensation {
    UserCompensation oldCompensation;
    UserCompensation newCompensation;

    OldAndNewCompensation() {}

    OldAndNewCompensation(
        final UserCompensation oldCompensation, final UserCompensation newCompensation) {
      this.newCompensation = newCompensation;
      this.oldCompensation = oldCompensation;
    }

    UserCompensation getOldCompensation() {
      return oldCompensation;
    }

    UserCompensation getNewCompensation() {
      return newCompensation;
    }

    void setOldCompensation(final UserCompensation oldCompensation) {
      this.oldCompensation = oldCompensation;
    }

    void setNewCompensation(final UserCompensation newCompensation) {
      this.newCompensation = newCompensation;
    }
  }
}
