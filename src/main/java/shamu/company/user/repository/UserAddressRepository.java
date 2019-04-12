package shamu.company.user.repository;

import shamu.company.common.BaseRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

import java.util.List;

public interface UserAddressRepository extends BaseRepository<UserAddress, Long> {

    List<UserAddress> findAllByUserIn(List<User> users);
}
