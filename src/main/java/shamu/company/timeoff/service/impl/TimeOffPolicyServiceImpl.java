package shamu.company.timeoff.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {

  private final TimeOffPolicyRepository timeOffPolicyRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  private final JobUserRepository jobUserRepository;

  private final UserRepository userRepository;

  @Autowired
  public TimeOffPolicyServiceImpl(
      TimeOffPolicyRepository timeOffPolicyRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      JobUserRepository jobUserRepository,
      UserRepository userRepository) {
    this.timeOffPolicyRepository = timeOffPolicyRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.jobUserRepository = jobUserRepository;
    this.userRepository = userRepository;
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
  public List<TimeOffBalanceDto> getTimeOffBalances(Long userId, Long companyId) {
    return timeOffPolicyUserRepository
        .findTimeOffBalancesByUser(userId, companyId)
        .stream().map((timeOffBalance -> {
          TimeOffBalanceDto timeOffBalanceDto = new TimeOffBalanceDto();
          BeanUtils.copyProperties(timeOffBalance, timeOffBalanceDto);
          return timeOffBalanceDto;
        })).collect(Collectors.toList());
  }

  @Override
  public void createTimeOffPolicyUsers(List<TimeOffPolicyUser> timeOffPolicyUsers) {
    timeOffPolicyUserRepository.saveAll(timeOffPolicyUsers);
  }

  private void createTimeOffPolicyUsers(List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos,
      Long policyId) {
    if (!timeOffPolicyUserPojos.isEmpty()) {
      timeOffPolicyUserRepository.saveAll(
          timeOffPolicyUserPojos
              .stream()
              .map(timeOffPolicyUserPojo ->
                  timeOffPolicyUserPojo.getTimeOffPolicyUser(policyId))
              .collect(Collectors.toList())
      );
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
  public TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(Long id) {
    TimeOffPolicy timeOffPolicy = timeOffPolicyRepository.findById(id).get();
    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        timeOffPolicyAccrualScheduleRepository
        .findAllByTimeOffPolicy(timeOffPolicy);
    List<AccrualScheduleMilestone> accrualScheduleMilestones = accrualScheduleMilestoneRepository
        .findByTimeOffPolicyAccrualScheduleId(timeOffPolicyAccrualSchedule.getId());

    return new TimeOffPolicyRelatedInfoDto(
        timeOffPolicy,timeOffPolicyAccrualSchedule,accrualScheduleMilestones);
  }

  @Override
  public TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      Long timeOffPolicyId,Company company) {
    Optional<TimeOffPolicy> timeOffPolicy = timeOffPolicyRepository.findById(timeOffPolicyId);

    Boolean isLimited = timeOffPolicy.get().getIsLimited();

    List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyUserRepository
        .findAllByTimeOffPolicyId(timeOffPolicyId);

    List<User> selectableTimeOffPolicyUsers = userRepository.findAllByCompany(company);

    ArrayList<Long> selectedUsersIds = new ArrayList<>();

    List<TimeOffPolicyRelatedUserDto> selectedEmployees = timeOffPolicyUsers.stream().map(
        (user) -> {
          JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user.getUser());
          JobUserDto employeeWithJobInfoDto = new JobUserDto(user.getUser(),employeeWithJobInfo);
          Integer balance = user.getBalance();
          selectedUsersIds.add(user.getUser().getId());
          return new TimeOffPolicyRelatedUserDto(balance,employeeWithJobInfoDto);
        }
    ).collect(Collectors.toList());


    List<TimeOffPolicyRelatedUserDto> unselectedEmployees = selectableTimeOffPolicyUsers
        .stream().filter(user -> !selectedUsersIds.contains(user.getId()))
        .map(user -> {
          JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
          JobUserDto employeeWithJobInfoDto = new JobUserDto(user,employeeWithJobInfo);
          Integer balance = 0;
          return new TimeOffPolicyRelatedUserDto(balance,employeeWithJobInfoDto);
        }).collect(Collectors.toList());

    return new TimeOffPolicyRelatedUserListDto(
        isLimited,unselectedEmployees,selectedEmployees);
  }

  @Override
  public void updateTimeOffPolicy(TimeOffPolicy timeOffPolicy) {
    timeOffPolicyRepository.save(timeOffPolicy);
  }

  @Override
  public TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      TimeOffPolicy timeOffPolicy) {
    return  timeOffPolicyAccrualScheduleRepository.findAllByTimeOffPolicy(timeOffPolicy);
  }

  @Override
  public void updateTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule) {
    timeOffPolicyAccrualScheduleRepository.save(timeOffPolicyAccrualSchedule);
  }

  @Override
  public TimeOffPolicyUser getTimeOffPolicyUserByUserAndTimeOffPolicy(User user,
      TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(user, timeOffPolicy);
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
  public TimeOffPolicyUser saveTimeOffPolicyUser(TimeOffPolicyUser timeOffPolicyUser) {
    return timeOffPolicyUserRepository.save(timeOffPolicyUser);
  }


  private void createAccrualScheduleMilestones(
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList, Long scheduleId) {
    if (!accrualScheduleMilestoneDtoList.isEmpty()) {
      accrualScheduleMilestoneRepository.saveAll(
          accrualScheduleMilestoneDtoList
              .stream()
              .map(accrualScheduleMilestoneDto ->
                  accrualScheduleMilestoneDto.getAccrualScheduleMilestone(scheduleId))
              .collect(Collectors.toList()));
    }
  }

  @Override
  public void updateMilestones(
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,Long scheduleId
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
                    .get().updateAccrualScheduleMilestone(accrualScheduleMilestone,scheduleId);
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
    List<Long> newUserIds =  userStatBalances.stream()
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
}
