package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.repository.UserCompensationRepository;

@Service
@Transactional
public class UserCompensationService {

  private final UserCompensationRepository userCompensationRepository;

  @Autowired
  public UserCompensationService(final UserCompensationRepository userCompensationRepository) {
    this.userCompensationRepository = userCompensationRepository;
  }

  public UserCompensation save(final UserCompensation userCompensation) {
    return userCompensationRepository.save(userCompensation);
  }
}
