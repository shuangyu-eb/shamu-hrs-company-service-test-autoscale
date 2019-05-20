package shamu.company.job.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Department;
import shamu.company.job.entity.Job;

public interface JobRepository extends BaseRepository<Job, Long> {

  public List<Job> getAllByDepartment(Department department);

  @Query(value = "SELECT * from jobs where department_id=?1 and deleted_at is null ",
      nativeQuery = true)
  List<Job> findAllByDepartmentId(Long id);
}
