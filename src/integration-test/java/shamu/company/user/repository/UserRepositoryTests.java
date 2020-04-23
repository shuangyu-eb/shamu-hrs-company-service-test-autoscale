package shamu.company.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import shamu.company.DataLayerBaseTests;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.utils.UuidUtil;

class UserRepositoryTests extends DataLayerBaseTests {

  @Autowired private UserRepository userRepository;

  @Test
  void testSave() {
    final User user = new User();
    user.setId(UuidUtil.getUuidString());
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("example1@example.com");
    user.setUserContactInformation(userContactInformation);
    final User savedUser = userRepository.save(user);
    assertThat(savedUser).isNotNull();
  }

  @Test
  void testSaveAll() {
    final User user = new User();
    user.setId(UuidUtil.getUuidString());
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("example@example.com");
    user.setUserContactInformation(userContactInformation);
    final List<User> savedUsers = userRepository.saveAll(Collections.singleton(user));
    assertThat(savedUsers.size()).isEqualTo(1);
  }
}
