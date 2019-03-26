package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserPersonalInformation;

public interface UserPersonalInformationRepository extends BaseRepository<UserPersonalInformation,Long> {
    UserPersonalInformation findUserPersonalInformationByEmailWork(String emailWork);
}
