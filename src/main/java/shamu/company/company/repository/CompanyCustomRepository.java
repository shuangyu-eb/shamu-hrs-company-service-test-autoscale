package shamu.company.company.repository;

public interface CompanyCustomRepository {

  Boolean existsByName(final String companyName);
}
