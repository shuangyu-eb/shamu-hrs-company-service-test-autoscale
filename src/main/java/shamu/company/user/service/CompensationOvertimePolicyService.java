package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;

import java.util.List;

@Service
@Transactional
public class CompensationOvertimePolicyService {
  CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  @Autowired
  public CompensationOvertimePolicyService(
      final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository) {
    this.compensationOvertimeStatusRepository = compensationOvertimeStatusRepository;
  }

  public List<CompensationOvertimeStatus> findAll(final String companyId) {
    return compensationOvertimeStatusRepository.findByCompanyId(companyId);
  }

  public CompensationOvertimeStatus findByName(final String companyId, final String name) {
    return compensationOvertimeStatusRepository.findByCompanyIdAndName(companyId, name);
  }
}
