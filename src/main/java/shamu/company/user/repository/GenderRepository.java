package shamu.company.user.repository;

import java.util.List;
import java.util.Optional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.Gender;

public interface GenderRepository extends BaseRepository<Gender, String> {

  Optional<Gender> findById(String id);

  List<Gender> findAll();
}
