package shamu.company.benefit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shamu.company.benefit.entity.BenefitRequest;
import shamu.company.benefit.entity.BenefitRequestApprovalStatus.BenefitRequestStatus;
import shamu.company.common.repository.BaseRepository;

@Repository
public interface BenefitRequestRepository extends BaseRepository<BenefitRequest, String> {

  @Query(value = "SELECT * FROM benefits_requests WHERE request_status = ?1", nativeQuery = true)
  Page<BenefitRequest> findAllByStatus(BenefitRequestStatus status, PageRequest pageRequest);

  @Query(
      value = "SELECT COUNT(*) FROM benefits_requests WHERE request_status = ?1",
      nativeQuery = true)
  Integer countRequestsByStatus(BenefitRequestStatus status);
}
