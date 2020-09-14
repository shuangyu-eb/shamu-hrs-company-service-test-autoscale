package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.UserCompensationRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
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

  @Autowired
  public UserCompensationService(
          final UserCompensationRepository userCompensationRepository,
          final UserCompensationMapper userCompensationMapper,
          final CompensationFrequencyService compensationFrequencyService) {
    this.userCompensationRepository = userCompensationRepository;
    this.userCompensationMapper = userCompensationMapper;
    this.compensationFrequencyService = compensationFrequencyService;
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

  public List<UserCompensation> saveAllByEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList,
      final List<OvertimePolicy> savedPolicies,
      final Date startDate) {
    final List<UserCompensation> userCompensations =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  final String userId = employeeOvertimeDetailsDto.getEmployeeId();
                  final BigInteger wageCents =
                      userCompensationMapper.updateCompensationCents(
                          employeeOvertimeDetailsDto.getRegularPay());
                  final CompensationFrequency compensationFrequency =
                      compensationFrequencyService.findById(
                          employeeOvertimeDetailsDto.getCompensationUnit());
                  final Optional<OvertimePolicy> overtimePolicy =
                      savedPolicies.stream()
                          .filter(
                              savedPolicy ->
                                  employeeOvertimeDetailsDto
                                      .getOvertimePolicy()
                                      .equals(savedPolicy.getPolicyName()))
                          .findFirst();
                  final Timestamp startDateTimeStamp = new Timestamp(startDate.getTime());
                  if (existsByUserId(userId)) {
                    final UserCompensation userCompensation = findByUserId(userId);
                    userCompensation.setCompensationFrequency(compensationFrequency);
                    overtimePolicy.ifPresent(userCompensation::setOvertimePolicy);
                    userCompensation.setWageCents(wageCents);
                    userCompensation.setStartDate(startDateTimeStamp);
                    return userCompensation;
                  } else {
                    return new UserCompensation(
                        userId,
                        wageCents,
                        overtimePolicy,
                        compensationFrequency,
                        startDateTimeStamp);
                  }
                })
            .collect(Collectors.toList());
    return saveAll(userCompensations);
  }
}
