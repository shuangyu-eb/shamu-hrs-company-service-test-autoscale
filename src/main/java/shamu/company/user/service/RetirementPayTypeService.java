package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.benefit.entity.RetirementPayTypes;
import shamu.company.user.repository.RetirementPayTypesRepository;

@Service
@Transactional
public class RetirementPayTypeService {

  private final RetirementPayTypesRepository retirementPayTypesRepository;

  @Autowired
  public RetirementPayTypeService(final RetirementPayTypesRepository retirementPayTypesRepository) {
    this.retirementPayTypesRepository = retirementPayTypesRepository;
  }

  public List<RetirementPayTypes> findAll() {
    return retirementPayTypesRepository.findAll();
  }

}
