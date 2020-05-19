package shamu.company.info.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.OldResourceNotFoundException;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.repository.UserEmergencyContactRepository;

@Service
public class UserEmergencyContactService {

  private final UserEmergencyContactRepository userEmergencyContactRepository;

  private final UserEmergencyContactMapper userEmergencyContactMapper;

  public UserEmergencyContactService(
      final UserEmergencyContactRepository userEmergencyContactRepository,
      final UserEmergencyContactMapper userEmergencyContactMapper) {
    this.userEmergencyContactRepository = userEmergencyContactRepository;
    this.userEmergencyContactMapper = userEmergencyContactMapper;
  }

  public UserEmergencyContact save(final UserEmergencyContact userEmergencyContact) {
    return userEmergencyContactRepository.save(userEmergencyContact);
  }

  public List<UserEmergencyContact> findUserEmergencyContacts(final String userId) {
    return userEmergencyContactRepository.findByUserId(userId);
  }

  public void createUserEmergencyContact(
      final String userId, final UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userId);
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }

  public void deleteEmergencyContact(final String id) {
    final UserEmergencyContact userEmergencyContact =
        userEmergencyContactRepository
            .findById(id)
            .orElseThrow(
                () -> new OldResourceNotFoundException("UserEmergencyContact does not exist!"));
    final String userId = userEmergencyContact.getUser().getId();
    userEmergencyContactRepository.releasePrimaryContact(userId);
    userEmergencyContactRepository.delete(id);
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.resetPrimaryContact(userId);
    }
  }

  public void updateEmergencyContact(final UserEmergencyContactDto userEmergencyContactDto) {
    final UserEmergencyContact userEmergencyContact = findById(userEmergencyContactDto.getId());

    userEmergencyContactMapper.updateFromUserEmergencyContactDto(
        userEmergencyContact, userEmergencyContactDto);
    if (userEmergencyContact.getState() != null
        && userEmergencyContact.getState().getId() == null) {
      userEmergencyContact.setState(null);
    }
    if (userEmergencyContact.getIsPrimary()) {
      userEmergencyContactRepository.releasePrimaryContact(userEmergencyContact.getUser().getId());
    }
    userEmergencyContactRepository.save(userEmergencyContact);
  }

  public List<String> findAllIdByUserId(final String id) {
    return userEmergencyContactRepository.findAllIdByUserId(id);
  }

  public void deleteInBatch(final List<String> userEmergencyContactIds) {
    userEmergencyContactRepository.deleteInBatch(userEmergencyContactIds);
  }

  public UserEmergencyContact findById(final String id) {
    return userEmergencyContactRepository
        .findById(id)
        .orElseThrow(() -> new OldResourceNotFoundException("Emergency contact was not found"));
  }
}
