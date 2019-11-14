package shamu.company.user.repository;

import java.util.List;
import java.util.Optional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.MaritalStatus;

public interface MaritalStatusRepository extends BaseRepository<MaritalStatus, String> {

  Optional<MaritalStatus> findById(String id);

  List<MaritalStatus> findAll();
}
