package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.repository.EthnicityRepository;

@Service
@Transactional
public class EthnicityService {

  private final EthnicityRepository ethnicityRepository;

  @Autowired
  public EthnicityService(final EthnicityRepository ethnicityRepository) {
    this.ethnicityRepository = ethnicityRepository;
  }

  public List<Ethnicity> findAll() {
    return ethnicityRepository.findAll();
  }
}
