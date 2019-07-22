package shamu.company.server;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

@Service
public class CompanyUserServiceImpl implements CompanyUserService {

  private final UserRepository userRepository;

  @Autowired
  public CompanyUserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public List<User> getUsersBy(List<Long> ids) {
    return userRepository.findAllById(ids);
  }

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmailWork(email);
  }
}
