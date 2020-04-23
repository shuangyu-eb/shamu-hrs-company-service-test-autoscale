package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;

@Service
public class UserContactInformationService {

  private final UserContactInformationRepository userContactInformationRepository;

  @Autowired
  public UserContactInformationService(
      final UserContactInformationRepository userContactInformationRepository) {
    this.userContactInformationRepository = userContactInformationRepository;
  }

  public UserContactInformation update(final UserContactInformation userContactInformation) {
    return userContactInformationRepository.save(userContactInformation);
  }

  public UserContactInformation findUserContactInformationById(final String id) {
    return userContactInformationRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("User contact information does not exist"));
  }

  public void delete(final UserContactInformation userContactInformation) {
    userContactInformationRepository.delete(userContactInformation);
  }

  public UserContactInformation save(final UserContactInformation userContactInformation) {
    return userContactInformationRepository.save(userContactInformation);
  }
}
