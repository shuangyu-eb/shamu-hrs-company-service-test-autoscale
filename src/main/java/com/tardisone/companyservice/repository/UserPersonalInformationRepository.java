package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.model.UserPersonalInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface UserPersonalInformationRepository extends JpaRepository<UserPersonalInformation, Integer> {
}
