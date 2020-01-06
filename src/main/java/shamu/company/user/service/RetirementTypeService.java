package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.user.repository.RetirementTypeRepository;

@Service
@Transactional
public class RetirementTypeService {

  private final RetirementTypeRepository retirementTypeRepository;

  @Autowired
  public RetirementTypeService(final RetirementTypeRepository retirementTypeRepository) {
    this.retirementTypeRepository = retirementTypeRepository;
  }

  public List<RetirementType> findAll() {
    return retirementTypeRepository.findAll();
  }
}
