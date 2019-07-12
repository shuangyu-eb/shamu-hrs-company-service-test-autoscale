package shamu.company.info.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.repository.UserEmergencyContactRepository;

@Service
public class UserEmergencyContactServiceImpl implements UserEmergencyContactService {

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  public UserEmergencyContactServiceImpl(
      UserEmergencyContactRepository userEmergencyContactRepository) {
    this.userEmergencyContactRepository = userEmergencyContactRepository;
  }

  @Override
  public List<UserEmergencyContact> getUserEmergencyContacts(Long userId) {
    return userEmergencyContactRepository.findByUserId(userId);
  }

  @Override
  public void createUserEmergencyContact(Long userId, UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userId);
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }

  @Override
  public void deleteEmergencyContact(Long userId, Long id) {
    Optional<UserEmergencyContact> userEmergencyContact =
        userEmergencyContactRepository.findById(id);
    userEmergencyContactRepository.releasePrimaryContact(userId);
    userEmergencyContactRepository.delete(id);
    if (userEmergencyContact.get().getIsPrimary()) {
      userEmergencyContactRepository.resetPrimaryContact(userId);
    }
  }

  @Override
  public void updateEmergencyContact(Long userId, UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userId);
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }
}
