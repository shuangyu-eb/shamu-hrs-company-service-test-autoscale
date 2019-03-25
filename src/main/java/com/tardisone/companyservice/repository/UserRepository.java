package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {
    User findByEmailWork(String emailWork);

    User findByVerificationToken(String activationToken);
}
