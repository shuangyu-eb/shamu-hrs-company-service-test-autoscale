package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.Ethnicity;

public interface EthnicityRepository extends BaseRepository<Ethnicity, String> {

  List<Ethnicity> findAll();
}
