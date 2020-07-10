package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.UserCompensationRepository;

import java.util.List;

@Service
@Transactional
public class UserCompensationService {

  private final UserCompensationRepository userCompensationRepository;

  private final UserCompensationMapper userCompensationMapper;

  @Autowired
  public UserCompensationService(
      final UserCompensationRepository userCompensationRepository,
      final UserCompensationMapper userCompensationMapper) {
    this.userCompensationRepository = userCompensationRepository;
    this.userCompensationMapper = userCompensationMapper;
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

  public List<UserCompensation> listNewestEnrolledCompensation(final String companyId) {
    return userCompensationRepository.listNewestEnrolledUserByCompanyId(
        companyId, CompensationOvertimeStatus.OvertimeStatus.NOT_ELIGIBLE.getValue());
  }

  public boolean existsByUserId(final String userId) {
    return userCompensationRepository.existsByUserId(userId);
  }

  public UserCompensation findByUserId(final String userId) {
    return userCompensationRepository.findByUserId(userId);
  }

  public void saveAll(final List<UserCompensation> userCompensationList) {
    userCompensationRepository.saveAll(userCompensationList);
  }
}
