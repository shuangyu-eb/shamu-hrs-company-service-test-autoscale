package shamu.company.common.repository;

import java.util.List;
import shamu.company.company.entity.Office;

public interface OfficeRepository extends BaseRepository<Office, Long> {

  List<Office> findByCompanyId(Long companyId);
}
