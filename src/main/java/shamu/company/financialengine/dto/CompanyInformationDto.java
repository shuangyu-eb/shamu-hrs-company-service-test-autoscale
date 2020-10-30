package shamu.company.financialengine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyInformationDto {

  private String legalName;

  private String businessName;

  private List<CompanyAddressesDto> companyAddresses;

  private String ein;

  private String firstPayDate;

  private String phoneNumber;

  private String stateEddNumber;

  private String industry;

  private String legalEntityType;

  private Boolean isAgriculturalBusiness;

  private Timestamp agriculturalBusinessVerifiedAt;

  private Boolean nonprofitExemptStatus;

  private Timestamp nonprofitExemptVerifiedAt;

  private String nonprofitExemptDocumentation;

  private String companyPayoutFrequencyType;

  private String companyPayoutMethodType;

  private AuthorizedSignerDto authorizedSigner;
}
