package shamu.company.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    CompanyRepository companyRepository;


    @Override
    public Boolean existsByName(String companyName) {
        return companyRepository.existsByName(companyName);
    }

    @Override
    public Boolean existsBySubdomainName(String subDomainName) {
        return companyRepository.existsBySubdomainName(subDomainName);
    }
}
