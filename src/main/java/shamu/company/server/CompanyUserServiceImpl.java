package shamu.company.server;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

@Service
public class CompanyUserServiceImpl implements CompanyUserService {

  private final UserRepository userRepository;

  private final CompanyRepository companyRepository;

  @Autowired
  public CompanyUserServiceImpl(final UserRepository userRepository,
      final CompanyRepository companyRepository) {
    this.userRepository = userRepository;
    this.companyRepository = companyRepository;
  }

  @Override
  public List<User> getUsersBy(final List<Long> ids) {
    return userRepository.findAllById(ids);
  }

  @Override
  public User findUserByEmail(final String email) {
    return userRepository.findByEmailWork(email);
  }

  @Override
  public List<User> getAllUsers(final Long companyId) {
    final Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Company does not exist"));
    return userRepository.findAllByCompanyId(company.getId());
  }

  @Override
  public AuthUser findUserByUserId(final String userId) {
    final User user = userRepository.findByUserId(userId);
    return new AuthUser(user);
  }
}
