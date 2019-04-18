package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.service.UserContactInformationService;

@Service
public class UserContactInformationServiceImpl implements UserContactInformationService {

  @Autowired
  UserContactInformationRepository repository;

  @Override
  public UserContactInformation update(UserContactInformation userContactInformation) {
    return repository.save(userContactInformation);
  }
}
