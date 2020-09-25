package shamu.company.user.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.employee.dto.CompensationDto;
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

@Service
@Transactional
public class UserCompensationService {

  private final UserCompensationRepository userCompensationRepository;

  private final UserCompensationMapper userCompensationMapper;

  private final CompensationFrequencyService compensationFrequencyService;

  private final TimeSheetService timeSheetService;

  private final OvertimePolicyRepository overtimePolicyRepository;

  private final JobUserRepository jobUserRepository;

  @Autowired
  public UserCompensationService(
      final UserCompensationRepository userCompensationRepository,
      final UserCompensationMapper userCompensationMapper,
      final CompensationFrequencyService compensationFrequencyService,
      final TimeSheetService timeSheetService,
      final OvertimePolicyRepository overtimePolicyRepository,
      final JobUserRepository jobUserRepository) {
    this.userCompensationRepository = userCompensationRepository;
    this.userCompensationMapper = userCompensationMapper;
    this.compensationFrequencyService = compensationFrequencyService;
    this.timeSheetService = timeSheetService;
    this.overtimePolicyRepository = overtimePolicyRepository;
    this.jobUserRepository = jobUserRepository;
  }

  public UserCompensation save(final UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
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
    final List<UserCompensation> userCompensationList =
        userCompensationRepository.findByUserIdIn(userIds);
    final Timestamp nowTime = DateUtil.getCurrentTime();
    userCompensationList.forEach(userCompensation -> userCompensation.setEndDate(nowTime));
    saveAll(userCompensationList);
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
                      userCompensation, employeeOvertimeDetailsDto, startDate);
                })
            .collect(Collectors.toList());

    return saveAll(userCompensationList);
  }

  public List<UserCompensation> updateByEditEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList,
      final boolean isAddAttendanceEmployees) {
    final List<OldAndNewCompensation> oldAndNewCompensations =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  UserCompensation oldCompensation =
                      findByUserId(employeeOvertimeDetailsDto.getEmployeeId());
                  UserCompensation newCompensation =
                      assembleFromEmployeeOvertimeDetailsDto(
                          new UserCompensation(), employeeOvertimeDetailsDto, new Date());
                  return new OldAndNewCompensation(oldCompensation, newCompensation);
                })
            .collect(Collectors.toList());
    return updateCompensationsByAttendance(oldAndNewCompensations, isAddAttendanceEmployees);
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
        overtimePolicyRepository.findAll().stream()
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
    final List<UserCompensation> oldUserCompensationList =
        userCompensationRepository.findByOvertimePolicyId(oldPolicyId);
    final List<OldAndNewCompensation> oldAndNewCompensations = new ArrayList<>();

    oldUserCompensationList.forEach(
        oldUserCompensation -> {
          final UserCompensation newUserCompensation = new UserCompensation();
          BeanUtils.copyProperties(oldUserCompensation, newUserCompensation);

          newUserCompensation.setOvertimePolicy(newPolicy);

          oldAndNewCompensations.add(
              new OldAndNewCompensation(oldUserCompensation, newUserCompensation));
        });
    updateCompensationsByAttendance(oldAndNewCompensations, false);
  }

  private List<UserCompensation> updateCompensationsByAttendance(
      final List<OldAndNewCompensation> oldAndNewCompensationList,
      final boolean isAddAttendanceEmployees) {
    final List<TimeSheet> timeSheets = new ArrayList<>();
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
          newUserCompensation.setEndDate(
              isAddAttendanceEmployees ? null : oldUserCompensation.getEndDate());
          final UserCompensation savedNewUserCompensation = save(newUserCompensation);
          newUserCompensationList.add(savedNewUserCompensation);

          oldUserCompensation.setEndDate(nowTime);
          oldUserCompensationList.add(oldUserCompensation);

          if (!isAddAttendanceEmployees) {
            final TimeSheet timeSheet = timeSheetService.findCurrentByUseCompensation(oldUserCompensation);

            if (timeSheet != null) {
              timeSheet.setUserCompensation(savedNewUserCompensation);
              timeSheets.add(timeSheet);
            }
          }

          final JobUser jobUser =
              jobUserRepository.findByUserCompensationId(oldUserCompensation.getId());
          jobUser.setUserCompensation(newUserCompensation);
        });

    jobUserRepository.saveAll(jobUsers);
    timeSheetService.saveAll(timeSheets);
    saveAll(oldUserCompensationList);
    return newUserCompensationList;
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
