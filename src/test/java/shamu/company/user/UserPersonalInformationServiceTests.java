package shamu.company.user;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserPersonalInformationService;

public class UserPersonalInformationServiceTests {

  @Mock private UserPersonalInformationRepository userPersonalInformationRepository;

  @Mock private GenderService genderService;

  @Mock private MaritalStatusService maritalStatusService;

  @Mock private UserPersonalInformationMapper userPersonalInformationMapper;

  @InjectMocks private UserPersonalInformationService userPersonalInformationService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void updateTest() {
    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setGender(new Gender(UUID.randomUUID().toString().replaceAll("-", "")));
    userPersonalInformation.setMaritalStatus(
        new MaritalStatus(UUID.randomUUID().toString().replaceAll("-", "")));

    userPersonalInformationService.update(userPersonalInformation);
    Mockito.verify(genderService, Mockito.times(1)).findById(Mockito.anyString());
    Mockito.verify(maritalStatusService, Mockito.times(1)).findById(Mockito.anyString());
  }

  @Nested
  class GetUserPersonalInformation {
    private final User targetUser = new User();

    private final AuthUser currentUser = new AuthUser();

    private String currentUserId;

    @BeforeEach
    void init() {
      currentUserId = UUID.randomUUID().toString().replaceAll("-", "");
      currentUser.setId(currentUserId);
      final String imageUrl = "url";
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      targetUser.setUserPersonalInformation(userPersonalInformation);
      targetUser.setImageUrl(imageUrl);
    }

    @Test
    void whenViewSelf_thenShouldShowAll() {
      targetUser.setId(currentUserId);
      userPersonalInformationService.findUserPersonalInformation(targetUser, currentUser);
      Mockito.verify(userPersonalInformationMapper, Mockito.times(1))
          .convertToUserPersonalInformationDto(Mockito.any(), Mockito.anyString());
    }

    @Test
    void whenViewerIsAdmin_thenShouldShowAll() {
      targetUser.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      currentUser.setRole(User.Role.ADMIN);
      userPersonalInformationService.findUserPersonalInformation(targetUser, currentUser);
      Mockito.verify(userPersonalInformationMapper, Mockito.times(1))
          .convertToUserPersonalInformationDto(Mockito.any(), Mockito.anyString());
    }

    @Test
    void whenViewerIsSuperAdmin_thenShouldShowAll() {
      targetUser.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      currentUser.setRole(User.Role.SUPER_ADMIN);
      userPersonalInformationService.findUserPersonalInformation(targetUser, currentUser);
      Mockito.verify(userPersonalInformationMapper, Mockito.times(1))
          .convertToUserPersonalInformationDto(Mockito.any(), Mockito.anyString());
    }

    @Test
    void whenViewerIsManager_thenShouldShowPart() {
      currentUser.setRole(User.Role.MANAGER);
      targetUser.setManagerUser(new User(currentUser.getId()));
      userPersonalInformationService.findUserPersonalInformation(targetUser, currentUser);
      Mockito.verify(userPersonalInformationMapper, Mockito.times(1))
          .convertToMyEmployeePersonalInformationDto(Mockito.any());
    }

    @Test
    void whenViewerIsEmployee_thenShouldShowBasic() {
      currentUser.setRole(User.Role.EMPLOYEE);
      Mockito.when(
              userPersonalInformationMapper.convertToBasicUserPersonalInformationDto(Mockito.any()))
          .thenReturn(new BasicUserPersonalInformationDto());
      userPersonalInformationService.findUserPersonalInformation(targetUser, currentUser);
      Mockito.verify(userPersonalInformationMapper, Mockito.times(1))
          .convertToBasicUserPersonalInformationDto(Mockito.any());
    }
  }
}
