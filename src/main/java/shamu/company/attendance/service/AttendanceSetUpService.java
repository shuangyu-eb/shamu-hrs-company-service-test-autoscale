package shamu.company.attendance.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserListDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticCompanyPayFrequencyTypeRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;

@Service
public class AttendanceSetUpService {

  private final CompanyTaSettingRepository companyTaSettingRepository;

  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  private final UserRepository userRepository;

  private final JobUserRepository jobUserRepository;

  private final JobUserMapper jobUserMapper;

  private final StaticCompanyPayFrequencyTypeRepository payFrequencyTypeRepository;

  private final CompanyRepository companyRepository;

  private final CompensationFrequencyRepository compensationFrequencyRepository;

  private final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  private final UserCompensationRepository userCompensationRepository;

  private final UserService userService;

  public AttendanceSetUpService(
      final CompanyTaSettingRepository companyTaSettingRepository,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final UserRepository userRepository,
      final JobUserRepository jobUserRepository,
      final JobUserMapper jobUserMapper,
      final StaticCompanyPayFrequencyTypeRepository payFrequencyTypeRepository,
      final CompanyRepository companyRepository,
      final CompensationFrequencyRepository compensationFrequencyRepository,
      final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository,
      final UserCompensationRepository userCompensationRepository,
      final UserService userService) {
    this.companyTaSettingRepository = companyTaSettingRepository;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.userRepository = userRepository;
    this.jobUserRepository = jobUserRepository;
    this.jobUserMapper = jobUserMapper;
    this.payFrequencyTypeRepository = payFrequencyTypeRepository;
    this.companyRepository = companyRepository;
    this.compensationFrequencyRepository = compensationFrequencyRepository;
    this.compensationOvertimeStatusRepository = compensationOvertimeStatusRepository;
    this.userCompensationRepository = userCompensationRepository;
    this.userService = userService;
  }

  public Boolean findIsAttendanceSetUp(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
  }

  public TimeAndAttendanceRelatedUserListDto getRelatedUsers(final String companyId) {
    // The logic of unSelectedUsers should be modified.
    final List<EmployeesTaSetting> timeAndAttendanceUsers = employeesTaSettingRepository.findAll();
    final List<User> allUsers = userRepository.findAllByCompanyId(companyId);

    final List<String> selectedUsersIds = new ArrayList<>();
    final List<TimeAndAttendanceRelatedUserDto> selectedEmployees =
        timeAndAttendanceUsers.stream()
            .map(
                timeAndAttendanceUser -> {
                  final User user = timeAndAttendanceUser.getEmployee();
                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
                  selectedUsersIds.add(user.getId());
                  String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, allUsers);
                  return jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
                      user, employeeWithJobInfo, userNameOrUserNameWithEmailAddress);
                })
            .collect(Collectors.toList());

    final List<User> unSelectedUsers =
        selectedUsersIds.isEmpty()
            ? allUsers
            : userRepository.findAllByCompanyIdAndIdNotIn(companyId, selectedUsersIds);
    final List<TimeAndAttendanceRelatedUserDto> unSelectedEmployees =
        unSelectedUsers.stream()
            .map(
                user -> {
                  final JobUser employeeWithJobInfo = jobUserRepository.findJobUserByUser(user);
                  String userNameOrUserNameWithEmailAddress =
                      userService.getUserNameInUsers(user, allUsers);
                  return jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
                      user, employeeWithJobInfo, userNameOrUserNameWithEmailAddress);
                })
            .collect(Collectors.toList());

    return new TimeAndAttendanceRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  public void saveAttendanceDetails(
      final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto, final String companyId) {
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payFrequencyTypeRepository.findByName(timeAndAttendanceDetailsDto.getPayPeriodFrequency());
    final Company company = companyRepository.findCompanyById(companyId);
    final Date payDate = timeAndAttendanceDetailsDto.getPayDate();
    final Timestamp payDay = new Timestamp(payDate.getTime());
    final CompanyTaSetting companyTaSetting =
        new CompanyTaSetting(company, staticCompanyPayFrequencyType, payDay);
    companyTaSettingRepository.save(companyTaSetting);

    final List<UserCompensation> userCompensations =
        timeAndAttendanceDetailsDto.getOvertimeDetails().stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  String userId = employeeOvertimeDetailsDto.getEmployeeId();
                  BigInteger wageCents =
                      BigInteger.valueOf(Math.round(employeeOvertimeDetailsDto.getRegularPay()));
                  CompensationFrequency compensationFrequency =
                      compensationFrequencyRepository
                          .findById(employeeOvertimeDetailsDto.getCompensationUnit())
                          .get();
                  CompensationOvertimeStatus compensationOvertimeStatus =
                      compensationOvertimeStatusRepository
                          .findById(employeeOvertimeDetailsDto.getOvertimeLaw())
                          .get();
                  if (userCompensationRepository.existsByUserId(userId)) {
                    UserCompensation userCompensation =
                        userCompensationRepository.findByUserId(userId);
                    userCompensation.setCompensationFrequency(compensationFrequency);
                    userCompensation.setOvertimeStatus(compensationOvertimeStatus);
                    userCompensation.setWageCents(wageCents);
                    return userCompensation;
                  } else {
                    return new UserCompensation(
                        userId, wageCents, compensationOvertimeStatus, compensationFrequency);
                  }
                })
            .collect(Collectors.toList());
    userCompensationRepository.saveAll(userCompensations);

    final List<JobUser> jobUsers =
        timeAndAttendanceDetailsDto.getOvertimeDetails().stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  JobUser jobUser =
                      jobUserRepository.findByUserId(employeeOvertimeDetailsDto.getEmployeeId());
                  Timestamp hireDate =
                      new Timestamp(employeeOvertimeDetailsDto.getHireDate().getTime());
                  jobUser.setStartDate(hireDate);
                  return jobUser;
                })
            .collect(Collectors.toList());
    jobUserRepository.saveAll(jobUsers);
  }
}
