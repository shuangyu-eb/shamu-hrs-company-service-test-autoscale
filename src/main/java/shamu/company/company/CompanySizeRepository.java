package shamu.company.company;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.BaseRepository;
import shamu.company.company.entity.CompanySize;

public interface CompanySizeRepository extends BaseRepository<CompanySize, Long> {

  @Query(value = "SELECT * FROM company_sizes WHERE name IN ?1", nativeQuery = true)
  List<CompanySize> findAllByName(List<String> nameList);
}
