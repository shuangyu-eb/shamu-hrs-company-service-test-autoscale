package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.repository.MaritalStatusRepository;

@Service
public class  MaritalStatusService {

  private final MaritalStatusRepository maritalStatusRepository;

  @Autowired
  public MaritalStatusService(final MaritalStatusRepository maritalStatusRepository) {
    this.maritalStatusRepository = maritalStatusRepository;
  }

  public MaritalStatus findMaritalStatusById(final Long id) {
    return maritalStatusRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("MaritalStatus does not exist"));
  }
}
