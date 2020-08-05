package shamu.company.job.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.Job;

public interface JobRepository extends BaseRepository<Job, String> {

  @Query(value = "SELECT * FROM jobs j WHERE binary j.title = ?1 ", nativeQuery = true)
  List<Job> findByTitle(String title);
}
