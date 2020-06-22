package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserCompensation;

public interface UserCompensationRepository extends BaseRepository<UserCompensation, String> {
    Boolean existsByUserId(String userId);

    UserCompensation findByUserId(String userId);
}
