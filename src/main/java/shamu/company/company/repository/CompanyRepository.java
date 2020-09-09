package shamu.company.company.repository;

import org.jetbrains.annotations.NotNull;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;

public interface CompanyRepository
    extends BaseRepository<Company, String>, CompanyCustomRepository {

  /**
   * Please don't use directly because tenants table need to be updated when the company is updated.
   * companyService.save() could instead this method.
   *
   * @deprecated (Use @Deprecated to alert the developer don't use this method directly.)
   */
  @Deprecated
  @NotNull
  @Override
  Company save(@NotNull Company company);
}
