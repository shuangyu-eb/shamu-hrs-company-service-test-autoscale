package shamu.company.company;

public interface CompanyService {

  Boolean existsByName(String companyName);

  Boolean existsBySubdomainName(String subDomainName);
}
