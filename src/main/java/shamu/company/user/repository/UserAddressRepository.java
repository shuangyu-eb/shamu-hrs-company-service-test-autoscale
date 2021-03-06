package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

public interface UserAddressRepository extends BaseRepository<UserAddress, String> {

  List<UserAddress> findAllByUserIn(List<User> users);

  UserAddress findUserAddressByUserId(String userId);
}
