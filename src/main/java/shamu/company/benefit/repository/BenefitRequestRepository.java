package shamu.company.benefit.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shamu.company.benefit.entity.BenefitRequest;
import shamu.company.common.repository.BaseRepository;

@Repository
public interface BenefitRequestRepository extends BaseRepository<BenefitRequest, String> {

  @Query(
      value =
          "SELECT * "
              + "FROM benefits_requests br "
              + "LEFT JOIN users u ON br.request_user_id = u.id "
              + "LEFT JOIN benefit_request_status brs ON brs.id = br.request_status_id "
              + "WHERE brs.name IN ?1 "
              + "AND u.company_id = unhex(?2) "
              + "ORDER BY br.created_at ASC ",
      nativeQuery = true)
  Page<BenefitRequest> findAllByStatusAndCompanyId(
      List<String> statuses, String companyId, PageRequest pageRequest);

  @Query(
      value =
          "SELECT COUNT(*) "
              + "FROM benefits_requests br "
              + "LEFT JOIN users u ON br.request_user_id = u.id "
              + "LEFT JOIN benefit_request_status brs ON brs.id = br.request_status_id "
              + "WHERE brs.name = ?1 "
              + "AND u.company_id = unhex(?2) ",
      nativeQuery = true)
  Integer countRequestsByStatusAndCompanyId(String status, String companyId);
}
