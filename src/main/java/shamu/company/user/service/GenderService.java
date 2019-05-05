package shamu.company.user.service;

import shamu.company.user.entity.Gender;

public interface GenderService {
  Gender findGenderById(Long id);
}
