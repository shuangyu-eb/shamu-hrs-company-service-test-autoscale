package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.service.UserContactInformationService;

@Service
public class UserContactInformationServiceImpl implements UserContactInformationService {

  private final UserContactInformationRepository repository;

  @Autowired
  public UserContactInformationServiceImpl(UserContactInformationRepository repository) {
    this.repository = repository;
  }

  @Override
  public UserContactInformation update(UserContactInformation userContactInformation) {
    return repository.save(userContactInformation);
  }

  public UserContactInformation findUserContactInformationById(Long id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("User contact information does not exist"));
  }
}
