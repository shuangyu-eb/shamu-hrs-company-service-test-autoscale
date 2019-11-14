package shamu.company.user.repository;

import java.util.List;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.common.repository.BaseRepository;

public interface RetirementTypeRepository extends BaseRepository<RetirementType, String> {

  List<RetirementType> findAll();
}
