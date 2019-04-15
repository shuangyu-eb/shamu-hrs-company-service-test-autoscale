package shamu.company.employee.repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.job.Job;

@Repository
public interface JobRepository extends BaseRepository<Job, Long> {
}
