package shamu.company.user.repository;

import java.util.Optional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.Gender;

public interface GenderRepository extends BaseRepository<Gender, Long> {
  Optional<Gender> findById(Long id);
}
