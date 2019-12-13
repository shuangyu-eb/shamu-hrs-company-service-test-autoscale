package shamu.company.company.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.repository.CompanySizeRepository;


@Service
@Transactional
public class CompanySizeService {

  private final CompanySizeRepository companySizeRepository;

  @Autowired
  public CompanySizeService(final CompanySizeRepository companySizeRepository) {
    this.companySizeRepository = companySizeRepository;
  }

  public List<CompanySize> findAll() {
    return companySizeRepository.findAll();
  }
}
