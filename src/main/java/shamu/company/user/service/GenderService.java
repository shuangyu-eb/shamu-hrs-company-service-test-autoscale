package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.Gender;
import shamu.company.user.repository.GenderRepository;

@Service
public class GenderService {

  private final GenderRepository genderRepository;

  @Autowired
  public GenderService(final GenderRepository genderRepository) {
    this.genderRepository = genderRepository;
  }

  public Gender findGenderById(final Long id) {
    return genderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Gender does not exist"));
  }
}
