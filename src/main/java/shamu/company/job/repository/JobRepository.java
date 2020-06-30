package shamu.company.job.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.Job;

public interface JobRepository extends BaseRepository<Job, String> {

  List<Job> findByCompanyId(String id);
}
