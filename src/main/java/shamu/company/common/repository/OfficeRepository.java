package shamu.company.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.company.entity.Office;

public interface OfficeRepository extends BaseRepository<Office, String> {

  List<Office> findByCompanyId(String companyId);

  @Query(
      value = "SELECT count(1) FROM jobs_users ju" + " WHERE ju.office_id = unhex(?1) ",
      nativeQuery = true)
  Integer findCountByOffice(String officeId);

  @Query(
      value =
          "SELECT * FROM offices o"
              + " WHERE binary o.name = ?1 and o.company_id = unhex(?2) ",
      nativeQuery = true)
  List<Office> findByNameAndCompanyId(String name, String companyId);
}
