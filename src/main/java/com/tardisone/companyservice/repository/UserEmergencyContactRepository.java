package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserEmergencyContact;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserEmergencyContactRepository extends BaseRepository<UserEmergencyContact, Long> {

	@Query(value = "SELECT * FROM user_emergency_contacts WHERE deleted_at IS NULL AND user_id = ?1", nativeQuery = true)
	List<UserEmergencyContact> findByUserId(Long id);
}
