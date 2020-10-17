package shamu.company.user.repository;

import java.util.List;
import shamu.company.benefit.entity.RetirementPayTypes;
import shamu.company.common.repository.BaseRepository;

public interface RetirementPayTypesRepository extends BaseRepository<RetirementPayTypes, String> {

  @Override
  List<RetirementPayTypes> findAll();

  RetirementPayTypes findByName(String name);
}
