package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.DeactivationReasons;
import shamu.company.user.repository.DeactivationReasonRepository;

@Service
@Transactional
public class DeactivationReasonService {

  private final DeactivationReasonRepository deactivationReasonRepository;

  @Autowired
  public DeactivationReasonService(
      final DeactivationReasonRepository deactivationReasonRepository) {
    this.deactivationReasonRepository = deactivationReasonRepository;
  }

  public List<DeactivationReasons> findAll() {
    return deactivationReasonRepository.findAll();
  }
}
