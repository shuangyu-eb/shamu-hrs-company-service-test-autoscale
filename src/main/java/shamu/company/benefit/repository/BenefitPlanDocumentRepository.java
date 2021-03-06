package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlanDocument;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanDocumentRepository extends BaseRepository<BenefitPlanDocument, String> {

  List<BenefitPlanDocument> findAllByBenefitPlanId(String planId);
}
