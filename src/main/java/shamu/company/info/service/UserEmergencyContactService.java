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

  public UserEmergencyContact save(final UserEmergencyContact userEmergencyContact) {
    return userEmergencyContactRepository.save(userEmergencyContact);
  }

  public List<UserEmergencyContact> findUserEmergencyContacts(final String userId) {
    return userEmergencyContactRepository.findByUserId(userId);
  }

  public void createUserEmergencyContact(final String userId,
      final UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userId);
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }

  public void deleteEmergencyContact(final String userId, final String id) {
    final UserEmergencyContact userEmergencyContact =
        userEmergencyContactRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("UserEmergencyContact does not exist!"));
    userEmergencyContactRepository.releasePrimaryContact(userId);
    userEmergencyContactRepository.delete(id);
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.resetPrimaryContact(userId);
    }
  }

  public void updateEmergencyContact(final String userId,
      final UserEmergencyContact userEmergencyContact) {
    createUserEmergencyContact(userId, userEmergencyContact);
  }

  public List<String> findAllIdByUserId(final String id) {
    return userEmergencyContactRepository.findAllIdByUserId(id);
  }

  public void deleteInBatch(final List<String> userEmergencyContactIds) {
    userEmergencyContactRepository.deleteInBatch(userEmergencyContactIds);
  }
}
