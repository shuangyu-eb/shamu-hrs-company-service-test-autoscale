package shamu.company.employee.Repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.job.Job;

@Repository
public interface JobRepository extends BaseRepository<Job, Long> {

    public Job findByTitle(String title);
}
