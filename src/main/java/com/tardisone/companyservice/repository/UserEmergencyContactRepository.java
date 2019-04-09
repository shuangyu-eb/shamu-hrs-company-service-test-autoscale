package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserEmergencyContact;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserEmergencyContactRepository extends BaseRepository<UserEmergencyContact, Long> {

	@Query(value = "SELECT * FROM user_emergency_contacts WHERE user_id = ?1", nativeQuery = true)
	List<UserEmergencyContact> findByUserId(Long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE user_emergency_contacts SET is_primary = FALSE WHERE user_id = ?1", nativeQuery = true)
	void releasePrimaryContact(Long id);
}
