package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.Gender;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.service.GenderService;

@Service
public class GenderServiceImpl implements GenderService {
  private final GenderRepository genderRepository;

  @Autowired
  public GenderServiceImpl(GenderRepository genderRepository) {
    this.genderRepository = genderRepository;
  }

  @Override
  public Gender findGenderById(Long id) {
    return genderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Gender does not exist"));
  }
}
