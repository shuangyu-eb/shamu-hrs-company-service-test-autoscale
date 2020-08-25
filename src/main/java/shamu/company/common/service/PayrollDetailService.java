package shamu.company.common.service;

import org.springframework.stereotype.Service;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.repository.PayrollDetailRepository;

@Service
public class PayrollDetailService {

  private final PayrollDetailRepository payrollDetailRepository;

  public PayrollDetailService(final PayrollDetailRepository payrollDetailRepository) {
    this.payrollDetailRepository = payrollDetailRepository;
  }

  public void savePayrollDetail(final PayrollDetail payrollDetail) {
    payrollDetailRepository.save(payrollDetail);
  }

  public PayrollDetail findByCompanyId(final String companyId) {
    return payrollDetailRepository.findByCompanyId(companyId);
  }
}
