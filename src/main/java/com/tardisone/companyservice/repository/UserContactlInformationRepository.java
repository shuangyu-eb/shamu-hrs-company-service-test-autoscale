package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.model.UserContactInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface UserContactlInformationRepository extends JpaRepository<UserContactInformation, Integer> {
}
