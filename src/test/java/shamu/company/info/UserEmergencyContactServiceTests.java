package shamu.company.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.utils.UuidUtil;

public class UserEmergencyContactServiceTests {

  @Mock
  private UserEmergencyContactRepository userEmergencyContactRepository;

  @InjectMocks
  private UserEmergencyContactService userEmergencyContactService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
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
    final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();

    @Test
    void whenIsPrimary_thenShouldCallFunctionReset() {
      userEmergencyContact.setIsPrimary(true);
      Mockito.when(userEmergencyContactRepository.findById(id))
             .thenReturn(java.util.Optional.of(userEmergencyContact));
      userEmergencyContactService.deleteEmergencyContact(userId, id);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(1))
             .resetPrimaryContact(userId);
    }

    @Test
    void whenIsNotPrimary_thenShouldNotCallFunctionReset() {
      userEmergencyContact.setIsPrimary(false);
      Mockito.when(userEmergencyContactRepository.findById(id))
              .thenReturn(java.util.Optional.of(userEmergencyContact));
      userEmergencyContactService.deleteEmergencyContact(userId, id);
      Mockito.verify(userEmergencyContactRepository, Mockito.times(0))
              .resetPrimaryContact(userId);
    }
  }
}
