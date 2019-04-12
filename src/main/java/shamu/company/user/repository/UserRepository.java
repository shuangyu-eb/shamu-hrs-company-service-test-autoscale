package shamu.company.user.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

import java.util.List;

public interface UserRepository extends BaseRepository<User, Long> {
    User findByEmailWork(String emailWork);

    User findByVerificationToken(String activationToken);

    @Query(value = "SELECT * FROM users WHERE manager_user_id IS NOT NULL AND deleted_at IS NULL", nativeQuery = true)
    List<User> findAllEmployees();

    Boolean existsByEmailWork(String email);
}
