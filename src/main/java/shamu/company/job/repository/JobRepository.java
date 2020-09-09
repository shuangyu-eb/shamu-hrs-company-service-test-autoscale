package shamu.company.job.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.Job;

public interface JobRepository extends BaseRepository<Job, String> {

  List<Job> findByCompanyId(String id);

  @Query(
      value = "SELECT * FROM jobs j" + " WHERE binary j.title = ?1 and j.company_id = unhex(?2) ",
      nativeQuery = true)
  List<Job> findByTitleAndCompanyId(String title, String companyId);
}
