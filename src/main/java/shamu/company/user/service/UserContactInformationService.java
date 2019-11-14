package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;

@Service
public class UserContactInformationService {

  private final UserContactInformationRepository repository;

  @Autowired
  public UserContactInformationService(final UserContactInformationRepository repository) {
    this.repository = repository;
  }

  public UserContactInformation update(final UserContactInformation userContactInformation) {
    return repository.save(userContactInformation);
  }

  public UserContactInformation findUserContactInformationById(final String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("User contact information does not exist"));
  }
}
