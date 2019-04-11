package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserCompensation;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "user_compensations")
public interface UserCompensationRepository extends BaseRepository<UserCompensation, Long> {
}
