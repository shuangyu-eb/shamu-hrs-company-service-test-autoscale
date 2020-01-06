package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;

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
}
