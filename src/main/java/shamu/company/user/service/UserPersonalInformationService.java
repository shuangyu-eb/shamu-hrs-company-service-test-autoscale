package shamu.company.user.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.UserPersonalInformationRepository;

@Service
public class UserPersonalInformationService {

  private final UserPersonalInformationRepository userPersonalInformationRepository;

  private final GenderService genderService;

  private final MaritalStatusService maritalStatusService;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  @Autowired
  public UserPersonalInformationService(
      final UserPersonalInformationRepository userPersonalInformationRepository,
      final GenderService genderService,
      final MaritalStatusService maritalStatusService,
      final UserPersonalInformationMapper userPersonalInformationMapper) {
    this.userPersonalInformationRepository = userPersonalInformationRepository;
    this.genderService = genderService;
    this.maritalStatusService = maritalStatusService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
  }

  public UserPersonalInformation update(final UserPersonalInformation userPersonalInformation) {
    final Gender gender = userPersonalInformation.getGender();
    final MaritalStatus maritalStatus = userPersonalInformation.getMaritalStatus();

    if (gender != null) {
      final Gender genderUpdated = genderService.findById(gender.getId());
      userPersonalInformation.setGender(genderUpdated);
    }
    if (maritalStatus != null) {
      final MaritalStatus maritalStatusUpdated =
          maritalStatusService.findById(maritalStatus.getId());
      userPersonalInformation.setMaritalStatus(maritalStatusUpdated);
    }
    return userPersonalInformationRepository.save(userPersonalInformation);
  }

  public BasicUserPersonalInformationDto findUserPersonalInformation(
      final User targetUser, final AuthUser authUser) {
    final UserPersonalInformation userPersonalInformation = targetUser.getUserPersonalInformation();
    final String imageUrl = targetUser.getImageUrl();

    final User.Role userRole = authUser.getRole();
    final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
    if (authUser.getId().equals(targetUser.getId())
        || userRole == User.Role.ADMIN
        || userRole == User.Role.SUPER_ADMIN) {
      return userPersonalInformationMapper.convertToUserPersonalInformationDto(
          userPersonalInformation, imageUrl);
    }

    if (userRole == User.Role.MANAGER
        && targetUser.getManagerUser() != null
        && authUser.getId().equals(targetUser.getManagerUser().getId())) {
      return userPersonalInformationMapper.convertToMyEmployeePersonalInformationDto(
          userPersonalInformation);
    }

    final Date birthDate = userPersonalInformation.getBirthDate();
    final String birthDateWithoutYear = birthDate != null ? sdf.format(birthDate) : "";
    final BasicUserPersonalInformationDto basicUserPersonalInformationDto =
        userPersonalInformationMapper.convertToBasicUserPersonalInformationDto(
            userPersonalInformation);
    basicUserPersonalInformationDto.setBirthDate(birthDateWithoutYear);
    return basicUserPersonalInformationDto;
  }

  public void delete(final UserPersonalInformation userPersonalInformation) {
    userPersonalInformationRepository.delete(userPersonalInformation);
  }
}
