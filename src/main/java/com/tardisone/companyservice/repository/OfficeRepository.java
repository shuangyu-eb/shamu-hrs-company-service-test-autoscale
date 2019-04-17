package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Timestamp;


public interface OfficeRepository extends JpaRepository<Office, Integer> {

    @Transactional
    @Modifying
    @Query(
            value = "update offices " +
                    " set office_address_id= ?1  " +
                    " where id = ?2 ",
            nativeQuery = true
    )
    void saveOffice(Long  officeAddressId,Long id);

}
