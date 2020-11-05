package shamu.company.financialengine.dto;

import lombok.Data;

@Data
public class BankAccountDto {
  private String bankAccountGuid;

  private String accountNumber;

  private String type;

  private String subtype;

  private Boolean verified;
}
