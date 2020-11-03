package shamu.company.financialengine.dto;

import java.util.List;
import lombok.Data;

// get fe available tax ID's list
@Data
public class CompanyTaxIdDto {

  private List<String> taxIdList;
}
