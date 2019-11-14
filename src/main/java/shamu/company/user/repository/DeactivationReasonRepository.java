package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.DeactivationReasons;

public interface DeactivationReasonRepository extends BaseRepository<DeactivationReasons, String> {

  List<DeactivationReasons> findAll();
}
