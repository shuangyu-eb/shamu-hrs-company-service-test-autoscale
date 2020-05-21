package shamu.company.info;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.user.entity.User;
import shamu.company.utils.UuidUtil;

public class UserEmergencyContactServiceTests {

  @Mock private UserEmergencyContactRepository userEmergencyContactRepository;

  @Mock private UserEmergencyContactMapper userEmergencyContactMapper;

  private UserEmergencyContactService userEmergencyContactService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    userEmergencyContactService =
        new UserEmergencyContactService(userEmergencyContactRepository, userEmergencyContactMapper);
  }

  @Test
  void testSave() {
    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
    Assertions.assertDoesNotThrow(() -> userEmergencyContactService.save(userEmergencyContact));
  }

  @Test
  void testFindUserEmergencyContacts() {
    Assertions.assertDoesNotThrow(() -> userEmergencyContactService.findUserEmergencyContacts("1"));
  }

  @Test
  void testFindAllIdByUserId() {
    Assertions.assertDoesNotThrow(() -> userEmergencyContactService.findAllIdByUserId("1"));
  }

  @Test
  void testDeleteInBatch() {
    final List<String> list = new ArrayList<>();
    list.add("1");
    Assertions.assertDoesNotThrow(() -> userEmergencyContactService.deleteInBatch(list));
  }

  @Nested
  class CreateUserEmergencyContact {

    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
    final String userId = UuidUtil.getUuidString();

    @Test
    void whenIsPrimary_thenShouldCallFunctionRelease() {
      userEmergencyContact.setIsPrimary(true);
      userEmergencyContactService.createUserEmergencyContact(userId, userEmergencyContact);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(1))
          .releasePrimaryContact(userId);
    }

    @Test
    void whenIsNotPrimary_thenShouldNotCallFunctionRelease() {
      userEmergencyContact.setIsPrimary(false);
      userEmergencyContactService.createUserEmergencyContact(userId, userEmergencyContact);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(0))
          .releasePrimaryContact(userId);
    }
  }

  @Nested
  class DeleteUserEmergencyContact {

    final String id = UuidUtil.getUuidString();
    final String userId = UuidUtil.getUuidString();
    final User user = new User(userId);
    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();

    @Test
    void whenIsPrimary_thenShouldCallFunctionReset() {
      userEmergencyContact.setIsPrimary(true);
      userEmergencyContact.setUser(user);

      Mockito.when(userEmergencyContactRepository.findById(Mockito.anyString()))
          .thenReturn(java.util.Optional.of(userEmergencyContact));
      userEmergencyContactService.deleteEmergencyContact(userId);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(1)).resetPrimaryContact(userId);
    }

    @Test
    void whenIsNotPrimary_thenShouldNotCallFunctionReset() {
      userEmergencyContact.setIsPrimary(false);
      userEmergencyContact.setUser(user);

      Mockito.when(userEmergencyContactRepository.findById(Mockito.anyString()))
          .thenReturn(java.util.Optional.of(userEmergencyContact));
      userEmergencyContactService.deleteEmergencyContact(userId);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(0)).resetPrimaryContact(userId);
    }
  }

  @Nested
  class testUpdateEmergencyContact {
    UserEmergencyContactDto userEmergencyContactDto;
    StateProvince stateProvince;
    UserEmergencyContact userEmergencyContact;

    @BeforeEach
    void init() {
      stateProvince = new StateProvince();
      userEmergencyContactDto = new UserEmergencyContactDto();
      userEmergencyContact = new UserEmergencyContact();
      userEmergencyContactDto.setId("1");
      userEmergencyContact.setState(stateProvince);
    }

    @Test
    void whenIsPrimaryTrue_thenShouldRelease() {
      final User user = new User();
      user.setId("1");
      userEmergencyContact.setIsPrimary(true);
      userEmergencyContact.setUser(user);
      final Optional<UserEmergencyContact> optional = Optional.of(userEmergencyContact);
      Mockito.when(userEmergencyContactRepository.findById(Mockito.anyString()))
          .thenReturn(optional);
      userEmergencyContactService.updateEmergencyContact(userEmergencyContactDto);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(1)).releasePrimaryContact("1");
    }

    @Test
    void whenIsPrimaryFalse_thenShouldNotRelease() {
      userEmergencyContact.setIsPrimary(false);
      final Optional<UserEmergencyContact> optional = Optional.of(userEmergencyContact);
      Mockito.when(userEmergencyContactRepository.findById(Mockito.anyString()))
          .thenReturn(optional);
      userEmergencyContactService.updateEmergencyContact(userEmergencyContactDto);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(0)).releasePrimaryContact("1");
    }

    @Test
    void whenContactNotFound_thenShouldThrow() {
      final Optional<UserEmergencyContact> optional = Optional.empty();
      Mockito.when(userEmergencyContactRepository.findById(Mockito.anyString()))
          .thenReturn(optional);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(
              () -> userEmergencyContactService.updateEmergencyContact(userEmergencyContactDto));
    }
  }
}
