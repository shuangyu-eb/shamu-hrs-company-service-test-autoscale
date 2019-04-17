package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserContactInformation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserContactlInformationRepository extends JpaRepository<UserContactInformation, Long> {
    UserContactInformation findUserContactInformationByEmailWork(String emailWork);
    UserContactInformation findUserContactInformationById(Long id);
}
