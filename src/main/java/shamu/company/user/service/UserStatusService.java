package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.repository.UserStatusRepository;

@Service
@Transactional
public class UserStatusService {

  private final UserStatusRepository userStatusRepository;

  @Autowired
  public UserStatusService(final UserStatusRepository userStatusRepository) {
    this.userStatusRepository = userStatusRepository;
  }

  public UserStatus findByName(final String name) {
    return userStatusRepository.findByName(name);
  }

  public List<UserStatus> findAll() {
    return userStatusRepository.findAll();
  }
}
