package shamu.company.user.repository;

import java.util.List;
import java.util.Optional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

public interface UserAddressRepository extends BaseRepository<UserAddress, Long> {

  List<UserAddress> findAllByUserIn(List<User> users);

  Optional<UserAddress> findUserAddressByUserId(Long userId);
}
