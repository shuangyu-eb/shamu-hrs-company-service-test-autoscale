package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserContactInformation;

public interface UserContactInformationRepository extends BaseRepository<UserContactInformation, Long> {
    UserContactInformation findUserContactInformationByEmailWork(String emailWork);
    UserContactInformation findUserContactInformationById(Long id);
}
