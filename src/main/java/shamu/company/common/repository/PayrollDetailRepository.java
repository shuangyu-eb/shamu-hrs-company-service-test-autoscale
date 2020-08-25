package shamu.company.common.repository;

import shamu.company.common.entity.PayrollDetail;

public interface PayrollDetailRepository extends BaseRepository<PayrollDetail, String> {

  PayrollDetail findByCompanyId(String companyId);
}
