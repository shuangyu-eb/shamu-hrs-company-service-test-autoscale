package shamu.company.company.dto;

import lombok.Data;

@Data
public class OfficeDto {

  private String id;

  private String name;

  private OfficeAddressDto officeAddress;
}
