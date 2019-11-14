package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanDependentRelationshipRepository
    extends BaseRepository<DependentRelationship, String> {

  List<DependentRelationship> findAll();
}
