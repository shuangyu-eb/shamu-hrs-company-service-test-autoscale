package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.Gender;
import shamu.company.user.repository.GenderRepository;

@Service
public class GenderService {

  private final GenderRepository genderRepository;

  @Autowired
  public GenderService(final GenderRepository genderRepository) {
    this.genderRepository = genderRepository;
  }

  public Gender findById(final String id) {
    return genderRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Gender with id %s not found!", id), id, "gender"));
  }

  public List<Gender> findAll() {
    return genderRepository.findAll();
  }
}
