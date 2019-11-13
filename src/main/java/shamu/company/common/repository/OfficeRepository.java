package shamu.company.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import shamu.company.company.entity.Office;

public interface OfficeRepository extends BaseRepository<Office, Long> {

  List<Office> findByCompanyId(Long companyId);

  @Query(
          value = "SELECT count(1) FROM jobs_users ju"
                  + " WHERE ju.office_id = ?1 ",
          nativeQuery = true)
  Integer getCountByOffice(Long officeId);
}
