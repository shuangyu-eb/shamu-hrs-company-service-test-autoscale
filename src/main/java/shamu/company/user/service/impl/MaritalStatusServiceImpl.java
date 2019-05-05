package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.service.MaritalStatusService;

@Service
public class MaritalStatusServiceImpl implements MaritalStatusService {

  private final MaritalStatusRepository maritalStatusRepository;

  @Autowired
  public MaritalStatusServiceImpl(MaritalStatusRepository maritalStatusRepository) {
    this.maritalStatusRepository = maritalStatusRepository;
  }

  @Override
  public MaritalStatus findMaritalStatusById(Long id) {
    return maritalStatusRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("MaritalStatus does not exist"));
  }
}
