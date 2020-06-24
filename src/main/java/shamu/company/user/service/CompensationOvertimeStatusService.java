package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;

import java.util.List;

@Service
@Transactional
public class CompensationOvertimeStatusService {

  private final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  @Autowired
  public CompensationOvertimeStatusService(
      final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository) {
    this.compensationOvertimeStatusRepository = compensationOvertimeStatusRepository;
  }

  public List<CompensationOvertimeStatus> findAll() {
    return compensationOvertimeStatusRepository.findAll();
  }

  public CompensationOvertimeStatus findByName(final String name) {
    return compensationOvertimeStatusRepository.findByName(name);
  }
}
