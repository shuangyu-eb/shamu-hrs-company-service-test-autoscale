package shamu.company.info.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;

@Service
public class UserEmergencyContactService {

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  public UserEmergencyContactService(
      final UserEmergencyContactRepository userEmergencyContactRepository) {
    this.userEmergencyContactRepository = userEmergencyContactRepository;
  }

  public List<UserEmergencyContact> getUserEmergencyContacts(final Long userId) {
    return userEmergencyContactRepository.findByUserId(userId);
  }

  public void createUserEmergencyContact(final Long userId,
      final UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userId);
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }

  public void deleteEmergencyContact(final Long userId, final Long id) {
    final UserEmergencyContact userEmergencyContact =
        userEmergencyContactRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("UserEmergencyContact does not exist!"));
    userEmergencyContactRepository.releasePrimaryContact(userId);
    userEmergencyContactRepository.delete(id);
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.resetPrimaryContact(userId);
    }
  }

  public void updateEmergencyContact(final Long userId,
      final UserEmergencyContact userEmergencyContact) {
    createUserEmergencyContact(userId, userEmergencyContact);
  }
}
