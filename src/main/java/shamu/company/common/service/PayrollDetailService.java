package shamu.company.common.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

  public PayrollDetail find() {
    final List<PayrollDetail> results = payrollDetailRepository.findAll();
    if (CollectionUtils.isEmpty(results)) {
      return null;
    }
    return payrollDetailRepository.findAll().get(0);
  }
}
