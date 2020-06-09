package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.repository.MaritalStatusRepository;

@Service
public class MaritalStatusService {

  private final MaritalStatusRepository maritalStatusRepository;

  @Autowired
  public MaritalStatusService(final MaritalStatusRepository maritalStatusRepository) {
    this.maritalStatusRepository = maritalStatusRepository;
  }

  public MaritalStatus findById(final String id) {
    return maritalStatusRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("MaritalStatus with id %s not found!", id),
                    id,
                    "marital status"));
  }

  public List<MaritalStatus> findAll() {
    return maritalStatusRepository.findAll();
  }
}
