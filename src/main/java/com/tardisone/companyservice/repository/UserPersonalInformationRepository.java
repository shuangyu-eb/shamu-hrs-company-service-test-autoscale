package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserPersonalInformation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserPersonalInformationRepository extends JpaRepository<UserPersonalInformation, Long> {
    UserPersonalInformation findUserPersonalInformationById(Long id);
    UserPersonalInformation findUserPersonalInformationByFirstName(String firstName);
}
